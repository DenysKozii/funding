package bot.util;

import bot.dto.OrderStatus;
import bot.service.Connection;
import bot.service.Trade;
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
        trade.updateFunding();
        log.info("open started");
        connection.getClientFutures().forEach(trade::open);
        log.info("open finished");
    }

    private void close() {
        log.info("close started");
        connection.getClientFutures().forEach(trade::close);
        log.info("close finished");
    }

    @Scheduled(cron = "58 59 23 * * *", zone = "GMT+0")
    public void open0() {
        open();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "GMT+0")
    public void close05() {
        close();
    }

    @Scheduled(cron = "6 0 0 * * *", zone = "GMT+0")
    public void close06() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "7 0 0 * * *", zone = "GMT+0")
    public void close07() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "8 0 0 * * *", zone = "GMT+0")
    public void close08() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "9 0 0 * * *", zone = "GMT+0")
    public void close09() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "10 0 0 * * *", zone = "GMT+0")
    public void close010() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "15 0 0 * * *", zone = "GMT+0")
    public void close015() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "30 0 0 * * *", zone = "GMT+0")
    public void close030() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "59 0 0 * * *", zone = "GMT+0")
    public void close059() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "58 59 7 * * *", zone = "GMT+0")
    public void open8() {
        open();
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "GMT+0")
    public void close8() {
        close();
    }

    @Scheduled(cron = "6 0 8 * * *", zone = "GMT+0")
    public void close86() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "7 0 8 * * *", zone = "GMT+0")
    public void close87() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "8 0 8 * * *", zone = "GMT+0")
    public void close88() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "9 0 8 * * *", zone = "GMT+0")
    public void close89() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "10 0 8 * * *", zone = "GMT+0")
    public void close810() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "15 0 8 * * *", zone = "GMT+0")
    public void close815() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "30 0 8 * * *", zone = "GMT+0")
    public void close830() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "59 0 8 * * *", zone = "GMT+0")
    public void close859() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "58 59 15 * * *", zone = "GMT+0")
    public void open16() {
        open();
    }

    @Scheduled(cron = "0 0 16 * * *", zone = "GMT+0")
    public void close16() {
        close();
    }

    @Scheduled(cron = "6 0 16 * * *", zone = "GMT+0")
    public void close166() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "7 0 16 * * *", zone = "GMT+0")
    public void close167() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "8 0 16 * * *", zone = "GMT+0")
    public void close168() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "9 0 16 * * *", zone = "GMT+0")
    public void close169() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "10 0 16 * * *", zone = "GMT+0")
    public void close1610() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "15 0 16 * * *", zone = "GMT+0")
    public void close1615() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "30 0 16 * * *", zone = "GMT+0")
    public void close1630() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @Scheduled(cron = "59 0 16 * * *", zone = "GMT+0")
    public void close1659() {
        close();
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }
}
