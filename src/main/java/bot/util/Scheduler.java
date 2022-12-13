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

    private void close() {
        log.info("close started");
        connection.getClientFutures().forEach(trade::close);
        log.info("close finished");
    }

    @Scheduled(cron = "58 59 23 * * *", zone = "GMT+0")
    public void open0() {
        open();
    }

    @Scheduled(cron = "5 0 0 * * *", zone = "GMT+0")
    public void close05() {
        close();
    }

    @Scheduled(cron = "8 0 0 * * *", zone = "GMT+0")
    public void close08() {
        close();
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

    @Scheduled(cron = "58 59 7 * * *", zone = "GMT+0")
    public void open8() {
        open();
    }

    @Scheduled(cron = "5 0 8 * * *", zone = "GMT+0")
    public void close85() {
        close();
    }

    @Scheduled(cron = "8 0 8 * * *", zone = "GMT+0")
    public void close88() {
        close();
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

    @Scheduled(cron = "58 59 15 * * *", zone = "GMT+0")
    public void open16() {
        open();
    }

    @Scheduled(cron = "5 0 16 * * *", zone = "GMT+0")
    public void close165() {
        close();
    }

    @Scheduled(cron = "8 0 16 * * *", zone = "GMT+0")
    public void close168() {
        close();
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
