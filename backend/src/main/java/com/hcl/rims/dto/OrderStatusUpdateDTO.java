package com.hcl.rims.dto;

import com.hcl.rims.model.SalesOrder;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateDTO {

    @NotNull(message = "New order status is required")
    private SalesOrder.OrderStatus status;

    private String notes;

    public OrderStatusUpdateDTO() {
    }

    public OrderStatusUpdateDTO(SalesOrder.OrderStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public SalesOrder.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(SalesOrder.OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
