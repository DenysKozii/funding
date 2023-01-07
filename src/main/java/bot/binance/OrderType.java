package bot.binance;

public enum OrderType {
    LIMIT("LIMIT"),
    MARKET("MARKET"),
    STOP("STOP"),
    STOP_MARKET("STOP_MARKET"),
    TAKE_PROFIT("TAKE_PROFIT"),
    TAKE_PROFIT_MARKET("TAKE_PROFIT_MARKET"),
    TRAILING_STOP_MARKET("TAKE_PROFIT_MARKET");

    private final String code;

    private OrderType(String code) {
        this.code = code;
    }

    public String toString() {
        return this.code;
    }

}

