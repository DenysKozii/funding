package bot.util;

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

    @Scheduled(fixedRate = 1000 * 60)
    public void timer() throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpUriRequest request = new HttpGet(herokuUrl);
            client.execute(request);
        }
        trade.logParameters();
    }

    private void open() {
        trade.updateFunding();
        log.info("open started");
        connection.getClientFutures().forEach(trade::open);
        log.info("open finished");
    }

    private void close() {
        log.info("close market started");
        connection.getClientFutures().forEach(trade::close);
        log.info("close market finished");
    }

    private void closeLimit() {
        log.info("close limit started");
        connection.getClientFutures().forEach(trade::closeLimit);
        log.info("close limit finished");
    }

    @Scheduled(cron = "0 59 23 * * *", zone = "GMT+0")
    public void reconnect0() {
        trade.reconnectSocket();
    }

    @Scheduled(cron = "57 59 23 * * *", zone = "GMT+0")
    public void open0() {
        open();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "GMT+0")
    public void close0() {
        closeLimit();
    }

    @Scheduled(cron = "10 0 0 * * *", zone = "GMT+0")
    public void close010() {
        close();
    }

    @Scheduled(cron = "15 0 0 * * *", zone = "GMT+0")
    public void close015() {
        close();
    }

    @Scheduled(cron = "30 0 0 * * *", zone = "GMT+0")
    public void close030() {
        close();
    }

    @Scheduled(cron = "59 0 0 * * *", zone = "GMT+0")
    public void close059() {
        close();
    }

    @Scheduled(cron = "0 59 7 * * *", zone = "GMT+0")
    public void reconnect8() {
        trade.reconnectSocket();
    }

    @Scheduled(cron = "57 59 7 * * *", zone = "GMT+0")
    public void open8() {
        open();
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "GMT+0")
    public void close8() {
        closeLimit();
    }

    @Scheduled(cron = "10 0 8 * * *", zone = "GMT+0")
    public void close810() {
        close();
    }

    @Scheduled(cron = "15 0 8 * * *", zone = "GMT+0")
    public void close815() {
        close();
    }

    @Scheduled(cron = "30 0 8 * * *", zone = "GMT+0")
    public void close830() {
        close();
    }

    @Scheduled(cron = "59 0 8 * * *", zone = "GMT+0")
    public void close859() {
        close();
    }

    @Scheduled(cron = "0 59 15 * * *", zone = "GMT+0")
    public void reconnect16() {
        trade.reconnectSocket();
    }

    @Scheduled(cron = "57 59 15 * * *", zone = "GMT+0")
    public void open16() {
        open();
    }

    @Scheduled(cron = "0 0 16 * * *", zone = "GMT+0")
    public void close16() {
        closeLimit();
    }

    @Scheduled(cron = "10 0 16 * * *", zone = "GMT+0")
    public void close1610() {
        close();
    }

    @Scheduled(cron = "15 0 16 * * *", zone = "GMT+0")
    public void close1615() {
        close();
    }

    @Scheduled(cron = "30 0 16 * * *", zone = "GMT+0")
    public void close1630() {
        close();
    }

    @Scheduled(cron = "59 0 16 * * *", zone = "GMT+0")
    public void close1659() {
        close();
    }
}
