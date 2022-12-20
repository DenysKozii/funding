package bot.service.impl;

import bot.dto.LogDto;
import bot.dto.LogPreviewDto;
import bot.dto.OrderStatus;
import bot.entity.Log;
import bot.repository.LogRepository;
import bot.service.Trade;
import com.binance.client.SyncRequestClient;
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

    @Autowired
    public TradeImpl(@Value("${websocket.url}") String websocketUrl,
                     @Value("${trade.percentage}") Double tradePercentage,
                     @Value("${trade.limit}") Double tradeLimit,
                     @Value("${profit.limit}") Double profitLimit,
                     @Value("${leverage}") Integer leverage,
                     @Value("${symbol.default}") String symbol,
                     @Value("${date.format.pattern}") String dateFormatPattern,
                     LogRepository logRepository) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.tradeLimit = tradeLimit;
        this.profitLimit = profitLimit;
        this.leverage = leverage;
        this.symbol = symbol;
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
            int round = Double.toString(responsePrice).split("\\.")[1].length();
            log.info("round for {} = {}", responsePrice, round);
            if (OrderSide.BUY.equals(orderSide)) {
                orderSide = OrderSide.SELL;
                price = new BigDecimal(responsePrice * (1 - rate + profitLimit))
                        .setScale(round / 2, RoundingMode.HALF_EVEN)
                        .doubleValue();
            } else {
                orderSide = OrderSide.BUY;
                price = new BigDecimal(responsePrice * (1 + rate - profitLimit))
                        .setScale(round / 2, RoundingMode.HALF_EVEN)
                        .doubleValue();
            }
            log.info("limit sell price = {}", price);
            sendLimitOrder(clientFutures);
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
    public void sendLimitOrder(SyncRequestClient clientFutures) {
        Order order = clientFutures.postOrder(
                symbol, orderSide, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC, positionQuantity,
                price.toString(), null, null, null, null, null, null, null, null,
                NewOrderRespType.RESULT);
        responsePrice = order.getAvgPrice().doubleValue();
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
        double mean10 = 0;
        double mean15 = 0;
        double mean30 = 0;
        double mean59 = 0;
        double mean = 0;
        OrderSide openOrderSide = OrderSide.BUY;
        int i = 0;
        for (Log log : logRepository.findAll()) {
            if (OrderStatus.OPEN.equals(log.getOrderStatus())) {
                openPrice = log.getResponsePrice() == 0 ? log.getPrice() : log.getResponsePrice();
                openOrderSide = log.getOrderSide();
                openAccountBalance = log.getAccountBalance();
            }
            changeBalancePercent = OrderSide.BUY.equals(openOrderSide) ? log.getPrice() / openPrice : openPrice / log.getPrice();
            if (i % 6 == 2) {
                mean10 += changeBalancePercent;
                mean = mean10 / ((int) (i / 6 + 1));
            } else if (i % 6 == 3) {
                mean15 += changeBalancePercent;
                mean = mean15 / ((int) (i / 6 + 1));
            } else if (i % 6 == 4) {
                mean30 += changeBalancePercent;
                mean = mean30 / ((int) (i / 6 + 1));
            } else if (i % 6 == 5) {
                mean59 += changeBalancePercent;
                mean = mean59 / ((int) (i / 6 + 1));
            }
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
                        .meanChangePercent(mean)
                        .accountBalanceChangePercent(log.getAccountBalance() / openAccountBalance)
                        .build();
                logs.add(logDto);
            }
            i++;
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
