package bot.binance;

@FunctionalInterface
public interface Handler<T> {
    void handle(T var1);
}

