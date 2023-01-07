package bot.binance;

import okhttp3.Request;

public class RestApiRequest<T> {
    public Request request;
    RestApiJsonParser<T> jsonParser;

    public RestApiRequest() {
    }
}