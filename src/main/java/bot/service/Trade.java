package bot.service;

import bot.dto.LogDto;
import bot.dto.OrderStatus;
import com.binance.client.SyncRequestClient;

import java.util.List;

public interface Trade {

    void open(SyncRequestClient clientFutures);

    void logOrder(OrderStatus orderStatus, Double accountBalance);

    void close(SyncRequestClient clientFutures);

    void sendOrder(String positionQuantity, SyncRequestClient clientFutures);

    void updateFunding();

    double getAccountBalance(SyncRequestClient clientFutures);

    List<LogDto> getLogs();

}
