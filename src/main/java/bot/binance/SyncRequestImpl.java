package bot.binance;

public class SyncRequestImpl implements SyncRequestClient {
    private final RestApiRequestImpl requestImpl;

    SyncRequestImpl(RestApiRequestImpl requestImpl) {
        this.requestImpl = requestImpl;
    }

    public Order postOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType orderType, TimeInForce timeInForce, String quantity, String price, String reduceOnly, String newClientOrderId, String stopPrice, String closePosition, String activationPrice, String callbackRate, WorkingType workingType, String priceProtect, NewOrderRespType newOrderRespType) {
        return (Order) RestApiInvoker.callSync(this.requestImpl.postOrder(symbol, side, positionSide, orderType, timeInForce, quantity, reduceOnly, price, newClientOrderId, stopPrice, closePosition, activationPrice, callbackRate, workingType, priceProtect, newOrderRespType));
    }

    public ResponseResult cancelAllOpenOrder(String symbol) {
        return (ResponseResult) RestApiInvoker.callSync(this.requestImpl.cancelAllOpenOrder(symbol));
    }

    public ResponseResult changeMarginType(String symbolName, MarginType marginType) {
        return (ResponseResult) RestApiInvoker.callSync(this.requestImpl.changeMarginType(symbolName, marginType));
    }

    public AccountInformation getAccountInformation() {
        return (AccountInformation) RestApiInvoker.callSync(this.requestImpl.getAccountInformation());
    }

    public Leverage changeInitialLeverage(String symbol, Integer leverage) {
        return (Leverage) RestApiInvoker.callSync(this.requestImpl.changeInitialLeverage(symbol, leverage));
    }

}

