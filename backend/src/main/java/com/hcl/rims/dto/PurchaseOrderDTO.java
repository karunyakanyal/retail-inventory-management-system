package com.hcl.rims.dto;

import com.hcl.rims.model.PurchaseOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDTO {

    private Long id;
    private String poNumber;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String supplierName;
    private LocalDateTime orderDate;
    private LocalDate expectedDate;
    private LocalDate receivedDate;
    private String warehouseLocation = "WH-001";
    private PurchaseOrder.POStatus status = PurchaseOrder.POStatus.DRAFT;
    private BigDecimal totalAmount;

    @NotEmpty(message = "PO lines cannot be empty")
    @Valid
    private List<POLineDTO> lines = new ArrayList<>();

    public PurchaseOrderDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
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

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public PurchaseOrder.POStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrder.POStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<POLineDTO> getLines() {
        return lines;
    }

    public void setLines(List<POLineDTO> lines) {
        this.lines = lines;
    }
}
