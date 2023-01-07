package bot.binance;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Position {
    private String symbol;
    private BigDecimal initialMargin;
    private BigDecimal maintMargin;
    private BigDecimal unrealizedProfit;
    private BigDecimal positionInitialMargin;
    private BigDecimal openOrderInitialMargin;
    private BigDecimal leverage;
    private Boolean isolated;
    private String entryPrice;
    private String maxNotional;
    private String positionSide;
    private BigDecimal positionAmt;

    public Position() {
    }

    public BigDecimal getPositionAmt() {
        return this.positionAmt;
    }

    public void setPositionAmt(BigDecimal positionAmt) {
        this.positionAmt = positionAmt;
    }

    public Boolean getIsolated() {
        return this.isolated;
    }

    public void setIsolated(Boolean isolated) {
        this.isolated = isolated;
    }

    public BigDecimal getLeverage() {
        return this.leverage;
    }

    public void setLeverage(BigDecimal leverage) {
        this.leverage = leverage;
    }

    public BigDecimal getInitialMargin() {
        return this.initialMargin;
    }

    public void setInitialMargin(BigDecimal initialMargin) {
        this.initialMargin = initialMargin;
    }

    public BigDecimal getMaintMargin() {
        return this.maintMargin;
    }

    public void setMaintMargin(BigDecimal maintMargin) {
        this.maintMargin = maintMargin;
    }

    public BigDecimal getOpenOrderInitialMargin() {
        return this.openOrderInitialMargin;
    }

    public void setOpenOrderInitialMargin(BigDecimal openOrderInitialMargin) {
        this.openOrderInitialMargin = openOrderInitialMargin;
    }

    public BigDecimal getPositionInitialMargin() {
        return this.positionInitialMargin;
    }

    public void setPositionInitialMargin(BigDecimal positionInitialMargin) {
        this.positionInitialMargin = positionInitialMargin;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getUnrealizedProfit() {
        return this.unrealizedProfit;
    }

    public void setUnrealizedProfit(BigDecimal unrealizedProfit) {
        this.unrealizedProfit = unrealizedProfit;
    }

    public String getEntryPrice() {
        return this.entryPrice;
    }

    public void setEntryPrice(String entryPrice) {
        this.entryPrice = entryPrice;
    }

    public String getMaxNotional() {
        return this.maxNotional;
    }

    public void setMaxNotional(String maxNotional) {
        this.maxNotional = maxNotional;
    }

    public String getPositionSide() {
        return this.positionSide;
    }

    public void setPositionSide(String positionSide) {
        this.positionSide = positionSide;
    }

    public String toString() {
        return (new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)).append("symbol", this.symbol).append("initialMargin", this.initialMargin).append("maintMargin", this.maintMargin).append("unrealizedProfit", this.unrealizedProfit).append("positionInitialMargin", this.positionInitialMargin).append("openOrderInitialMargin", this.openOrderInitialMargin).append("leverage", this.leverage).append("isolated", this.isolated).append("entryPrice", this.entryPrice).append("maxNotional", this.maxNotional).append("positionSide", this.positionSide).append("positionAmt", this.positionAmt).toString();
    }
}

