package bot.service;

import bot.dto.LogDto;
import bot.dto.LogPreviewDto;
import bot.dto.OrderStatus;
import com.binance.client.SyncRequestClient;

import java.util.List;

public interface Trade {

    void open(SyncRequestClient clientFutures);

    void logOrder(OrderStatus orderStatus, Double accountBalance);

    void close(SyncRequestClient clientFutures);

    void closeLimit(SyncRequestClient clientFutures);

    void sendMarketOrder(SyncRequestClient clientFutures);

    boolean sendLimitOrder(SyncRequestClient clientFutures, int round);

    void updateFunding();

    void logFunding();

    double getAccountBalance(SyncRequestClient clientFutures);

    List<LogDto> getLogsByGroupId(Long groupId);

    List<LogPreviewDto> getLogPreviews();

    void setLeverage(Integer leverage);

    void setProfitLimit(Double profitLimit);

    void setTradeLimit(Double tradeLimit);
}
