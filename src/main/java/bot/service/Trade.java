package bot.service;

import bot.dto.ProfitLevel;
import bot.binance.SyncRequestClient;

import java.util.List;

public interface Trade {

    void open(SyncRequestClient clientFutures);

    void close(SyncRequestClient clientFutures);

    void closeLimit(SyncRequestClient clientFutures);

    void sendMarketOrder(SyncRequestClient clientFutures);

    boolean sendLimitOrder(SyncRequestClient clientFutures, int round, ProfitLevel profitLevel);

    void updateFunding();

    List<String> getFunding();

    double getAccountBalance(SyncRequestClient clientFutures);

    void reconnectSocket();

}
