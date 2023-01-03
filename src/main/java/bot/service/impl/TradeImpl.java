package bot.service.impl;

import bot.dto.ProfitLevel;
import bot.dto.SettingsDto;
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
import java.util.Optional;

@Data
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TradeImpl implements Trade {

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
                     @Value("${spliterator}") String spliterator) {
        this.websocketUrl = websocketUrl;
        this.tradePercentage = tradePercentage;
        this.fundingLimit = fundingLimit;
        this.profitLimit = profitLimit;
        this.leverage = leverage;
        this.symbol = symbol;
        this.roundStart = roundStart;
        this.dateFormatPattern = dateFormatPattern;
        this.spliterator = spliterator;
    }

    @Override
    @SneakyThrows
    public void open(SyncRequestClient clientFutures) {
        responsePrice = 0.0;
        if (Math.abs(rate) < fundingLimit) {
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
            if (absoluteRate < ProfitLevel.LOW.getFunding()) {
                profitLimit = ProfitLevel.LOW.getProfit();
            } else if (absoluteRate < ProfitLevel.MEDIUM.getFunding()) {
                profitLimit = ProfitLevel.MEDIUM.getProfit();
            } else if (absoluteRate < ProfitLevel.HIGH.getFunding()) {
                profitLimit = ProfitLevel.HIGH.getProfit();
            }
            while (!sendLimitOrder(clientFutures, round) && round > 0) {
                round--;
            }
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

}
