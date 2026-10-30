package com.hcl.rims.dto;

import java.math.BigDecimal;

public class KpiSummaryDTO {

    private BigDecimal totalStockValue;
    private long totalStockItems;
    private long stockoutCount;
    private long overstockCount;
    private long totalOrders;
    private long fulfilledOrders;
    private double orderFulfilmentRate;

    public KpiSummaryDTO() {
    }

    public KpiSummaryDTO(BigDecimal totalStockValue, long totalStockItems, long stockoutCount, long overstockCount, long totalOrders, long fulfilledOrders, double orderFulfilmentRate) {
        this.totalStockValue = totalStockValue;
        this.totalStockItems = totalStockItems;
        this.stockoutCount = stockoutCount;
        this.overstockCount = overstockCount;
        this.totalOrders = totalOrders;
        this.fulfilledOrders = fulfilledOrders;
        this.orderFulfilmentRate = orderFulfilmentRate;
    }

    public BigDecimal getTotalStockValue() {
        return totalStockValue;
    }

    public void setTotalStockValue(BigDecimal totalStockValue) {
        this.totalStockValue = totalStockValue;
    }

    public long getTotalStockItems() {
        return totalStockItems;
    }

    public void setTotalStockItems(long totalStockItems) {
        this.totalStockItems = totalStockItems;
    }

    public long getStockoutCount() {
        return stockoutCount;
    }

    public void setStockoutCount(long stockoutCount) {
        this.stockoutCount = stockoutCount;
    }

    public long getOverstockCount() {
        return overstockCount;
    }

    public void setOverstockCount(long overstockCount) {
        this.overstockCount = overstockCount;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getFulfilledOrders() {
        return fulfilledOrders;
    }

    public void setFulfilledOrders(long fulfilledOrders) {
        this.fulfilledOrders = fulfilledOrders;
    }

    public double getOrderFulfilmentRate() {
        return orderFulfilmentRate;
    }

    public void setOrderFulfilmentRate(double orderFulfilmentRate) {
        this.orderFulfilmentRate = orderFulfilmentRate;
    }
}
