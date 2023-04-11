package bot.service.impl;

import bot.binance.*;
import bot.dto.LeverageLevel;
import bot.dto.ProfitLevel;
import bot.entity.Funding;
import bot.entity.Trade;
import bot.repository.FundingRepository;
import bot.repository.TradeRepository;
import bot.service.TradingService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TradingServiceImpl implements TradingService {

    final SimpleDateFormat formatter;
    final String websocketUrl;
    final String dateFormatPattern;
    final String spliterator;
    final String updateWebsocketSuffix;
    String symbol;
    Double rate;
    Double price;
    Double quantity;
    Integer roundStart;
    int round = -1;

    TradeRepository tradeRepository;
    FundingRepository fundingRepository;

    @Autowired
    public TradingServiceImpl(@Value("${websocket.url}") String websocketUrl,
                              @Value("${symbol.default}") String symbol,
                              @Value("${round.start}") Integer roundStart,
                              @Value("${date.format.pattern}") String dateFormatPattern,
                              @Value("${update.websocket.suffix}") String updateWebsocketSuffix,
                              @Value("${spliterator}") String spliterator,
                              TradeRepository tradeRepository,
                              FundingRepository fundingRepository) {
        this.websocketUrl = websocketUrl;
        this.symbol = symbol;
        this.roundStart = roundStart;
        this.dateFormatPattern = dateFormatPattern;
        this.updateWebsocketSuffix = updateWebsocketSuffix;
        this.spliterator = spliterator;
        this.tradeRepository = tradeRepository;
        this.fundingRepository = fundingRepository;
        this.formatter = new SimpleDateFormat(dateFormatPattern);
    }

    @Override
    @SneakyThrows
    public void open(SyncRequestClient client) {
        client.setResponsePrice(0.0);
        ProfitLevel profitLevel = ProfitLevel.getProfitLevel(Math.abs(rate));
        if (ProfitLevel.REJECT.equals(profitLevel)) {
            log.info("{}: rate {} is lower than limit {}", client.getName(), rate, profitLevel.getFunding());
            return;
        }
        try {
            client.changeMarginType(symbol, MarginType.ISOLATED);
        } catch (Exception ignored) {
        }

        double accountBalance = getAccountBalance(client);
        int leverage = LeverageLevel.getLeverage(accountBalance);
        client.changeInitialLeverage(symbol, leverage);
        quantity = accountBalance * client.getPercentage();
        quantity *= leverage;
        quantity /= price;
        client.setPositionQuantity(quantity.intValue() > 0 ?
                String.valueOf(quantity.intValue()) :
                String.format("%.1f", quantity));
        client.setOrderSide(rate > 0 ? OrderSide.BUY : OrderSide.SELL);
        if (!"0.0".equals(client.getPositionQuantity())) {
            log.info("{}: open quantity = {}, rate = {}, order side = {}", client.getName(),
                    client.getPositionQuantity(),
                    rate,
                    client.getOrderSide());
            sendMarketOrder(client);
            closeLimit(client);
            client.setOpenBalance(accountBalance);
        } else {
            log.warn("{}: position quantity = {}. Orders have not been sent", client.getName(),
                    client.getPositionQuantity());
        }
    }

    @Override
    public void close(SyncRequestClient client) {
        Optional<Position> position = client.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();
        client.cancelAllOpenOrder(symbol);
        if (position.isEmpty() || position.get().getPositionAmt().doubleValue() == 0.0) {
            log.info("{}: position {} is already closed", client.getName(), symbol);
            if (client.getOpenBalance() != 0.0) {
                double accountBalance = getAccountBalance(client);
                Trade trade = Trade.builder()
                        .date(formatter.format(new Date()))
                        .name(client.getName())
                        .symbol(symbol)
                        .balanceBefore(client.getOpenBalance())
                        .balanceAfter(accountBalance)
                        .profit(accountBalance / client.getOpenBalance() - 1.0)
                        .fundingRate(rate)
                        .build();
                tradeRepository.save(trade);
                client.setOpenBalance(0.0);
            }
            return;
        }

        if (OrderSide.BUY.equals(client.getOrderSide())) {
            client.setOrderSide(OrderSide.SELL);
        } else {
            client.setOrderSide(OrderSide.BUY);
        }
        if (position.get().getPositionAmt().doubleValue() < 0) {
            client.setPositionQuantity(String.valueOf(-1 * position.get().getPositionAmt().doubleValue()));
        }
        sendMarketOrder(client);
    }

    @Override
    public void closeLimit(SyncRequestClient client) {
        Optional<Position> position = client.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();
        if (position.isPresent() && position.get().getPositionAmt() != null) {
            if (round == -1) {
                round = price > 1 ? 4 : roundStart;
                round = price > 100 ? 3 : round;
            }
            double absoluteRate = Math.abs(rate);
            ProfitLevel profitLevel = ProfitLevel.getProfitLevel(absoluteRate);
            while (!sendLimitOrder(client, round, profitLevel) && round > 0) {
                round--;
            }
        } else {
            log.info("{}: no positions for {}", client.getName(), symbol);
        }
    }

    @Override
    public boolean sendLimitOrder(SyncRequestClient client, int round, ProfitLevel profitLevel) {
        try {
            OrderSide side = client.getOrderSide();
            if (OrderSide.BUY.equals(side)) {
                side = OrderSide.SELL;
                price = BigDecimal.valueOf(client.getResponsePrice() * (1 + rate + profitLevel.getProfit()))
                        .setScale(round, RoundingMode.HALF_UP)
                        .doubleValue();
            } else {
                side = OrderSide.BUY;
                price = BigDecimal.valueOf(client.getResponsePrice() * (1 + rate - profitLevel.getProfit()))
                        .setScale(round, RoundingMode.HALF_DOWN)
                        .doubleValue();
            }
            Order order = client.postOrder(
                    symbol, side, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC, client.getPositionQuantity(),
                    price.toString(), null, null, null, null, null, null, null, null,
                    NewOrderRespType.RESULT);
            log.info("{}: {} limit close with round = {} and price = {}", client.getName(), symbol, round, order.getPrice());
            return true;
        } catch (BinanceApiException binanceApiException) {
            log.error(binanceApiException.getMessage());
            return false;
        }
    }

    @Override
    public void sendMarketOrder(SyncRequestClient client) {
        Order order = client.postOrder(
                symbol, client.getOrderSide(), PositionSide.BOTH, OrderType.MARKET, null, client.getPositionQuantity(),
                null, null, null, null, null, null, null, null, null,
                NewOrderRespType.RESULT);
        client.setResponsePrice(order.getAvgPrice().doubleValue());
        client.setPositionQuantity(order.getExecutedQty().toString());
        log.info("{}: {} open order sent with executed avg price = {} and quantity = {}",
                client.getName(), symbol, client.getResponsePrice(), client.getPositionQuantity());
    }

    @Override
    public void updateFunding() {
        List<String> elements = getFunding();
        symbol = elements.get(0);
        rate = Double.parseDouble(elements.get(1));
        price = Double.parseDouble(elements.get(2));
        ProfitLevel profitLevel = ProfitLevel.getProfitLevel(Math.abs(rate));

        Funding funding = Funding.builder()
                .date(formatter.format(new Date()))
                .rate(rate)
                .symbol(symbol)
                .skip(ProfitLevel.REJECT.equals(profitLevel))
                .build();
        fundingRepository.save(funding);
    }

    @Override
    @SneakyThrows
    public List<String> getFunding() {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpUriRequest request = new HttpGet(websocketUrl);
            HttpResponse response = client.execute(request);
            var bufReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            List<String> elements = Collections.emptyList();
            while ((line = bufReader.readLine()) != null) {
                elements = Arrays.asList(line.split(spliterator));
                log.info("Funding with symbol: {}, rate: {}, price: {}",
                        elements.get(0),
                        elements.get(1),
                        elements.get(2));
            }
            return elements;
        }
    }

    @Override
    public double getAccountBalance(SyncRequestClient client) {
        AccountInformation accountInformation = client.getAccountInformation();
        return accountInformation.getAvailableBalance().doubleValue();
    }

    @Override
    @SneakyThrows
    public void reconnectSocket() {
        log.info("Reconnect websocket!");
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpUriRequest request = new HttpPost(websocketUrl + updateWebsocketSuffix);
            client.execute(request);
        }
    }

    @Override
    public void logFunding() {
        Funding funding = fundingRepository.findFirstByOrderByIdDesc()
                .orElseThrow(() -> new NoSuchElementException("No fundings found!"));
        List<String> elements = getFunding();
        double priceStretch = Double.parseDouble(elements.get(2));
        funding.setPriceBefore(price);
        funding.setPriceAfter(priceStretch);
        funding.setStretch(priceStretch / price - 1.0);
        fundingRepository.save(funding);
    }
}
