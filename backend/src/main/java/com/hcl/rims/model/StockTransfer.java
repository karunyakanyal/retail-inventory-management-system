package com.hcl.rims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transfers")
public class StockTransfer {

    public enum TransferStatus {
        PENDING,
        IN_TRANSIT,
        COMPLETED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String transferNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Column(nullable = false)
    private String sourceLocation;

    @NotBlank
    @Column(nullable = false)
    private String destLocation;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    private LocalDateTime transferDate;

    private String notes;

    @PrePersist
    public void prePersist() {
        if (transferDate == null) {
            transferDate = LocalDateTime.now();
        }
    }

    public StockTransfer() {
    }

    public StockTransfer(String transferNumber, Product product, String sourceLocation, String destLocation, Integer quantity, TransferStatus status, String notes) {
        this.transferNumber = transferNumber;
        this.product = product;
        this.sourceLocation = sourceLocation;
        this.destLocation = destLocation;
        this.quantity = quantity;
        this.status = status;
        this.notes = notes;
        this.transferDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransferNumber() {
        return transferNumber;
    }

    public void setTransferNumber(String transferNumber) {
        this.transferNumber = transferNumber;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
