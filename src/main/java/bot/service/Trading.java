package bot.service;

import bot.dto.ProfitLevel;
import bot.binance.SyncRequestClient;

import java.util.List;

public interface Trading {

    void open(SyncRequestClient client);

    void close(SyncRequestClient client);

    void closeLimit(SyncRequestClient client);

    void sendMarketOrder(SyncRequestClient client);

    boolean sendLimitOrder(SyncRequestClient client, int round, ProfitLevel profitLevel);

    void updateFunding();

    List<String> getFunding();

    double getAccountBalance(SyncRequestClient client);

    void reconnectSocket();

}
