package bot.service.impl;

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

    public TradeImpl(@Value("${websocket.url}") String websocketUrl,
                     @Value("${trade.percentage}") Double tradePercentage,
                     @Value("${leverage}") Integer leverage,
                     @Value("${symbol.default}") String symbol) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.leverage = leverage;
        this.symbol = symbol;
    }

    @Override
    @SneakyThrows
    public void open(SyncRequestClient clientFutures) {
        updateFunding();
        if (Math.abs(rate) < 0.001) {
            log.info("rate is lower than limit {}", 0.001);
            return;
        }

        try {
            clientFutures.changeMarginType(symbol, MarginType.ISOLATED);
        } catch (Exception ignored) {}
        clientFutures.changeInitialLeverage(symbol, leverage);

        double quantity = availableQuantity(clientFutures);
        quantity *= 2;
        quantity /= price;
        String positionQuantity = (int) quantity > 0 ? String.valueOf((int) quantity) : String.format("%.2f", quantity);
        log.info("position quantity = {}", positionQuantity);
        if (rate < 0) {
            orderSide = OrderSide.BUY;
            sendOrder(positionQuantity, clientFutures);
        } else {
            orderSide = OrderSide.SELL;
            sendOrder(positionQuantity, clientFutures);
        }
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
    public double availableQuantity(SyncRequestClient clientFutures) {
        AccountInformation accountInformation = clientFutures.getAccountInformation();
        double quantity = accountInformation.getAvailableBalance().doubleValue();
        return Math.max(quantity * tradePercentage, 0);
    }
}
