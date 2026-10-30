package com.hcl.rims.dto;

public class SupplierPerformanceDTO {

    private Long supplierId;
    private String supplierName;
    private long totalPOs;
    private long receivedPOs;
    private long onTimePOs;
    private double onTimeDeliveryRate;

    public SupplierPerformanceDTO() {
    }

    public SupplierPerformanceDTO(Long supplierId, String supplierName, long totalPOs, long receivedPOs, long onTimePOs, double onTimeDeliveryRate) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.totalPOs = totalPOs;
        this.receivedPOs = receivedPOs;
        this.onTimePOs = onTimePOs;
        this.onTimeDeliveryRate = onTimeDeliveryRate;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public long getTotalPOs() {
        return totalPOs;
    }

    public void setTotalPOs(long totalPOs) {
        this.totalPOs = totalPOs;
    }

    public long getReceivedPOs() {
        return receivedPOs;
    }

    public void setReceivedPOs(long receivedPOs) {
        this.receivedPOs = receivedPOs;
    }

    public long getOnTimePOs() {
        return onTimePOs;
    }

    public void setOnTimePOs(long onTimePOs) {
        this.onTimePOs = onTimePOs;
    }

    public double getOnTimeDeliveryRate() {
        return onTimeDeliveryRate;
    }

    public void setOnTimeDeliveryRate(double onTimeDeliveryRate) {
        this.onTimeDeliveryRate = onTimeDeliveryRate;
    }
}
