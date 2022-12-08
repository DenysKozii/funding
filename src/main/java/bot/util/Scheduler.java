package bot.util;

import bot.service.Connection;
import bot.service.Trade;
import com.binance.client.model.enums.OrderType;
import com.binance.client.model.enums.TimeInForce;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Scheduler {

    String herokuUrl;
    Trade trade;
    Connection connection;

    @Autowired
    public Scheduler(@Value("${heroku.url}") String herokuUrl, Trade trade, Connection connection) {
        this.herokuUrl = herokuUrl;
        this.trade = trade;
        this.connection = connection;
    }

    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void timer() throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpUriRequest request = new HttpGet(herokuUrl);
            client.execute(request);
        }
        trade.updateFunding();
    }

    private void open() {
        log.info("open started");
        connection.getClientFutures().forEach(trade::open);
        log.info("open finished");
    }

    private void close(OrderType orderType, TimeInForce timeInForce) {
        log.info("close started");
        connection.getClientFutures().forEach(client -> trade.close(orderType, timeInForce, client));
        log.info("close finished");
    }

    @Scheduled(cron = "59 12 21 * * *", zone = "GMT+0")
    public void open0() {
        open();
    }

    @Scheduled(cron = "1 13 21 * * *", zone = "GMT+0")
    public void close0() {
        close(OrderType.LIMIT, TimeInForce.GTC);
    }

    @Scheduled(cron = "7 13 21 * * *", zone = "GMT+0")
    public void close06() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "10 0 0 * * *", zone = "GMT+0")
    public void close010() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "15 0 0 * * *", zone = "GMT+0")
    public void close015() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "30 0 0 * * *", zone = "GMT+0")
    public void close030() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "59 0 0 * * *", zone = "GMT+0")
    public void close059() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "59 59 7 * * *", zone = "GMT+0")
    public void open8() {
        open();
    }

    @Scheduled(cron = "1 0 8 * * *", zone = "GMT+0")
    public void close8() {
        close(OrderType.LIMIT, TimeInForce.GTC);
    }

    @Scheduled(cron = "7 0 8 * * *", zone = "GMT+0")
    public void close87() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "10 0 8 * * *", zone = "GMT+0")
    public void close810() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "15 0 8 * * *", zone = "GMT+0")
    public void close815() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "30 0 8 * * *", zone = "GMT+0")
    public void close830() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "59 0 8 * * *", zone = "GMT+0")
    public void close859() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "59 59 15 * * *", zone = "GMT+0")
    public void open16() {
        open();
    }

    @Scheduled(cron = "1 0 16 * * *", zone = "GMT+0")
    public void close16() {
        close(OrderType.LIMIT, TimeInForce.GTC);
    }

    @Scheduled(cron = "7 0 16 * * *", zone = "GMT+0")
    public void close167() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "10 0 16 * * *", zone = "GMT+0")
    public void close1610() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "15 0 16 * * *", zone = "GMT+0")
    public void close1615() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "30 0 16 * * *", zone = "GMT+0")
    public void close1630() {
        close(OrderType.MARKET, null);
    }

    @Scheduled(cron = "59 0 16 * * *", zone = "GMT+0")
    public void close1659() {
        close(OrderType.MARKET, null);
    }
}
