package bot.service;

import bot.dto.LogDto;
import com.binance.client.SyncRequestClient;

import java.util.List;

public interface Trade {

    void open(SyncRequestClient clientFutures);

    void close(SyncRequestClient clientFutures);

    void sendOrder(String positionQuantity, SyncRequestClient clientFutures);

    void updateFunding();

    double availableQuantity(SyncRequestClient clientFutures);

    List<LogDto> getLogs();

}
