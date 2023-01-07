package bot.binance;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Leverage {
    private BigDecimal leverage;
    private Double maxNotionalValue;
    private String symbol;

    public Leverage() {
    }

    public BigDecimal getLeverage() {
        return this.leverage;
    }

    public void setLeverage(BigDecimal leverage) {
        this.leverage = leverage;
    }

    public Double getMaxNotionalValue() {
        return this.maxNotionalValue;
    }

    public void setMaxNotionalValue(Double maxNotionalValue) {
        this.maxNotionalValue = maxNotionalValue;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String toString() {
        return (new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)).append("leverage", this.leverage).append("maxNotionalValue", this.maxNotionalValue).append("symbol", this.symbol).toString();
    }
}

