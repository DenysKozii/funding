package bot.binance;

import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

class RestApiRequestImpl {
    private static final Logger log = LoggerFactory.getLogger(RestApiRequestImpl.class);
    private String apiKey;
    private String secretKey;
    private String serverUrl;

    RestApiRequestImpl(String apiKey, String secretKey, RequestOptions options) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.serverUrl = options.getUrl();
    }

    private Request createRequestByGet(String address, UrlParamsBuilder builder) {
        log.debug("Request URL " + this.serverUrl);
        return this.createRequestByGet(this.serverUrl, address, builder);
    }

    private Request createRequestByGet(String url, String address, UrlParamsBuilder builder) {
        return this.createRequest(url, address, builder);
    }


    private Request createRequest(String url, String address, UrlParamsBuilder builder) {
        String requestUrl = url + address;
        log.debug("Request URL " + requestUrl);
        if (builder != null) {
            return builder.hasPostParam() ? (new Request.Builder()).url(requestUrl).post(builder.buildPostBody()).addHeader("Content-Type", "application/json").addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build() : (new Request.Builder()).url(requestUrl + builder.buildUrl()).addHeader("Content-Type", "application/x-www-form-urlencoded").addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build();
        } else {
            return (new Request.Builder()).url(requestUrl).addHeader("Content-Type", "application/x-www-form-urlencoded").addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build();
        }
    }

    RestApiRequest<Order> postOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType orderType, TimeInForce timeInForce, String quantity, String reduceOnly, String price, String newClientOrderId, String stopPrice, String closePosition, String activationPrice, String callBackRate, WorkingType workingType, String priceProtect, NewOrderRespType newOrderRespType) {
        RestApiRequest<Order> request = new RestApiRequest();
        UrlParamsBuilder builder = UrlParamsBuilder.build().putToUrl("symbol", symbol).putToUrl("side", side).putToUrl("positionSide", positionSide).putToUrl("type", orderType).putToUrl("timeInForce", timeInForce).putToUrl("quantity", quantity).putToUrl("price", price).putToUrl("reduceOnly", reduceOnly).putToUrl("newClientOrderId", newClientOrderId).putToUrl("stopPrice", stopPrice).putToUrl("closePosition", closePosition).putToUrl("activationPrice", activationPrice).putToUrl("callBackRate", callBackRate).putToUrl("workingType", workingType).putToUrl("priceProtect", priceProtect).putToUrl("newOrderRespType", newOrderRespType);
        request.request = this.createRequestByPostWithSignature("/fapi/v1/order", builder);
        request.jsonParser = (jsonWrapper) -> {
            Order result = new Order();
            result.setClientOrderId(jsonWrapper.getString("clientOrderId"));
            result.setCumQuote(jsonWrapper.getBigDecimal("cumQuote"));
            result.setExecutedQty(jsonWrapper.getBigDecimal("executedQty"));
            result.setOrderId(jsonWrapper.getLong("orderId"));
            result.setAvgPrice(jsonWrapper.getBigDecimal("avgPrice"));
            result.setOrigQty(jsonWrapper.getBigDecimal("origQty"));
            result.setPrice(jsonWrapper.getBigDecimal("price"));
            result.setReduceOnly(jsonWrapper.getBoolean("reduceOnly"));
            result.setSide(jsonWrapper.getString("side"));
            result.setPositionSide(jsonWrapper.getString("positionSide"));
            result.setStatus(jsonWrapper.getString("status"));
            result.setStopPrice(jsonWrapper.getBigDecimal("stopPrice"));
            result.setClosePosition(jsonWrapper.getBoolean("closePosition"));
            result.setSymbol(jsonWrapper.getString("symbol"));
            result.setTimeInForce(jsonWrapper.getString("timeInForce"));
            result.setType(jsonWrapper.getString("type"));
            result.setOrigType(jsonWrapper.getString("origType"));
            if (jsonWrapper.containKey("activatePrice")) {
                result.setActivatePrice(jsonWrapper.getBigDecimal("activatePrice"));
            }

            if (jsonWrapper.containKey("priceRate")) {
                result.setActivatePrice(jsonWrapper.getBigDecimal("priceRate"));
            }

            result.setUpdateTime(jsonWrapper.getLong("updateTime"));
            result.setWorkingType(jsonWrapper.getString("workingType"));
            result.setPriceProtect(jsonWrapper.getBoolean("priceProtect"));
            return result;
        };
        return request;
    }

    private Request createRequestByPostWithSignature(String address, UrlParamsBuilder builder) {
        return this.createRequestWithSignature(this.serverUrl, address, builder.setMethod("POST"));
    }

    private Request createRequestByGetWithSignature(String address, UrlParamsBuilder builder) {
        return this.createRequestWithSignature(this.serverUrl, address, builder);
    }

    private Request createRequestByPutWithSignature(String address, UrlParamsBuilder builder) {
        return this.createRequestWithSignature(this.serverUrl, address, builder.setMethod("PUT"));
    }

    private Request createRequestByDeleteWithSignature(String address, UrlParamsBuilder builder) {
        return this.createRequestWithSignature(this.serverUrl, address, builder.setMethod("DELETE"));
    }

    private Request createRequestWithSignature(String url, String address, UrlParamsBuilder builder) {
        if (builder == null) {
            throw new BinanceApiException("RuntimeError", "[Invoking] Builder is null when create request with Signature");
        } else {
            String requestUrl = url + address;
            (new ApiSignature()).createSignature(this.apiKey, this.secretKey, builder);
            if (builder.hasPostParam()) {
                requestUrl = requestUrl + builder.buildUrl();
                return (new Request.Builder()).url(requestUrl).post(builder.buildPostBody()).addHeader("Content-Type", "application/json").addHeader("X-MBX-APIKEY", this.apiKey).addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build();
            } else if (builder.checkMethod("PUT")) {
                requestUrl = requestUrl + builder.buildUrl();
                return (new Request.Builder()).url(requestUrl).put(builder.buildPostBody()).addHeader("Content-Type", "application/x-www-form-urlencoded").addHeader("X-MBX-APIKEY", this.apiKey).addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build();
            } else if (builder.checkMethod("DELETE")) {
                requestUrl = requestUrl + builder.buildUrl();
                return (new Request.Builder()).url(requestUrl).delete().addHeader("Content-Type", "application/x-www-form-urlencoded").addHeader("client_SDK_Version", "binance_futures-1.0.1-java").addHeader("X-MBX-APIKEY", this.apiKey).build();
            } else {
                requestUrl = requestUrl + builder.buildUrl();
                return (new Request.Builder()).url(requestUrl).addHeader("Content-Type", "application/x-www-form-urlencoded").addHeader("client_SDK_Version", "binance_futures-1.0.1-java").addHeader("X-MBX-APIKEY", this.apiKey).build();
            }
        }
    }

    RestApiRequest<ResponseResult> cancelAllOpenOrder(String symbol) {
        RestApiRequest<ResponseResult> request = new RestApiRequest();
        UrlParamsBuilder builder = UrlParamsBuilder.build().putToUrl("symbol", symbol);
        request.request = this.createRequestByDeleteWithSignature("/fapi/v1/allOpenOrders", builder);
        request.jsonParser = (jsonWrapper) -> {
            ResponseResult responseResult = new ResponseResult();
            responseResult.setCode(jsonWrapper.getInteger("code"));
            responseResult.setMsg(jsonWrapper.getString("msg"));
            return responseResult;
        };
        return request;
    }

    RestApiRequest<ResponseResult> changeMarginType(String symbolName, MarginType marginType) {
        RestApiRequest<ResponseResult> request = new RestApiRequest();
        UrlParamsBuilder builder = UrlParamsBuilder.build().putToUrl("symbol", symbolName).putToUrl("marginType", marginType);
        request.request = this.createRequestByPostWithSignature("/fapi/v1/marginType", builder);
        request.jsonParser = (jsonWrapper) -> {
            ResponseResult result = new ResponseResult();
            result.setCode(jsonWrapper.getInteger("code"));
            result.setMsg(jsonWrapper.getString("msg"));
            return result;
        };
        return request;
    }

    RestApiRequest<AccountInformation> getAccountInformation() {
        RestApiRequest<AccountInformation> request = new RestApiRequest();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        request.request = this.createRequestByGetWithSignature("/fapi/v2/account", builder);
        request.jsonParser = (jsonWrapper) -> {
            AccountInformation result = new AccountInformation();
            result.setFeeTier(jsonWrapper.getBigDecimal("feeTier"));
            result.setCanTrade(jsonWrapper.getBoolean("canTrade"));
            result.setCanDeposit(jsonWrapper.getBoolean("canDeposit"));
            result.setCanWithdraw(jsonWrapper.getBoolean("canWithdraw"));
            result.setUpdateTime(jsonWrapper.getLong("updateTime"));
            result.setTotalInitialMargin(jsonWrapper.getBigDecimal("totalInitialMargin"));
            result.setTotalMaintMargin(jsonWrapper.getBigDecimal("totalMaintMargin"));
            result.setTotalWalletBalance(jsonWrapper.getBigDecimal("totalWalletBalance"));
            result.setTotalUnrealizedProfit(jsonWrapper.getBigDecimal("totalUnrealizedProfit"));
            result.setTotalMarginBalance(jsonWrapper.getBigDecimal("totalMarginBalance"));
            result.setTotalPositionInitialMargin(jsonWrapper.getBigDecimal("totalPositionInitialMargin"));
            result.setTotalOpenOrderInitialMargin(jsonWrapper.getBigDecimal("totalOpenOrderInitialMargin"));
            result.setTotalCrossWalletBalance(jsonWrapper.getBigDecimal("totalCrossWalletBalance"));
            result.setTotalCrossUnPnl(jsonWrapper.getBigDecimal("totalCrossUnPnl"));
            result.setAvailableBalance(jsonWrapper.getBigDecimal("availableBalance"));
            result.setMaxWithdrawAmount(jsonWrapper.getBigDecimal("maxWithdrawAmount"));
            List<Asset> assetList = new LinkedList();
            JsonWrapperArray assetArray = jsonWrapper.getJsonArray("assets");
            assetArray.forEach((item) -> {
                Asset element = new Asset();
                element.setAsset(item.getString("asset"));
                element.setWalletBalance(item.getBigDecimal("walletBalance"));
                element.setUnrealizedProfit(item.getBigDecimal("unrealizedProfit"));
                element.setMarginBalance(item.getBigDecimal("marginBalance"));
                element.setMaintMargin(item.getBigDecimal("maintMargin"));
                element.setInitialMargin(item.getBigDecimal("initialMargin"));
                element.setPositionInitialMargin(item.getBigDecimal("positionInitialMargin"));
                element.setOpenOrderInitialMargin(item.getBigDecimal("openOrderInitialMargin"));
                element.setCrossWalletBalance(item.getBigDecimal("crossWalletBalance"));
                element.setCrossUnPnl(item.getBigDecimal("crossUnPnl"));
                element.setAvailableBalance(item.getBigDecimal("availableBalance"));
                element.setMaxWithdrawAmount(item.getBigDecimal("maxWithdrawAmount"));
                assetList.add(element);
            });
            result.setAssets(assetList);
            List<Position> positionList = new LinkedList();
            JsonWrapperArray positionArray = jsonWrapper.getJsonArray("positions");
            positionArray.forEach((item) -> {
                Position element = new Position();
                element.setSymbol(item.getString("symbol"));
                element.setInitialMargin(item.getBigDecimal("initialMargin"));
                element.setMaintMargin(item.getBigDecimal("maintMargin"));
                element.setUnrealizedProfit(item.getBigDecimal("unrealizedProfit"));
                element.setPositionInitialMargin(item.getBigDecimal("positionInitialMargin"));
                element.setOpenOrderInitialMargin(item.getBigDecimal("openOrderInitialMargin"));
                element.setLeverage(item.getBigDecimal("leverage"));
                element.setIsolated(item.getBoolean("isolated"));
                element.setEntryPrice(item.getString("entryPrice"));
                element.setMaxNotional(item.getString("maxNotional"));
                element.setPositionSide(item.getString("positionSide"));
                element.setPositionAmt(item.getBigDecimal("positionAmt"));
                positionList.add(element);
            });
            result.setPositions(positionList);
            return result;
        };
        return request;
    }


    RestApiRequest<Leverage> changeInitialLeverage(String symbol, Integer leverage) {
        RestApiRequest<Leverage> request = new RestApiRequest();
        UrlParamsBuilder builder = UrlParamsBuilder.build().putToUrl("symbol", symbol).putToUrl("leverage", leverage);
        request.request = this.createRequestByPostWithSignature("/fapi/v1/leverage", builder);
        request.jsonParser = (jsonWrapper) -> {
            Leverage result = new Leverage();
            result.setLeverage(jsonWrapper.getBigDecimal("leverage"));
            if (jsonWrapper.getString("maxNotionalValue").equalsIgnoreCase("INF")) {
                result.setMaxNotionalValue(Double.POSITIVE_INFINITY);
            } else {
                result.setMaxNotionalValue(jsonWrapper.getDouble("maxNotionalValue"));
            }

            result.setSymbol(jsonWrapper.getString("symbol"));
            return result;
        };
        return request;
    }

}
