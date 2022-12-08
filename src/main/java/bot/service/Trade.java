package bot.service;

import bot.dto.LogDto;
import bot.dto.LogPreviewDto;
import bot.dto.OrderStatus;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.OrderType;
import com.binance.client.model.enums.TimeInForce;

import java.util.List;

public interface Trade {

    void open(SyncRequestClient clientFutures);

    void logOrder(OrderStatus orderStatus, Double accountBalance);

    void close(OrderType orderType, TimeInForce timeInForce, SyncRequestClient clientFutures);

    void sendOrder(String positionQuantity, OrderType orderType, TimeInForce timeInForce, SyncRequestClient clientFutures);

    void updateFunding();

    double getAccountBalance(SyncRequestClient clientFutures);

    List<LogDto> getLogsByGroupId(Long groupId);

    List<LogPreviewDto> getLogPreviews();
}
