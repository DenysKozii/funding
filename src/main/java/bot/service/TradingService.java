package bot.service;

import bot.dto.FundingDto;
import bot.dto.ProfitLevel;
import bot.binance.SyncRequestClient;
import bot.dto.TradeDto;

import java.util.List;

public interface TradingService {

    void open(SyncRequestClient client);

    void close(SyncRequestClient client);

    void closeLimit(SyncRequestClient client);

    void sendMarketOrder(SyncRequestClient client);

    boolean sendLimitOrder(SyncRequestClient client, int round, ProfitLevel profitLevel);

    void updateFunding();

    List<String> getFunding();

    double getAccountBalance(SyncRequestClient client);

    void reconnectSocket();

    List<TradeDto> getTrades();

    List<FundingDto> getFundings();

}
