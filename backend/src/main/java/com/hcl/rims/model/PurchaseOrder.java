package com.hcl.rims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    public enum POStatus {
        DRAFT,
        SUBMITTED,
        ORDERED,
        RECEIVED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String poNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    private LocalDateTime orderDate;

    private LocalDate expectedDate;

    private LocalDate receivedDate;

    @Column(nullable = false)
    private String warehouseLocation = "WH-001";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private POStatus status = POStatus.DRAFT;

    @NotNull
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<POLine> lines = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }

    public PurchaseOrder() {
    }

    public PurchaseOrder(String poNumber, Supplier supplier, LocalDate expectedDate, POStatus status) {
        this.poNumber = poNumber;
        this.supplier = supplier;
        this.expectedDate = expectedDate;
        this.status = status;
        this.orderDate = LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.warehouseLocation = "WH-001";
    }

    public PurchaseOrder(String poNumber, Supplier supplier, LocalDate expectedDate, POStatus status, String warehouseLocation) {
        this.poNumber = poNumber;
        this.supplier = supplier;
        this.expectedDate = expectedDate;
        this.status = status;
        this.orderDate = LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.warehouseLocation = warehouseLocation != null ? warehouseLocation : "WH-001";
    }

    public void addLine(POLine line) {
        lines.add(line);
        line.setPurchaseOrder(this);
        recalculateTotal();
    }

    public void removeLine(POLine line) {
        lines.remove(line);
        line.setPurchaseOrder(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = lines.stream()
            .map(POLine::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
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

    public POStatus getStatus() {
        return status;
    }

    public void setStatus(POStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<POLine> getLines() {
        return lines;
    }

    public void setLines(List<POLine> lines) {
        this.lines = lines;
        recalculateTotal();
    }
}
