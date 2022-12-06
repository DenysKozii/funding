package bot.util;

import bot.dto.OrderStatus;
import bot.service.Connection;
import bot.service.Trade;
import lombok.AccessLevel;
import lombok.SneakyThrows;
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

    private void close(Integer number) {
        log.info("close {} started", number);
        connection.getClientFutures().forEach(trade::close);
        log.info("close {} finished", number);
    }

    @SneakyThrows
    @Scheduled(cron = "0 0 0 * * *", zone = "GMT+0")
    public void open0() {
        open();
    }

    @SneakyThrows
    @Scheduled(cron = "8 0 0 * * *", zone = "GMT+0")
    public void close0() {
        close(0);
    }

    @SneakyThrows
    @Scheduled(cron = "15 0 0 * * *", zone = "GMT+0")
    public void close01() {
        close(1);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @SneakyThrows
    @Scheduled(cron = "20 0 0 * * *", zone = "GMT+0")
    public void close02() {
        close(2);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @SneakyThrows
    @Scheduled(cron = "25 0 0 * * *", zone = "GMT+0")
    public void close03() {
        close(3);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @SneakyThrows
    @Scheduled(cron = "0 0 8 * * *", zone = "GMT+0")
    public void open8() {
        open();
    }

    @SneakyThrows
    @Scheduled(cron = "10 0 8 * * *", zone = "GMT+0")
    public void close8() {
        close(0);
    }

    @SneakyThrows
    @Scheduled(cron = "15 0 8 * * *", zone = "GMT+0")
    public void close81() {
        close(1);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @SneakyThrows
    @Scheduled(cron = "20 0 8 * * *", zone = "GMT+0")
    public void close82() {
        close(2);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @SneakyThrows
    @Scheduled(cron = "25 0 8 * * *", zone = "GMT+0")
    public void close83() {
        close(3);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @SneakyThrows
    @Scheduled(cron = "0 0 16 * * *", zone = "GMT+0")
    public void open16() {
        open();
    }

    @SneakyThrows
    @Scheduled(cron = "10 0 16 * * *", zone = "GMT+0")
    public void close16() {
        close(0);
    }

    @SneakyThrows
    @Scheduled(cron = "15 0 16 * * *", zone = "GMT+0")
    public void close161() {
        close(1);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @SneakyThrows
    @Scheduled(cron = "20 0 16 * * *", zone = "GMT+0")
    public void close162() {
        close(2);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }

    @SneakyThrows
    @Scheduled(cron = "25 0 16 * * *", zone = "GMT+0")
    public void close163() {
        close(3);
        trade.logOrder(OrderStatus.BUFFER_CLOSE, 0.0);
    }
}
