package com.hcl.rims.web;

import com.hcl.rims.dto.StockAdjustmentDTO;
import com.hcl.rims.dto.StockTransferRequestDTO;
import com.hcl.rims.model.StockLevel;
import com.hcl.rims.model.StockTransfer;
import com.hcl.rims.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/stock")
    public List<StockLevel> getStock(@RequestParam(required = false) String location,
                                     @RequestParam(required = false) Long productId) {
        if (productId != null && location != null && !location.isBlank()) {
            return inventoryService.findStock(productId, location)
                .map(List::of)
                .orElse(List.of());
        }
        if (location != null && !location.isBlank()) {
            return inventoryService.findStockByLocation(location);
        }
        return inventoryService.findAllStockLevels();
    }

    @GetMapping("/stock/{location}")
    public List<StockLevel> getStockByLocationPath(@PathVariable String location) {
        return inventoryService.findStockByLocation(location);
    }

    @GetMapping("/low-stock")
    public List<StockLevel> getLowStockAlerts() {
        return inventoryService.findLowStock();
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<StockLevel> adjustStock(@Valid @RequestBody StockAdjustmentDTO dto) {
        StockLevel adjusted = inventoryService.adjustStock(dto);
        return ResponseEntity.ok(adjusted);
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<StockTransfer> createTransfer(@Valid @RequestBody StockTransferRequestDTO dto) {
        StockTransfer transfer = inventoryService.transferStock(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }

    @GetMapping("/transfers")
    public List<StockTransfer> getTransfers() {
        return inventoryService.findAllTransfers();
    }

    @PutMapping("/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<StockLevel> updateStock(@Valid @RequestBody StockLevel stockLevel) {
        return ResponseEntity.ok(inventoryService.updateStock(stockLevel));
    }
}
