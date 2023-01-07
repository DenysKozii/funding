package bot.binance;

import java.math.BigDecimal;

public class Order {
    private String clientOrderId;
    private BigDecimal cumQty;
    private BigDecimal cumQuote;
    private BigDecimal executedQty;
    private Long orderId;
    private BigDecimal avgPrice;
    private BigDecimal origQty;
    private BigDecimal price;
    private Boolean reduceOnly;
    private String side;
    private String positionSide;
    private String status;
    private BigDecimal stopPrice;
    private Boolean closePosition;
    private String symbol;
    private String timeInForce;
    private String type;
    private String origType;
    private BigDecimal activatePrice;
    private BigDecimal priceRate;
    private Long updateTime;
    private String workingType;
    private Boolean priceProtect;

    public Order() {
    }

    public String getOrigType() {
        return this.origType;
    }

    public void setOrigType(String origType) {
        this.origType = origType;
    }

    public BigDecimal getActivatePrice() {
        return this.activatePrice;
    }

    public void setActivatePrice(BigDecimal activatePrice) {
        this.activatePrice = activatePrice;
    }

    public BigDecimal getPriceRate() {
        return this.priceRate;
    }

    public void setPriceRate(BigDecimal priceRate) {
        this.priceRate = priceRate;
    }

    public void setPriceProtect(Boolean priceProtect) {
        this.priceProtect = priceProtect;
    }

    public BigDecimal getCumQty() {
        return this.cumQty;
    }

    public void setCumQty(BigDecimal cumQty) {
        this.cumQty = cumQty;
    }

    public Boolean getClosePosition() {
        return this.closePosition;
    }

    public void setClosePosition(Boolean closePosition) {
        this.closePosition = closePosition;
    }

    public Boolean getPriceProtect() {
        return this.priceProtect;
    }

    public String getClientOrderId() {
        return this.clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public BigDecimal getCumQuote() {
        return this.cumQuote;
    }

    public void setCumQuote(BigDecimal cumQuote) {
        this.cumQuote = cumQuote;
    }

    public BigDecimal getExecutedQty() {
        return this.executedQty;
    }

    public void setExecutedQty(BigDecimal executedQty) {
        this.executedQty = executedQty;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrigQty() {
        return this.origQty;
    }

    public void setOrigQty(BigDecimal origQty) {
        this.origQty = origQty;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getReduceOnly() {
        return this.reduceOnly;
    }

    public void setReduceOnly(Boolean reduceOnly) {
        this.reduceOnly = reduceOnly;
    }

    public String getSide() {
        return this.side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getPositionSide() {
        return this.positionSide;
    }

    public void setPositionSide(String positionSide) {
        this.positionSide = positionSide;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getStopPrice() {
        return this.stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getAvgPrice() {
        return this.avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getTimeInForce() {
        return this.timeInForce;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getWorkingType() {
        return this.workingType;
    }

    public void setWorkingType(String workingType) {
        this.workingType = workingType;
    }

    public String toString() {
        return clientOrderId;
    }
}

