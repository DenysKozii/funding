package bot.service.impl;

import bot.dto.*;
import bot.dto.OrderStatus;
import bot.entity.Log;
import bot.repository.LogRepository;
import bot.service.Trade;
import com.binance.client.SyncRequestClient;
import com.binance.client.exception.BinanceApiException;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.AccountInformation;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.Position;
import lombok.AccessLevel;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TradeImpl implements Trade {

    final LogRepository logRepository;

    final String websocketUrl;
    final Double tradePercentage;
    final String dateFormatPattern;
    final String spliterator;
    Double fundingLimit;
    Double profitLimit;
    Integer leverage;
    String symbol;
    Double rate;
    Double price;
    Long groupId;
    Double responsePrice;
    Double quantity;
    String positionQuantity;
    OrderSide orderSide;
    Integer roundStart;

    @Autowired
    public TradeImpl(@Value("${websocket.url}") String websocketUrl,
                     @Value("${trade.percentage}") Double tradePercentage,
                     @Value("${funding.limit}") Double fundingLimit,
                     @Value("${profit.limit}") Double profitLimit,
                     @Value("${leverage}") Integer leverage,
                     @Value("${symbol.default}") String symbol,
                     @Value("${round.start}") Integer roundStart,
                     @Value("${date.format.pattern}") String dateFormatPattern,
                     @Value("${spliterator}") String spliterator,
                     LogRepository logRepository) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.fundingLimit = fundingLimit;
        this.profitLimit = profitLimit;
        this.leverage = leverage;
        this.symbol = symbol;
        this.roundStart = roundStart;
        this.dateFormatPattern = dateFormatPattern;
        this.spliterator = spliterator;
        this.logRepository = logRepository;
        if (logRepository.count() > 0) {
            this.groupId = logRepository.findAll().get((int) logRepository.count() - 1).getGroupId();
        } else {
            this.groupId = 0L;
        }
    }

    @Override
    @SneakyThrows
    public void open(SyncRequestClient clientFutures) {
        groupId++;
        responsePrice = 0.0;
        if (Math.abs(rate) < fundingLimit) {
            logOrder(OrderStatus.OPEN, getAccountBalance(clientFutures));
            log.info("rate {} is lower than limit {}", rate, fundingLimit);
            return;
        }
        try {
            clientFutures.changeMarginType(symbol, MarginType.ISOLATED);
        } catch (Exception ignored) {
        }
        clientFutures.changeInitialLeverage(symbol, leverage);

        double accountBalance = getAccountBalance(clientFutures);
        quantity = accountBalance * tradePercentage;
        quantity *= leverage;
        quantity /= price;
        positionQuantity = quantity.intValue() > 0 ?
                String.valueOf(quantity.intValue()) :
                String.format("%.1f", quantity);
        orderSide = rate > 0 ? OrderSide.BUY : OrderSide.SELL;
        log.info("open quantity = {}, rate = {}, order side = {}", positionQuantity, rate, orderSide);
        sendMarketOrder(clientFutures);
        logOrder(OrderStatus.OPEN, accountBalance);
    }

    @Override
    public void close(SyncRequestClient clientFutures) {
        Optional<Position> position = clientFutures.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();
        clientFutures.cancelAllOpenOrder(symbol);
        if (position.isEmpty() || position.get().getPositionAmt().doubleValue() == 0.0) {
            logOrder(OrderStatus.CLOSE, getAccountBalance(clientFutures));
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
        logOrder(OrderStatus.CLOSE, getAccountBalance(clientFutures));
    }

    @Override
    public void closeLimit(SyncRequestClient clientFutures) {
        Optional<Position> position = clientFutures.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();
        if (position.isPresent() && position.get().getPositionAmt() != null) {
            int round = price > 1 ? 4 : roundStart;
            round = price > 100 ? 3 : round;
            double absoluteRate = Math.abs(rate);
            if (absoluteRate < ProfitLevel.LOW.getFunding()) {
                profitLimit = ProfitLevel.LOW.getProfit();
            } else if (absoluteRate < ProfitLevel.MEDIUM.getFunding()) {
                profitLimit = ProfitLevel.MEDIUM.getFunding();
            } else if (absoluteRate < ProfitLevel.HIGH.getFunding()) {
                profitLimit = ProfitLevel.HIGH.getFunding();
            } else if (absoluteRate < ProfitLevel.SUPER.getFunding()) {
                profitLimit = ProfitLevel.SUPER.getFunding();
            }
            while (!sendLimitOrder(clientFutures, round) && round > 0) {
                round--;
            }
            logOrder(OrderStatus.CLOSE, getAccountBalance(clientFutures));
        } else {
            log.info("No positions for {}", symbol);
        }
    }

    @Override
    public boolean sendLimitOrder(SyncRequestClient clientFutures, int round) {
        try {
            OrderSide side;
            if (OrderSide.BUY.equals(orderSide)) {
                side = OrderSide.SELL;
                price = new BigDecimal(responsePrice * (1 + rate + profitLimit))
                        .setScale(round, RoundingMode.HALF_UP)
                        .doubleValue();
            } else {
                side = OrderSide.BUY;
                price = new BigDecimal(responsePrice * (1 + rate - profitLimit))
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
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpUriRequest request = new HttpGet(websocketUrl);
            HttpResponse response = client.execute(request);
            var bufReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = bufReader.readLine()) != null) {
                symbol = line.split(";")[0];
                rate = Double.parseDouble(line.split(";")[1]);
                price = Double.parseDouble(line.split(";")[2]);
                log.info("symbol = {}", symbol);
                log.info("rate = {}", rate);
                log.info("price = {}", price);
            }
        }
    }

    @Override
    @SneakyThrows
    public void logFunding() {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpUriRequest request = new HttpGet(websocketUrl);
            HttpResponse response = client.execute(request);
            var bufReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = bufReader.readLine()) != null) {
                log.info("symbol = {}", line.split(spliterator)[0]);
                log.info("rate = {}", Double.parseDouble(line.split(spliterator)[1]));
                log.info("price = {}", Double.parseDouble(line.split(spliterator)[2]));
            }
            log.info("leverage = {}", leverage);
            log.info("profit limit = {}", profitLimit);
            log.info("funding rate limit = {}", fundingLimit);
        }
    }

    @Override
    public double getAccountBalance(SyncRequestClient clientFutures) {
        AccountInformation accountInformation = clientFutures.getAccountInformation();
        return accountInformation.getAvailableBalance().doubleValue();
    }

    @Override
    public void updateSettings(SettingsDto settings) {
        leverage = settings.getLeverage();
        fundingLimit = settings.getFundingLimit();
    }

    @Override
    public List<LogDto> getLogsByGroupId(Long groupId) {
        List<LogDto> logs = new ArrayList<>();
        double openPrice = 0;
        double openAccountBalance = 0;
        double changeBalancePercent;
        OrderSide openOrderSide = OrderSide.BUY;
        for (Log log : logRepository.findAll()) {
            if (OrderStatus.OPEN.equals(log.getOrderStatus())) {
                openPrice = log.getResponsePrice() == 0 ? log.getPrice() : log.getResponsePrice();
                openOrderSide = log.getOrderSide();
                openAccountBalance = log.getAccountBalance();
            }
            changeBalancePercent = OrderSide.BUY.equals(openOrderSide) ? log.getPrice() / openPrice : openPrice / log.getPrice();
            if (Objects.equals(log.getGroupId(), groupId)) {
                LogDto logDto = LogDto.builder()
                        .groupId(log.getGroupId())
                        .date(log.getDate())
                        .symbol(log.getSymbol())
                        .name("Denys")
                        .rate(log.getRate())
                        .orderStatus(log.getOrderStatus())
                        .price(log.getPrice())
                        .responsePrice(log.getResponsePrice())
                        .accountBalance(log.getAccountBalance())
                        .orderSide(log.getOrderSide())
                        .priceChangePercent(changeBalancePercent)
                        .accountBalanceChangePercent(log.getAccountBalance() / openAccountBalance)
                        .build();
                logs.add(logDto);
            }
        }
        return logs;
    }

    @Override
    public List<LogPreviewDto> getLogPreviews() {
        return logRepository.findAll().stream()
                .filter(log -> OrderStatus.OPEN.equals(log.getOrderStatus()))
                .map(log -> new LogPreviewDto(log.getGroupId(), log.getDate(), log.getSymbol()))
                .collect(Collectors.toList());
    }

    @Override
    public void logOrder(OrderStatus orderStatus, Double accountBalance) {
        DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
        Date date = new Date();
        Log log = Log.builder()
                .groupId(groupId)
                .date(dateFormat.format(date))
                .symbol(symbol)
                .rate(rate)
                .price(price)
                .responsePrice(responsePrice)
                .orderStatus(orderStatus)
                .accountBalance(accountBalance)
                .orderSide(orderSide)
                .build();
        logRepository.save(log);
    }
}
