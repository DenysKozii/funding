package bot.service.impl;

import bot.service.Connection;
import bot.service.Trade;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.AccountInformation;
import com.binance.client.model.trade.Position;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TradeImpl implements Trade {

    String websocketUrl;

    Double tradePercentage;

    Integer leverage;
    @NonFinal
    String symbol;
    @NonFinal
    Double rate;
    @NonFinal
    Double price;
    @NonFinal
    OrderSide orderSide;
    @NonFinal
    boolean closed;
    Connection connection;

    public TradeImpl(@Value("${websocket.url}") String websocketUrl,
                     @Value("${trade.percentage}") Double tradePercentage,
                     @Value("${leverage}") Integer leverage,
                     @Value("${symbol.default}") String symbol,
                     Connection connection) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.leverage = leverage;
        this.symbol = symbol;
        this.connection = connection;
    }

    @Override
    @SneakyThrows
    public void open() {
        SyncRequestClient clientFutures = connection.getClientFutures();
        updateFunding();
        if (Math.abs(rate) < 0.001) {
            log.info("rate is lower than limit {}", 0.001);
            return;
        }

        clientFutures.changeMarginType(symbol, MarginType.ISOLATED);
        clientFutures.changeInitialLeverage(symbol, leverage);

        double quantity = availableQuantity();
        quantity *= 2;
        quantity /= price;
        String positionQuantity = String.valueOf((int) quantity);
        log.info("position quantity = {}", positionQuantity);
        if (rate < 0) {
            orderSide = OrderSide.BUY;
            sendOrder(positionQuantity);
        } else {
            orderSide = OrderSide.SELL;
            sendOrder(positionQuantity);
        }
        closed = false;
    }

    @Override
    public void close() {
        if (closed) {
            log.info("close for {} is ignored", symbol);
            return;
        }
        SyncRequestClient clientFutures = connection.getClientFutures();

        Optional<Position> position = clientFutures.getAccountInformation().getPositions()
                .stream().filter(o -> o.getSymbol().equals(symbol)).findFirst();

        if (position.isEmpty() || position.get().getPositionAmt().doubleValue() == 0) {
            log.info("position {} is closed", symbol);
            closed = true;
            return;
        }

        String positionQuantity = position.get().getPositionAmt().toString();
        if (OrderSide.BUY.equals(orderSide)) {
            orderSide = OrderSide.SELL;
            sendOrder(positionQuantity);
        } else {
            orderSide = OrderSide.BUY;
            positionQuantity = String.valueOf(-1 * Double.parseDouble(positionQuantity));
            sendOrder(positionQuantity);
        }
    }

    @Override
    public void sendOrder(String positionQuantity) {
        connection.getClientFutures().postOrder(
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
    public double availableQuantity() {
        AccountInformation accountInformation = connection.getClientFutures().getAccountInformation();
        double quantity = accountInformation.getAvailableBalance().doubleValue();
        return Math.max(quantity * tradePercentage, 0);
    }
}
