package com.hcl.rims.service;

import com.hcl.rims.dto.*;
import com.hcl.rims.exception.BadRequestException;
import com.hcl.rims.exception.InsufficientStockException;
import com.hcl.rims.exception.ResourceNotFoundException;
import com.hcl.rims.model.*;
import com.hcl.rims.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final StockLevelRepository stockLevelRepository;
    private final AuditService auditService;

    public OrderService(SalesOrderRepository salesOrderRepository,
                        PurchaseOrderRepository purchaseOrderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        SupplierRepository supplierRepository,
                        StockLevelRepository stockLevelRepository,
                        AuditService auditService) {
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.auditService = auditService;
    }

    public List<SalesOrder> findAllSalesOrders() {
        return salesOrderRepository.findAll();
    }

    public List<SalesOrderDTO> findAllSalesOrderDTOs() {
        return salesOrderRepository.findAll().stream()
            .map(this::toSalesOrderDTO)
            .collect(Collectors.toList());
    }

    public Optional<SalesOrder> findSalesOrderById(Long id) {
        return salesOrderRepository.findById(id);
    }

    public SalesOrderDTO findSalesOrderDtoById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        return toSalesOrderDTO(order);
    }

    public Optional<SalesOrder> findSalesOrderByNumber(String orderNumber) {
        return salesOrderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional
    public SalesOrder createSalesOrder(SalesOrder order) {
        order.recalculateTotal();
        SalesOrder saved = salesOrderRepository.save(order);
        auditService.log("CREATE_SALES_ORDER", "SalesOrder", saved.getId(),
            "Created sales order: " + saved.getOrderNumber() + ", Total: " + saved.getTotalAmount());
        return saved;
    }

    @Transactional
    public SalesOrderDTO createSalesOrderFromDto(SalesOrderDTO dto) {
        String orderNumber = (dto.getOrderNumber() != null && !dto.getOrderNumber().isBlank())
            ? dto.getOrderNumber() : "SO-" + System.currentTimeMillis();

        if (salesOrderRepository.findByOrderNumber(orderNumber).isPresent()) {
            throw new BadRequestException("Sales order with number '" + orderNumber + "' already exists");
        }

        Customer customer = null;
        if (dto.getCustomerId() != null) {
            customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + dto.getCustomerId()));
        }

        String location = (dto.getLocation() != null && !dto.getLocation().isBlank()) ? dto.getLocation() : "WH-001";
        SalesOrder order = new SalesOrder(orderNumber, customer, SalesOrder.OrderStatus.NEW, location);
        order.setNotes(dto.getNotes());

        if (dto.getLines() != null) {
            for (OrderLineDTO lineDto : dto.getLines()) {
                Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + lineDto.getProductId()));
                BigDecimal unitPrice = lineDto.getUnitPrice() != null ? lineDto.getUnitPrice() : product.getPrice();
                OrderLine line = new OrderLine(order, product, lineDto.getQuantity(), unitPrice);
                order.addLine(line);
            }
        }

        SalesOrder saved = salesOrderRepository.save(order);
        auditService.log("CREATE_SALES_ORDER", "SalesOrder", saved.getId(),
            "Created sales order: " + saved.getOrderNumber() + ", Lines: " + saved.getLines().size());
        return toSalesOrderDTO(saved);
    }

    @Transactional
    public SalesOrderDTO allocateStock(Long orderId, String targetLocation) {
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + orderId));

        if (order.getStatus() != SalesOrder.OrderStatus.NEW) {
            throw new BadRequestException("Cannot allocate stock for order in status: " + order.getStatus() + ". Must be NEW.");
        }

        String location = (targetLocation != null && !targetLocation.isBlank()) ? targetLocation : order.getLocation();

        // 1. Validation pass: check availability for all items
        for (OrderLine line : order.getLines()) {
            StockLevel stock = stockLevelRepository.findByProductIdAndLocation(line.getProduct().getId(), location)
                .orElse(null);
            int available = stock != null ? stock.getQty() : 0;
            if (available < line.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product " + line.getProduct().getSku() +
                    " at " + location + ". Available: " + available + ", Required: " + line.getQuantity());
            }
        }

        // 2. Deduction pass: atomically reduce available stock
        for (OrderLine line : order.getLines()) {
            StockLevel stock = stockLevelRepository.findByProductIdAndLocation(line.getProduct().getId(), location).orElseThrow();
            stock.setQty(stock.getQty() - line.getQuantity());
            stockLevelRepository.save(stock);
        }

        order.setLocation(location);
        order.setStockAllocated(true);
        order.setStatus(SalesOrder.OrderStatus.ALLOCATED);
        SalesOrder saved = salesOrderRepository.save(order);

        auditService.log("ALLOCATE_ORDER", "SalesOrder", saved.getId(),
            "Allocated stock at " + location + " for order: " + saved.getOrderNumber());

        return toSalesOrderDTO(saved);
    }

    @Transactional
    public SalesOrderDTO updateOrderStatus(Long orderId, SalesOrder.OrderStatus newStatus, String notes) {
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + orderId));

        SalesOrder.OrderStatus current = order.getStatus();

        // State Machine Validation: NEW -> ALLOCATED -> PICKED -> PACKED -> SHIPPED -> DELIVERED
        boolean isValid = switch (current) {
            case NEW -> newStatus == SalesOrder.OrderStatus.ALLOCATED;
            case ALLOCATED -> newStatus == SalesOrder.OrderStatus.PICKED;
            case PICKED -> newStatus == SalesOrder.OrderStatus.PACKED;
            case PACKED -> newStatus == SalesOrder.OrderStatus.SHIPPED;
            case SHIPPED -> newStatus == SalesOrder.OrderStatus.DELIVERED;
            default -> false;
        };

        if (!isValid) {
            throw new BadRequestException("Invalid order status transition from " + current + " to " + newStatus);
        }

        order.setStatus(newStatus);
        if (notes != null) {
            order.setNotes(notes);
        }
        SalesOrder saved = salesOrderRepository.save(order);

        auditService.log("UPDATE_ORDER_STATUS", "SalesOrder", saved.getId(),
            "Order " + saved.getOrderNumber() + " transitioned from " + current + " to " + newStatus);

        return toSalesOrderDTO(saved);
    }

    @Transactional
    public SalesOrderDTO cancelOrder(Long orderId, String reason) {
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + orderId));

        if (order.getStatus() == SalesOrder.OrderStatus.CANCELLED) {
            return toSalesOrderDTO(order);
        }

        if (order.getStatus() == SalesOrder.OrderStatus.SHIPPED ||
            order.getStatus() == SalesOrder.OrderStatus.DELIVERED ||
            order.getStatus() == SalesOrder.OrderStatus.RETURNED) {
            throw new BadRequestException("Cannot cancel order in status " + order.getStatus() + ". Use return/refund instead.");
        }

        // Restock allocated inventory if allocated
        if (order.isStockAllocated()) {
            for (OrderLine line : order.getLines()) {
                StockLevel stock = stockLevelRepository.findByProductIdAndLocation(line.getProduct().getId(), order.getLocation())
                    .orElseGet(() -> new StockLevel(line.getProduct(), order.getLocation(), 0, 10, 5));
                stock.setQty(stock.getQty() + line.getQuantity());
                stockLevelRepository.save(stock);
            }
            order.setStockAllocated(false);
        }

        order.setStatus(SalesOrder.OrderStatus.CANCELLED);
        if (reason != null) {
            order.setNotes("Cancelled: " + reason);
        }
        SalesOrder saved = salesOrderRepository.save(order);

        auditService.log("CANCEL_ORDER", "SalesOrder", saved.getId(),
            "Cancelled order " + saved.getOrderNumber() + ". Reason: " + reason);

        return toSalesOrderDTO(saved);
    }

    @Transactional
    public SalesOrderDTO returnOrder(Long orderId, ReturnOrderDTO returnDto) {
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + orderId));

        if (order.getStatus() != SalesOrder.OrderStatus.SHIPPED && order.getStatus() != SalesOrder.OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot return order in status " + order.getStatus() + ". Order must be SHIPPED or DELIVERED.");
        }

        String restockLocation = (returnDto.getRestockLocation() != null && !returnDto.getRestockLocation().isBlank())
            ? returnDto.getRestockLocation() : order.getLocation();

        // Restock returned items
        for (OrderLine line : order.getLines()) {
            StockLevel stock = stockLevelRepository.findByProductIdAndLocation(line.getProduct().getId(), restockLocation)
                .orElseGet(() -> new StockLevel(line.getProduct(), restockLocation, 0, 10, 5));
            stock.setQty(stock.getQty() + line.getQuantity());
            stockLevelRepository.save(stock);
        }

        order.setStatus(SalesOrder.OrderStatus.RETURNED);
        order.setNotes("Returned to " + restockLocation + ". Reason: " + returnDto.getReason());
        SalesOrder saved = salesOrderRepository.save(order);

        auditService.log("RETURN_ORDER", "SalesOrder", saved.getId(),
            "Returned order " + saved.getOrderNumber() + " to " + restockLocation + ". Reason: " + returnDto.getReason());

        return toSalesOrderDTO(saved);
    }

    // Purchase Order Operations
    public List<PurchaseOrder> findAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public List<PurchaseOrderDTO> findAllPurchaseOrderDTOs() {
        return purchaseOrderRepository.findAll().stream()
            .map(this::toPurchaseOrderDTO)
            .collect(Collectors.toList());
    }

    public Optional<PurchaseOrder> findPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public PurchaseOrderDTO findPurchaseOrderDtoById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        return toPurchaseOrderDTO(po);
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        order.recalculateTotal();
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        auditService.log("CREATE_PURCHASE_ORDER", "PurchaseOrder", saved.getId(),
            "Created purchase order: " + saved.getPoNumber() + ", Total: " + saved.getTotalAmount());
        return saved;
    }

    @Transactional
    public PurchaseOrderDTO createPurchaseOrderFromDto(PurchaseOrderDTO dto) {
        String poNumber = (dto.getPoNumber() != null && !dto.getPoNumber().isBlank())
            ? dto.getPoNumber() : "PO-" + System.currentTimeMillis();

        if (purchaseOrderRepository.findByPoNumber(poNumber).isPresent()) {
            throw new BadRequestException("Purchase order with number '" + poNumber + "' already exists");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + dto.getSupplierId()));

        String location = (dto.getWarehouseLocation() != null && !dto.getWarehouseLocation().isBlank())
            ? dto.getWarehouseLocation() : "WH-001";

        PurchaseOrder po = new PurchaseOrder(
            poNumber,
            supplier,
            dto.getExpectedDate() != null ? dto.getExpectedDate() : LocalDate.now().plusDays(7),
            PurchaseOrder.POStatus.ORDERED,
            location
        );

        if (dto.getLines() != null) {
            for (POLineDTO lineDto : dto.getLines()) {
                Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + lineDto.getProductId()));
                BigDecimal unitCost = lineDto.getUnitCost() != null ? lineDto.getUnitCost() : product.getCostPrice();
                POLine line = new POLine(po, product, lineDto.getQuantity(), unitCost);
                po.addLine(line);
            }
        }

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditService.log("CREATE_PURCHASE_ORDER", "PurchaseOrder", saved.getId(),
            "Created PO " + saved.getPoNumber() + " with " + saved.getLines().size() + " lines");
        return toPurchaseOrderDTO(saved);
    }

    @Transactional
    public PurchaseOrderDTO receivePurchaseOrder(Long poId, ReceivePODTO receiveDto) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + poId));

        if (po.getStatus() == PurchaseOrder.POStatus.RECEIVED) {
            throw new BadRequestException("Purchase order " + po.getPoNumber() + " has already been received");
        }

        String warehouse = (receiveDto != null && receiveDto.getWarehouseLocation() != null && !receiveDto.getWarehouseLocation().isBlank())
            ? receiveDto.getWarehouseLocation() : po.getWarehouseLocation();

        LocalDate receivedDate = (receiveDto != null && receiveDto.getReceivedDate() != null)
            ? receiveDto.getReceivedDate() : LocalDate.now();

        // Increase StockLevel at target warehouse for each line
        for (POLine line : po.getLines()) {
            StockLevel stock = stockLevelRepository.findByProductIdAndLocation(line.getProduct().getId(), warehouse)
                .orElseGet(() -> new StockLevel(line.getProduct(), warehouse, 0, 10, 5));
            stock.setQty(stock.getQty() + line.getQuantity());
            stockLevelRepository.save(stock);
        }

        po.setWarehouseLocation(warehouse);
        po.setReceivedDate(receivedDate);
        po.setStatus(PurchaseOrder.POStatus.RECEIVED);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        auditService.log("RECEIVE_PURCHASE_ORDER", "PurchaseOrder", saved.getId(),
            "Received PO " + saved.getPoNumber() + " into warehouse " + warehouse + " on " + receivedDate);

        return toPurchaseOrderDTO(saved);
    }

    public SalesOrderDTO toSalesOrderDTO(SalesOrder order) {
        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getName());
        }
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setLocation(order.getLocation());
        dto.setStockAllocated(order.isStockAllocated());
        dto.setNotes(order.getNotes());

        List<OrderLineDTO> lineDtos = order.getLines().stream().map(l -> new OrderLineDTO(
            l.getId(),
            l.getProduct().getId(),
            l.getProduct().getSku(),
            l.getProduct().getName(),
            l.getQuantity(),
            l.getUnitPrice(),
            l.getSubtotal()
        )).collect(Collectors.toList());
        dto.setLines(lineDtos);

        return dto;
    }

    public PurchaseOrderDTO toPurchaseOrderDTO(PurchaseOrder po) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(po.getId());
        dto.setPoNumber(po.getPoNumber());
        if (po.getSupplier() != null) {
            dto.setSupplierId(po.getSupplier().getId());
            dto.setSupplierName(po.getSupplier().getName());
        }
        dto.setOrderDate(po.getOrderDate());
        dto.setExpectedDate(po.getExpectedDate());
        dto.setReceivedDate(po.getReceivedDate());
        dto.setWarehouseLocation(po.getWarehouseLocation());
        dto.setStatus(po.getStatus());
        dto.setTotalAmount(po.getTotalAmount());

        List<POLineDTO> lineDtos = po.getLines().stream().map(l -> new POLineDTO(
            l.getId(),
            l.getProduct().getId(),
            l.getProduct().getSku(),
            l.getProduct().getName(),
            l.getQuantity(),
            l.getUnitCost(),
            l.getSubtotal()
        )).collect(Collectors.toList());
        dto.setLines(lineDtos);

        return dto;
    }
}
