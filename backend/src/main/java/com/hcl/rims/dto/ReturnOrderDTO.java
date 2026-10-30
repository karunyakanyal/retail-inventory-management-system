package com.hcl.rims.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ReturnOrderDTO {

    @NotBlank(message = "Restock location is required")
    private String restockLocation;

    @NotBlank(message = "Return reason is required")
    private String reason;

    private BigDecimal refundAmount;

    public ReturnOrderDTO() {
    }

    public ReturnOrderDTO(String restockLocation, String reason, BigDecimal refundAmount) {
        this.restockLocation = restockLocation;
        this.reason = reason;
        this.refundAmount = refundAmount;
    }

    public String getRestockLocation() {
        return restockLocation;
    }

    public void setRestockLocation(String restockLocation) {
        this.restockLocation = restockLocation;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
}
