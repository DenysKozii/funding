package bot.binance;

public interface SyncRequestClient {
    static SyncRequestClient create() {
        return create("", "", new RequestOptions());
    }

    static SyncRequestClient create(String apiKey, String secretKey) {
        return BinanceApiInternalFactory.getInstance().createSyncRequestClient(apiKey, secretKey, new RequestOptions());
    }

    static SyncRequestClient create(String apiKey, String secretKey, RequestOptions options) {
        return BinanceApiInternalFactory.getInstance().createSyncRequestClient(apiKey, secretKey, options);
    }

    Order postOrder(String var1, OrderSide var2, PositionSide var3, OrderType var4, TimeInForce var5, String var6, String var7, String var8, String var9, String var10, String var11, String var12, String var13, WorkingType var14, String var15, NewOrderRespType var16);

    ResponseResult cancelAllOpenOrder(String var1);

    ResponseResult changeMarginType(String var1, MarginType var2);

    AccountInformation getAccountInformation();

    Leverage changeInitialLeverage(String var1, Integer var2);

}
