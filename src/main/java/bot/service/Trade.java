package bot.service;

import bot.dto.SettingsDto;
import bot.binance.SyncRequestClient;

import java.util.List;

public interface Trade {

    void open(SyncRequestClient clientFutures);

    void close(SyncRequestClient clientFutures);

    void closeLimit(SyncRequestClient clientFutures);

    void sendMarketOrder(SyncRequestClient clientFutures);

    boolean sendLimitOrder(SyncRequestClient clientFutures, int round);

    void updateFunding();

    void logParameters();

    List<String> getFunding();

    double getAccountBalance(SyncRequestClient clientFutures);

    void updateSettings(SettingsDto settings);
}
