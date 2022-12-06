package bot.service.impl;

import bot.dto.OrderStatus;
import bot.entity.Log;
import bot.repository.LogRepository;
import bot.service.Trade;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.AccountInformation;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TradeImpl implements Trade {

    String websocketUrl;
    Double tradePercentage;
    Double tradeLimit;
    Integer leverage;
    @NonFinal
    String symbol;
    @NonFinal
    Double rate;
    @NonFinal
    Double price;
    @NonFinal
    OrderSide orderSide;

    LogRepository logRepository;

    @Autowired
    public TradeImpl(@Value("${websocket.url}") String websocketUrl,
                     @Value("${trade.percentage}") Double tradePercentage,
                     @Value("${trade.limit}") Double tradeLimit,
                     @Value("${leverage}") Integer leverage,
                     @Value("${symbol.default}") String symbol,
                     LogRepository logRepository) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.tradeLimit = tradeLimit;
        this.leverage = leverage;
        this.symbol = symbol;
        this.logRepository = logRepository;
    }

    @Override
    @SneakyThrows
    public void open(SyncRequestClient clientFutures) {
        if (Math.abs(rate) < tradeLimit) {
            log.info("rate is lower than limit {}", tradeLimit);
            return;
        }

        try {
            clientFutures.changeMarginType(symbol, MarginType.ISOLATED);
        } catch (Exception ignored) {}
        clientFutures.changeInitialLeverage(symbol, leverage);
        double accountBalance = getAccountBalance(clientFutures);
        double quantity =  Math.max(accountBalance * tradePercentage, 0);
        quantity *= leverage;
        quantity /= price;
        String positionQuantity = (int) quantity > 0 ? String.valueOf((int) quantity) : String.format("%.1f", quantity);
        log.info("position quantity = {}", positionQuantity);
        if (rate > 0) {
            orderSide = OrderSide.BUY;
            sendOrder(positionQuantity, clientFutures);
        } else {
            orderSide = OrderSide.SELL;
            sendOrder(positionQuantity, clientFutures);
        }
        logOrder(OrderStatus.OPEN, accountBalance);
    }

    @Override
    public void logOrder(OrderStatus orderStatus, Double accountBalance) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSSSS");
        Date date = new Date();
        Log log = Log.builder()
                .date(dateFormat.format(date))
                .symbol(symbol)
                .rate(rate)
                .price(price)
                .orderStatus(orderStatus)
                .accountBalance(accountBalance)
                .build();
        logRepository.save(log);
    }

    @Override
    public void close(SyncRequestClient clientFutures) {
        Optional<Position> position = clientFutures.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();

        if (position.isEmpty() || position.get().getPositionAmt().doubleValue() == 0.0) {
            log.info("position {} is already closed", symbol);
            return;
        }

        String positionQuantity = position.get().getPositionAmt().toString();
        log.info("close {} position quantity = {}", symbol, positionQuantity);
        if (OrderSide.BUY.equals(orderSide)) {
            orderSide = OrderSide.SELL;
            sendOrder(positionQuantity, clientFutures);
        } else {
            orderSide = OrderSide.BUY;
            positionQuantity = String.valueOf(-1 * Double.parseDouble(positionQuantity));
            sendOrder(positionQuantity, clientFutures);
        }
        logOrder(OrderStatus.CLOSE, getAccountBalance(clientFutures));
    }

    @Override
    public void sendOrder(String positionQuantity, SyncRequestClient clientFutures) {
        clientFutures.postOrder(
                symbol, orderSide, PositionSide.BOTH, OrderType.MARKET, null, positionQuantity,
                null, null, null, null, null, null, null, null, null,
                NewOrderRespType.RESULT);
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
    public List<Log> getLogs() {
        return logRepository.findAll();
    }

}
