package com.hcl.rims.web;

import com.hcl.rims.dto.SupplierDTO;
import com.hcl.rims.dto.SupplierPerformanceDTO;
import com.hcl.rims.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierDTO> getAllSuppliers() {
        return supplierService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierDTO dto) {
        SupplierDTO created = supplierService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDTO dto) {
        SupplierDTO updated = supplierService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/performance")
    public ResponseEntity<SupplierPerformanceDTO> getSupplierPerformance(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getPerformance(id));
    }

    @GetMapping("/performance")
    public List<SupplierPerformanceDTO> getAllSupplierPerformance() {
        return supplierService.getAllPerformance();
    }
}
