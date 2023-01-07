package bot.binance;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Asset {
    private String asset;
    private BigDecimal walletBalance;
    private BigDecimal unrealizedProfit;
    private BigDecimal marginBalance;
    private BigDecimal maintMargin;
    private BigDecimal initialMargin;
    private BigDecimal positionInitialMargin;
    private BigDecimal openOrderInitialMargin;
    private BigDecimal crossWalletBalance;
    private BigDecimal crossUnPnl;
    private BigDecimal availableBalance;
    private BigDecimal maxWithdrawAmount;

    public Asset() {
    }

    public BigDecimal getWalletBalance() {
        return this.walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public BigDecimal getCrossWalletBalance() {
        return this.crossWalletBalance;
    }

    public void setCrossWalletBalance(BigDecimal crossWalletBalance) {
        this.crossWalletBalance = crossWalletBalance;
    }

    public BigDecimal getCrossUnPnl() {
        return this.crossUnPnl;
    }

    public void setCrossUnPnl(BigDecimal crossUnPnl) {
        this.crossUnPnl = crossUnPnl;
    }

    public BigDecimal getAvailableBalance() {
        return this.availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getAsset() {
        return this.asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
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

    public BigDecimal getMarginBalance() {
        return this.marginBalance;
    }

    public void setMarginBalance(BigDecimal marginBalance) {
        this.marginBalance = marginBalance;
    }

    public BigDecimal getMaxWithdrawAmount() {
        return this.maxWithdrawAmount;
    }

    public void setMaxWithdrawAmount(BigDecimal maxWithdrawAmount) {
        this.maxWithdrawAmount = maxWithdrawAmount;
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

    public BigDecimal getUnrealizedProfit() {
        return this.unrealizedProfit;
    }

    public void setUnrealizedProfit(BigDecimal unrealizedProfit) {
        this.unrealizedProfit = unrealizedProfit;
    }

    public String toString() {
        return (new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)).append("asset", this.asset).append("walletBalance", this.walletBalance).append("unrealizedProfit", this.unrealizedProfit).append("marginBalance", this.marginBalance).append("maintMargin", this.maintMargin).append("initialMargin", this.initialMargin).append("positionInitialMargin", this.positionInitialMargin).append("openOrderInitialMargin", this.openOrderInitialMargin).append("crossWalletBalance", this.crossWalletBalance).append("crossUnPnl", this.crossUnPnl).append("availableBalance", this.availableBalance).append("maxWithdrawAmount", this.maxWithdrawAmount).toString();
    }
}

