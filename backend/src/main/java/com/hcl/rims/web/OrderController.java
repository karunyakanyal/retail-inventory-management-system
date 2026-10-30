package com.hcl.rims.web;

import com.hcl.rims.dto.*;
import com.hcl.rims.model.PurchaseOrder;
import com.hcl.rims.model.SalesOrder;
import com.hcl.rims.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // --- Sales Orders ---
    @GetMapping("/sales")
    public List<SalesOrderDTO> getAllSalesOrders() {
        return orderService.findAllSalesOrderDTOs();
    }

    @GetMapping("/sales/{id}")
    public ResponseEntity<SalesOrderDTO> getSalesOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findSalesOrderDtoById(id));
    }

    @PostMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<SalesOrderDTO> createSalesOrder(@Valid @RequestBody SalesOrderDTO orderDto) {
        SalesOrderDTO created = orderService.createSalesOrderFromDto(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/sales/{id}/allocate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<SalesOrderDTO> allocateStock(
            @PathVariable Long id,
            @RequestParam(required = false) String location) {
        SalesOrderDTO allocated = orderService.allocateStock(id, location);
        return ResponseEntity.ok(allocated);
    }

    @PutMapping("/sales/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<SalesOrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDTO statusDto) {
        SalesOrderDTO updated = orderService.updateOrderStatus(id, statusDto.getStatus(), statusDto.getNotes());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/sales/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<SalesOrderDTO> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "User cancellation request") String reason) {
        SalesOrderDTO cancelled = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(cancelled);
    }

    @PostMapping("/sales/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<SalesOrderDTO> returnOrder(
            @PathVariable Long id,
            @Valid @RequestBody ReturnOrderDTO returnDto) {
        SalesOrderDTO returned = orderService.returnOrder(id, returnDto);
        return ResponseEntity.ok(returned);
    }

    // --- Purchase Orders ---
    @GetMapping("/purchase")
    public List<PurchaseOrderDTO> getAllPurchaseOrders() {
        return orderService.findAllPurchaseOrderDTOs();
    }

    @GetMapping("/purchase/{id}")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findPurchaseOrderDtoById(id));
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody PurchaseOrderDTO poDto) {
        PurchaseOrderDTO created = orderService.createPurchaseOrderFromDto(poDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/purchase/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<PurchaseOrderDTO> receivePurchaseOrder(
            @PathVariable Long id,
            @RequestBody(required = false) ReceivePODTO receiveDto) {
        ReceivePODTO dto = receiveDto != null ? receiveDto : new ReceivePODTO();
        PurchaseOrderDTO received = orderService.receivePurchaseOrder(id, dto);
        return ResponseEntity.ok(received);
    }
}
