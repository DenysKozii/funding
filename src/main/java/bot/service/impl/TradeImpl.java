package bot.service.impl;

import bot.dto.LogDto;
import bot.dto.LogPreviewDto;
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
    Double tradeLimit;
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
                     @Value("${trade.limit}") Double tradeLimit,
                     @Value("${profit.limit}") Double profitLimit,
                     @Value("${leverage}") Integer leverage,
                     @Value("${symbol.default}") String symbol,
                     @Value("${round.start}") Integer roundStart,
                     @Value("${date.format.pattern}") String dateFormatPattern,
                     LogRepository logRepository) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.tradeLimit = tradeLimit;
        this.profitLimit = profitLimit;
        this.leverage = leverage;
        this.symbol = symbol;
        this.roundStart = roundStart;
        this.dateFormatPattern = dateFormatPattern;
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
        if (Math.abs(rate) < tradeLimit) {
            logOrder(OrderStatus.OPEN, getAccountBalance(clientFutures));
            log.info("rate {} is lower than limit {}", rate, tradeLimit);
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
        log.info("open quantity = {}", positionQuantity);
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
        sendMarketOrder(clientFutures);
        logOrder(OrderStatus.CLOSE, getAccountBalance(clientFutures));
    }

    @Override
    public void closeLimit(SyncRequestClient clientFutures) {
        Optional<Position> position = clientFutures.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();
        if (position.isPresent()) {
            int round = price > 1 ? 4 : roundStart;
            round = price > 100 ? 3 : round;
            while (round > 0 && !sendLimitOrder(clientFutures, round)) {
                round--;
            }
            logOrder(OrderStatus.CLOSE, getAccountBalance(clientFutures));
        } else {
            log.info("No positions for {}", symbol);
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
    public boolean sendLimitOrder(SyncRequestClient clientFutures, int round) {
        try {
            if (OrderSide.BUY.equals(orderSide)) {
                orderSide = OrderSide.SELL;
                price = new BigDecimal(responsePrice * (1 + rate + profitLimit))
                        .setScale(round, RoundingMode.HALF_UP)
                        .doubleValue();
            } else {
                orderSide = OrderSide.BUY;
                price = new BigDecimal(responsePrice * (1 + rate - profitLimit))
                        .setScale(round, RoundingMode.HALF_DOWN)
                        .doubleValue();
            }
            log.info("{} limit close with round = {}", symbol, round);
            Order order = clientFutures.postOrder(
                    symbol, orderSide, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC, positionQuantity,
                    price.toString(), null, null, null, null, null, null, null, null,
                    NewOrderRespType.RESULT);
            responsePrice = order.getAvgPrice().doubleValue();
            return true;
        } catch (BinanceApiException binanceApiException) {
            return false;
        }
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
                log.info("symbol = {}", line.split(";")[0]);
                log.info("rate = {}", Double.parseDouble(line.split(";")[1]));
                log.info("price = {}", Double.parseDouble(line.split(";")[2]));
            }
        }
    }

    @Override
    public double getAccountBalance(SyncRequestClient clientFutures) {
        AccountInformation accountInformation = clientFutures.getAccountInformation();
        return accountInformation.getAvailableBalance().doubleValue();
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
