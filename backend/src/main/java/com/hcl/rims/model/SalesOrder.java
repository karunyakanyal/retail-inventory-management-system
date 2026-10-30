package com.hcl.rims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

    public enum OrderStatus {
        NEW,
        ALLOCATED,
        PICKED,
        PACKED,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        RETURNED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDateTime orderDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @NotNull
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String location = "WH-001";

    @Column(nullable = false)
    private boolean stockAllocated = false;

    private String notes;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderLine> lines = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }

    public SalesOrder() {
    }

    public SalesOrder(String orderNumber, Customer customer, OrderStatus status) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.status = status;
        this.orderDate = LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.location = "WH-001";
        this.stockAllocated = false;
    }

    public SalesOrder(String orderNumber, Customer customer, OrderStatus status, String location) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.status = status;
        this.orderDate = LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.location = location != null ? location : "WH-001";
        this.stockAllocated = false;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
        line.setSalesOrder(this);
        recalculateTotal();
    }

    public void removeLine(OrderLine line) {
        lines.remove(line);
        line.setSalesOrder(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = lines.stream()
            .map(OrderLine::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
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

    public List<OrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
        recalculateTotal();
    }
}
