package bot.binance;

public enum OrderSide {
    BUY("BUY"),
    SELL("SELL");

    private final String code;

    private OrderSide(String side) {
        this.code = side;
    }

    public String toString() {
        return this.code;
    }
}
