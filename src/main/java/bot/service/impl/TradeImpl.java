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
import lombok.experimental.NonFinal;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TradeImpl implements Trade {

    String websocketUrl;
    Double tradePercentage;
    Double tradeLimit;
    Integer leverage;
    String dateFormatPattern;
    @NonFinal
    String symbol;
    @NonFinal
    Double rate;
    @NonFinal
    Double price;
    @NonFinal
    Long groupId;
    @NonFinal
    Double responsePrice;
    @NonFinal
    Double quantity;
    @NonFinal
    String positionQuantity;
    @NonFinal
    OrderSide orderSide;

    LogRepository logRepository;

    @Autowired
    public TradeImpl(@Value("${websocket.url}") String websocketUrl,
                     @Value("${trade.percentage}") Double tradePercentage,
                     @Value("${trade.limit}") Double tradeLimit,
                     @Value("${leverage}") Integer leverage,
                     @Value("${symbol.default}") String symbol,
                     @Value("${date.format.pattern}") String dateFormatPattern,
                     LogRepository logRepository) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.tradeLimit = tradeLimit;
        this.leverage = leverage;
        this.symbol = symbol;
        this.dateFormatPattern = dateFormatPattern;
        this.logRepository = logRepository;
        this.groupId = logRepository.findAll().get((int) logRepository.count() - 1).getGroupId();
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
        quantity = getAccountBalance(clientFutures) * tradePercentage;
        quantity *= leverage;
        quantity /= price;
        String positionQuantity = quantity.intValue() > 0 ? String.valueOf(quantity.intValue()) : String.format("%.1f", quantity);
        log.info("position quantity = {}", positionQuantity);
        log.info("position price = {}", price);
        logOrder(OrderStatus.OPEN, getAccountBalance(clientFutures));
        if (rate < 0) {
            orderSide = OrderSide.BUY;
            sendOrder(positionQuantity, OrderType.MARKET, null, clientFutures);
        } else {
            orderSide = OrderSide.SELL;
            sendOrder(positionQuantity, OrderType.MARKET, null, clientFutures);
        }
    }

    @Override
    public void close(OrderType orderType, TimeInForce timeInForce, SyncRequestClient clientFutures) {
        Optional<Position> position = clientFutures.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();

        if (position.isEmpty() || position.get().getPositionAmt().doubleValue() == 0.0) {
            logOrder(OrderStatus.CLOSE, getAccountBalance(clientFutures));
            log.info("position {} is already closed", symbol);
            return;
        }

        String positionQuantity = position.get().getPositionAmt().toString();
        if (OrderSide.BUY.equals(orderSide)) {
            orderSide = OrderSide.SELL;
            sendOrder(positionQuantity, orderType, timeInForce, clientFutures);
        } else {
            orderSide = OrderSide.BUY;
            positionQuantity = String.valueOf(-1 * Double.parseDouble(positionQuantity));
            sendOrder(positionQuantity, orderType, timeInForce, clientFutures);
        }
        logOrder(OrderStatus.CLOSE, getAccountBalance(clientFutures));
    }

    @Override
    public void sendOrder(String positionQuantity, OrderType orderType, TimeInForce timeInForce, SyncRequestClient clientFutures) {
        double sellPrice = 0;
        if (OrderType.LIMIT.equals(orderType)) {
            int round = Double.toString(responsePrice).split("\\.")[1].length();
            log.info("round for {} = {}", responsePrice, round);
            sellPrice = new BigDecimal(responsePrice - responsePrice * Math.abs(rate) / 1.5).setScale(round, RoundingMode.HALF_EVEN).doubleValue();
            log.info("sell price = {}", sellPrice);
        }
        Order order = clientFutures.postOrder(
                symbol, orderSide, PositionSide.BOTH, orderType, timeInForce, positionQuantity,
                OrderType.LIMIT.equals(orderType) ? Double.toString(sellPrice) : null,
                null, null, null, null, null, null, null, null,
                NewOrderRespType.RESULT);
        responsePrice = order.getAvgPrice().doubleValue();
        log.info("{} order sent with executed avg price = {}", symbol, order.getAvgPrice().doubleValue());
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
    public double getAccountBalance(SyncRequestClient clientFutures) {
        AccountInformation accountInformation = clientFutures.getAccountInformation();
        return accountInformation.getAvailableBalance().doubleValue();
    }

    @Override
    public List<LogDto> getLogsByGroupId(Long groupId) {
        List<LogDto> logs = new ArrayList<>();
        double openPrice = 0;
        OrderSide openOrderSide = OrderSide.BUY;
        for (Log log : logRepository.findAllByGroupId(groupId)) {
            if (OrderStatus.OPEN.equals(log.getOrderStatus())) {
                openPrice = log.getPrice();
                openOrderSide = log.getOrderSide();
            }
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
                    .changePercents(OrderSide.BUY.equals(openOrderSide) ? log.getPrice() / openPrice : openPrice / log.getPrice())
                    .build();
            logs.add(logDto);
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
        updateFunding();
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
