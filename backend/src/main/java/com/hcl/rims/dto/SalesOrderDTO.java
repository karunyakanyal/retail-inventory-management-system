package com.hcl.rims.dto;

import com.hcl.rims.model.SalesOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderDTO {

    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private LocalDateTime orderDate;
    private SalesOrder.OrderStatus status;
    private BigDecimal totalAmount;
    private String location;
    private boolean stockAllocated;
    private String notes;

    @NotEmpty(message = "Order lines cannot be empty")
    @Valid
    private List<OrderLineDTO> lines = new ArrayList<>();

    public SalesOrderDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public SalesOrder.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(SalesOrder.OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isStockAllocated() {
        return stockAllocated;
    }

    public void setStockAllocated(boolean stockAllocated) {
        this.stockAllocated = stockAllocated;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderLineDTO> getLines() {
        return lines;
    }

    public void setLines(List<OrderLineDTO> lines) {
        this.lines = lines;
    }
}
