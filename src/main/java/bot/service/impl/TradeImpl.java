package bot.service.impl;

import bot.binance.*;
import bot.dto.LeverageLevel;
import bot.dto.ProfitLevel;
import bot.service.Trade;
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
import java.util.*;

@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TradeImpl implements Trade {

    final String websocketUrl;
    final Double tradePercentage;
    final String dateFormatPattern;
    final String spliterator;
    final String updateWebsocketSuffix;
    String symbol;
    Double rate;
    Double price;
    Double responsePrice;
    Double quantity;
    String positionQuantity;
    OrderSide orderSide;
    Integer roundStart;

    @Autowired
    public TradeImpl(@Value("${websocket.url}") String websocketUrl,
                     @Value("${trade.percentage}") Double tradePercentage,
                     @Value("${symbol.default}") String symbol,
                     @Value("${round.start}") Integer roundStart,
                     @Value("${date.format.pattern}") String dateFormatPattern,
                     @Value("${update.websocket.suffix}") String updateWebsocketSuffix,
                     @Value("${spliterator}") String spliterator) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.symbol = symbol;
        this.roundStart = roundStart;
        this.dateFormatPattern = dateFormatPattern;
        this.updateWebsocketSuffix = updateWebsocketSuffix;
        this.spliterator = spliterator;
    }

    @Override
    @SneakyThrows
    public void open(SyncRequestClient clientFutures) {
        responsePrice = 0.0;
        ProfitLevel profitLevel = ProfitLevel.getProfitLevel(Math.abs(rate));
        if (ProfitLevel.REJECT.equals(profitLevel)) {
            log.info("rate {} is lower than limit {}", rate, profitLevel.getFunding());
            return;
        }
        try {
            clientFutures.changeMarginType(symbol, MarginType.ISOLATED);
        } catch (Exception ignored) {
        }

        double accountBalance = getAccountBalance(clientFutures);
        int leverage = LeverageLevel.getLeverage(accountBalance);
        clientFutures.changeInitialLeverage(symbol, leverage);
        quantity = accountBalance * tradePercentage;
        quantity *= leverage;
        quantity /= price;
        positionQuantity = quantity.intValue() > 0 ?
                String.valueOf(quantity.intValue()) :
                String.format("%.1f", quantity);
        orderSide = rate > 0 ? OrderSide.BUY : OrderSide.SELL;
        log.info("open quantity = {}, rate = {}, order side = {}", positionQuantity, rate, orderSide);
        sendMarketOrder(clientFutures);
        closeLimit(clientFutures);
    }

    @Override
    public void close(SyncRequestClient clientFutures) {
        Optional<Position> position = clientFutures.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();
        clientFutures.cancelAllOpenOrder(symbol);
        if (position.isEmpty() || position.get().getPositionAmt().doubleValue() == 0.0) {
            log.info("position {} is already closed", symbol);
            return;
        }

        if (OrderSide.BUY.equals(orderSide)) {
            orderSide = OrderSide.SELL;
        } else {
            orderSide = OrderSide.BUY;
        }
        if (position.get().getPositionAmt().doubleValue() < 0) {
            positionQuantity = String.valueOf(-1 * position.get().getPositionAmt().doubleValue());
        }
        sendMarketOrder(clientFutures);
    }

    @Override
    public void closeLimit(SyncRequestClient clientFutures) {
        Optional<Position> position = clientFutures.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();
        if (position.isPresent() && position.get().getPositionAmt() != null) {
            int round = price > 1 ? 4 : roundStart;
            round = price > 100 ? 3 : round;
            double absoluteRate = Math.abs(rate);
            ProfitLevel profitLevel = ProfitLevel.getProfitLevel(absoluteRate);
            while (!sendLimitOrder(clientFutures, round, profitLevel) && round > 0) {
                round--;
            }
        } else {
            log.info("No positions for {}", symbol);
        }
    }

    @Override
    public boolean sendLimitOrder(SyncRequestClient clientFutures, int round, ProfitLevel profitLevel) {
        try {
            OrderSide side;
            if (OrderSide.BUY.equals(orderSide)) {
                side = OrderSide.SELL;
                price = new BigDecimal(responsePrice * (1 + rate + profitLevel.getProfit()))
                        .setScale(round, RoundingMode.HALF_UP)
                        .doubleValue();
            } else {
                side = OrderSide.BUY;
                price = new BigDecimal(responsePrice * (1 + rate - profitLevel.getProfit()))
                        .setScale(round, RoundingMode.HALF_DOWN)
                        .doubleValue();
            }
            Order order = clientFutures.postOrder(
                    symbol, side, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC, positionQuantity,
                    price.toString(), null, null, null, null, null, null, null, null,
                    NewOrderRespType.RESULT);
            log.info("{} limit close with round = {} and price = {}", symbol, round, order.getPrice());
            return true;
        } catch (BinanceApiException binanceApiException) {
            log.error(binanceApiException.getMessage());
            return false;
        }
    }

    @Override
    public void sendMarketOrder(SyncRequestClient clientFutures) {
        Order order = clientFutures.postOrder(
                symbol, orderSide, PositionSide.BOTH, OrderType.MARKET, null, positionQuantity,
                null, null, null, null, null, null, null, null, null,
                NewOrderRespType.RESULT);
        responsePrice = order.getAvgPrice().doubleValue();
        positionQuantity = order.getExecutedQty().toString();
        log.info("{} order sent with executed avg price = {} and quantity = {}", symbol, responsePrice, positionQuantity);
    }

    @Override
    @SneakyThrows
    public void updateFunding() {
        List<String> funding = getFunding();
        symbol = funding.get(0);
        rate = Double.parseDouble(funding.get(1));
        price = Double.parseDouble(funding.get(2));
        log.info("symbol = {}", symbol);
        log.info("rate = {}", rate);
        log.info("price = {}", price);
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
                log.info("{}: rate = {}, price = {}", elements.get(0),
                        Double.parseDouble(elements.get(1)),
                        Double.parseDouble(elements.get(2)));
            }
            return elements;
        }
    }

    @Override
    public double getAccountBalance(SyncRequestClient clientFutures) {
        AccountInformation accountInformation = clientFutures.getAccountInformation();
        return accountInformation.getAvailableBalance().doubleValue();
    }

    @SneakyThrows
    @Override
    public void reconnectSocket() {
        log.info("reconnect websocket");
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpUriRequest request = new HttpPost(websocketUrl + updateWebsocketSuffix);
            client.execute(request);
        }
    }

}
