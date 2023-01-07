package bot.binance;

@FunctionalInterface
public interface RestApiJsonParser<T> {
    T parseJson(JsonWrapper var1);
}

