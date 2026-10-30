package com.hcl.rims.dto;

import java.math.BigDecimal;

public class SalesTrendDTO {

    private String date;
    private long orderCount;
    private BigDecimal totalSales;

    public SalesTrendDTO() {
    }

    public SalesTrendDTO(String date, long orderCount, BigDecimal totalSales) {
        this.date = date;
        this.orderCount = orderCount;
        this.totalSales = totalSales;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}
