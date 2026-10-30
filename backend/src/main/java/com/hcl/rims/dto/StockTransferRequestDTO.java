package com.hcl.rims.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockTransferRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Source location is required")
    private String sourceLocation;

    @NotBlank(message = "Destination location is required")
    private String destLocation;

    @NotNull(message = "Transfer quantity is required")
    @Min(value = 1, message = "Transfer quantity must be at least 1")
    private Integer quantity;

    private String notes;

    public StockTransferRequestDTO() {
    }

    public StockTransferRequestDTO(Long productId, String sourceLocation, String destLocation, Integer quantity, String notes) {
        this.productId = productId;
        this.sourceLocation = sourceLocation;
        this.destLocation = destLocation;
        this.quantity = quantity;
        this.notes = notes;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getDestLocation() {
        return destLocation;
    }

    public void setDestLocation(String destLocation) {
        this.destLocation = destLocation;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
