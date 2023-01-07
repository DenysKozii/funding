package bot.binance;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class RestApiInvoker {
    private static final Logger log = LoggerFactory.getLogger(RestApiInvoker.class);
    private static final OkHttpClient client = new OkHttpClient();

    RestApiInvoker() {
    }

    static void checkResponse(JsonWrapper json) {
        try {
            String err_code;
            if (json.containKey("success")) {
                boolean success = json.getBoolean("success");
                if (!success) {
                    err_code = json.getStringOrDefault("code", "");
                    String err_msg = json.getStringOrDefault("msg", "");
                    if ("".equals(err_code)) {
                        throw new BinanceApiException("ExecuteError", "[Executing] " + err_msg);
                    }

                    throw new BinanceApiException("ExecuteError", "[Executing] " + err_code + ": " + err_msg);
                }
            } else if (json.containKey("code")) {
                int code = json.getInteger("code");
                if (code != 200) {
                    err_code = json.getStringOrDefault("msg", "");
                    throw new BinanceApiException("ExecuteError", "[Executing] " + code + ": " + err_code);
                }
            }

        } catch (BinanceApiException var4) {
            throw var4;
        } catch (Exception var5) {
            throw new BinanceApiException("RuntimeError", "[Invoking] Unexpected error: " + var5.getMessage());
        }
    }

    static <T> T callSync(RestApiRequest<T> request) {
        try {
            log.debug("Request URL " + request.request.url());
            Response response = client.newCall(request.request).execute();
            if (response != null && response.body() != null) {
                String str = response.body().string();
                response.close();
                log.debug("Response =====> " + str);
                JsonWrapper jsonWrapper = JsonWrapper.parseFromString(str);
                checkResponse(jsonWrapper);
                return request.jsonParser.parseJson(jsonWrapper);
            } else {
                throw new BinanceApiException("EnvironmentError", "[Invoking] Cannot get the response from server");
            }
        } catch (BinanceApiException var4) {
            throw var4;
        } catch (Exception var5) {
            throw new BinanceApiException("EnvironmentError", "[Invoking] Unexpected error: " + var5.getMessage());
        }
    }

    static WebSocket createWebSocket(Request request, WebSocketListener listener) {
        return client.newWebSocket(request, listener);
    }
}
