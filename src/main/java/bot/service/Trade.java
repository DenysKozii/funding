package bot.service;

import bot.dto.SettingsDto;
import com.binance.client.SyncRequestClient;

public interface Trade {

    void open(SyncRequestClient clientFutures);

    void close(SyncRequestClient clientFutures);

    void closeLimit(SyncRequestClient clientFutures);

    void sendMarketOrder(SyncRequestClient clientFutures);

    boolean sendLimitOrder(SyncRequestClient clientFutures, int round);

    void updateFunding();

    void logFunding();

    double getAccountBalance(SyncRequestClient clientFutures);

    void updateSettings(SettingsDto settings);
}
