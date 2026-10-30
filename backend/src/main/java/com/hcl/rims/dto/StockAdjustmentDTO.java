package com.hcl.rims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockAdjustmentDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Location code is required")
    private String location;

    @NotNull(message = "Adjustment quantity delta is required")
    private Integer quantityDelta;

    @NotBlank(message = "Reason for stock adjustment is required")
    private String reason;

    public StockAdjustmentDTO() {
    }

    public StockAdjustmentDTO(Long productId, String location, Integer quantityDelta, String reason) {
        this.productId = productId;
        this.location = location;
        this.quantityDelta = quantityDelta;
        this.reason = reason;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(Integer quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
