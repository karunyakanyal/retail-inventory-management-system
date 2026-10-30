package com.hcl.rims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class StockLevelDTO {

    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productSku;
    private String productName;

    @NotBlank(message = "Location code is required")
    private String location;

    @NotNull(message = "Quantity is required")
    private Integer qty;

    private Integer reorderPoint = 10;
    private Integer safetyStock = 5;
    private LocalDateTime updatedAt;

    public StockLevelDTO() {
    }

    public StockLevelDTO(Long id, Long productId, String productSku, String productName, String location, Integer qty, Integer reorderPoint, Integer safetyStock, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.location = location;
        this.qty = qty;
        this.reorderPoint = reorderPoint;
        this.safetyStock = safetyStock;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(Integer safetyStock) {
        this.safetyStock = safetyStock;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
