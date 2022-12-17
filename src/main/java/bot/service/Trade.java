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

    void sendMarketOrder(String positionQuantity, SyncRequestClient clientFutures);

    void sendLimitOrder(String positionQuantity, String price, SyncRequestClient clientFutures);

    void updateFunding();

    void logFunding();

    double getAccountBalance(SyncRequestClient clientFutures);

    List<LogDto> getLogsByGroupId(Long groupId);

    List<LogPreviewDto> getLogPreviews();

    void setLeverage(Integer leverage);

    void setProfitLimit(Double profitLimit);
}
