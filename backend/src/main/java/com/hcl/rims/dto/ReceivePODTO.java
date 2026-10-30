package com.hcl.rims.dto;

import java.time.LocalDate;

public class ReceivePODTO {

    private String warehouseLocation;
    private LocalDate receivedDate;
    private String notes;

    public ReceivePODTO() {
    }

    public ReceivePODTO(String warehouseLocation, LocalDate receivedDate, String notes) {
        this.warehouseLocation = warehouseLocation;
        this.receivedDate = receivedDate;
        this.notes = notes;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
