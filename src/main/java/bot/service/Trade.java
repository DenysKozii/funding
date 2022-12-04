package bot.service;

import com.binance.client.SyncRequestClient;

public interface Trade {

    void open(SyncRequestClient clientFutures);

    void close(SyncRequestClient clientFutures);

    void sendOrder(String positionQuantity, SyncRequestClient clientFutures);

    void updateFunding();

    double availableQuantity(SyncRequestClient clientFutures);

}
