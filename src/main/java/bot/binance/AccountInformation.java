package bot.binance;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AccountInformation {
    private BigDecimal feeTier;
    private Boolean canTrade;
    private Boolean canDeposit;
    private Boolean canWithdraw;
    private Long updateTime;
    private BigDecimal totalInitialMargin;
    private BigDecimal totalMaintMargin;
    private BigDecimal totalWalletBalance;
    private BigDecimal totalUnrealizedProfit;
    private BigDecimal totalMarginBalance;
    private BigDecimal totalPositionInitialMargin;
    private BigDecimal totalOpenOrderInitialMargin;
    private BigDecimal totalCrossWalletBalance;
    private BigDecimal totalCrossUnPnl;
    private BigDecimal availableBalance;
    private BigDecimal maxWithdrawAmount;
    private List<Asset> assets;
    private List<Position> positions;

    public AccountInformation() {
    }

    public BigDecimal getTotalCrossWalletBalance() {
        return this.totalCrossWalletBalance;
    }

    public void setTotalCrossWalletBalance(BigDecimal totalCrossWalletBalance) {
        this.totalCrossWalletBalance = totalCrossWalletBalance;
    }

    public BigDecimal getTotalCrossUnPnl() {
        return this.totalCrossUnPnl;
    }

    public void setTotalCrossUnPnl(BigDecimal totalCrossUnPnl) {
        this.totalCrossUnPnl = totalCrossUnPnl;
    }

    public BigDecimal getAvailableBalance() {
        return this.availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Boolean getCanDeposit() {
        return this.canDeposit;
    }

    public void setCanDeposit(Boolean canDeposit) {
        this.canDeposit = canDeposit;
    }

    public Boolean getCanTrade() {
        return this.canTrade;
    }

    public void setCanTrade(Boolean canTrade) {
        this.canTrade = canTrade;
    }

    public Boolean getCanWithdraw() {
        return this.canWithdraw;
    }

    public void setCanWithdraw(Boolean canWithdraw) {
        this.canWithdraw = canWithdraw;
    }

    public BigDecimal getFeeTier() {
        return this.feeTier;
    }

    public void setFeeTier(BigDecimal feeTier) {
        this.feeTier = feeTier;
    }

    public BigDecimal getMaxWithdrawAmount() {
        return this.maxWithdrawAmount;
    }

    public void setMaxWithdrawAmount(BigDecimal maxWithdrawAmount) {
        this.maxWithdrawAmount = maxWithdrawAmount;
    }

    public BigDecimal getTotalInitialMargin() {
        return this.totalInitialMargin;
    }

    public void setTotalInitialMargin(BigDecimal totalInitialMargin) {
        this.totalInitialMargin = totalInitialMargin;
    }

    public BigDecimal getTotalMaintMargin() {
        return this.totalMaintMargin;
    }

    public void setTotalMaintMargin(BigDecimal totalMaintMargin) {
        this.totalMaintMargin = totalMaintMargin;
    }

    public BigDecimal getTotalMarginBalance() {
        return this.totalMarginBalance;
    }

    public void setTotalMarginBalance(BigDecimal totalMarginBalance) {
        this.totalMarginBalance = totalMarginBalance;
    }

    public BigDecimal getTotalOpenOrderInitialMargin() {
        return this.totalOpenOrderInitialMargin;
    }

    public void setTotalOpenOrderInitialMargin(BigDecimal totalOpenOrderInitialMargin) {
        this.totalOpenOrderInitialMargin = totalOpenOrderInitialMargin;
    }

    public BigDecimal getTotalPositionInitialMargin() {
        return this.totalPositionInitialMargin;
    }

    public void setTotalPositionInitialMargin(BigDecimal totalPositionInitialMargin) {
        this.totalPositionInitialMargin = totalPositionInitialMargin;
    }

    public BigDecimal getTotalUnrealizedProfit() {
        return this.totalUnrealizedProfit;
    }

    public void setTotalUnrealizedProfit(BigDecimal totalUnrealizedProfit) {
        this.totalUnrealizedProfit = totalUnrealizedProfit;
    }

    public BigDecimal getTotalWalletBalance() {
        return this.totalWalletBalance;
    }

    public void setTotalWalletBalance(BigDecimal totalWalletBalance) {
        this.totalWalletBalance = totalWalletBalance;
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<Asset> getAssets() {
        return this.assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    public List<Position> getPositions() {
        return this.positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public String toString() {
        return (new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)).append("feeTier", this.feeTier).append("canTrade", this.canTrade).append("canDeposit", this.canDeposit).append("canWithdraw", this.canWithdraw).append("updateTime", this.updateTime).append("totalInitialMargin", this.totalInitialMargin).append("totalMaintMargin", this.totalMaintMargin).append("totalWalletBalance", this.totalWalletBalance).append("totalUnrealizedProfit", this.totalUnrealizedProfit).append("totalMarginBalance", this.totalMarginBalance).append("totalPositionInitialMargin", this.totalPositionInitialMargin).append("totalOpenOrderInitialMargin", this.totalOpenOrderInitialMargin).append("totalCrossWalletBalance", this.totalCrossWalletBalance).append("totalCrossUnPnl", this.totalCrossUnPnl).append("availableBalance", this.availableBalance).append("maxWithdrawAmount", this.maxWithdrawAmount).append("assets", this.assets).append("positions", this.positions).toString();
    }
}

