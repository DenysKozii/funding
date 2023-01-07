package bot.binance;

public enum PositionSide {
    BOTH("BOTH"),
    SHORT("SHORT"),
    LONG("LONG");

    private final String code;

    private PositionSide(String side) {
        this.code = side;
    }

    public String toString() {
        return this.code;
    }
}

