package com.hcl.rims.service;

import com.hcl.rims.dto.StockAdjustmentDTO;
import com.hcl.rims.dto.StockTransferRequestDTO;
import com.hcl.rims.exception.BadRequestException;
import com.hcl.rims.exception.InsufficientStockException;
import com.hcl.rims.exception.ResourceNotFoundException;
import com.hcl.rims.model.Product;
import com.hcl.rims.model.StockLevel;
import com.hcl.rims.model.StockTransfer;
import com.hcl.rims.repo.ProductRepository;
import com.hcl.rims.repo.StockLevelRepository;
import com.hcl.rims.repo.StockTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final StockLevelRepository stockLevelRepository;
    private final StockTransferRepository stockTransferRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public InventoryService(StockLevelRepository stockLevelRepository,
                            StockTransferRepository stockTransferRepository,
                            ProductRepository productRepository,
                            AuditService auditService) {
        this.stockLevelRepository = stockLevelRepository;
        this.stockTransferRepository = stockTransferRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public List<StockLevel> findAllStockLevels() {
        return stockLevelRepository.findAll();
    }

    public List<StockLevel> findStockByLocation(String location) {
        return stockLevelRepository.findByLocation(location);
    }

    public List<StockLevel> findLowStock() {
        return stockLevelRepository.findLowStock();
    }

    public Optional<StockLevel> findStock(Long productId, String location) {
        return stockLevelRepository.findByProductIdAndLocation(productId, location);
    }

    @Transactional
    public StockLevel updateStock(StockLevel stockLevel) {
        StockLevel saved = stockLevelRepository.save(stockLevel);
        auditService.log("STOCK_UPDATE", "StockLevel", saved.getId(),
            "Updated stock for product ID=" + saved.getProduct().getId() + " at " + saved.getLocation() + " to qty=" + saved.getQty());
        return saved;
    }

    @Transactional
    public StockLevel adjustStock(StockAdjustmentDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + dto.getProductId()));

        StockLevel stockLevel = stockLevelRepository.findByProductIdAndLocation(dto.getProductId(), dto.getLocation())
            .orElseGet(() -> new StockLevel(product, dto.getLocation(), 0, 10, 5));

        int newQty = stockLevel.getQty() + dto.getQuantityDelta();
        if (newQty < 0) {
            throw new BadRequestException("Adjustment would cause negative inventory. Current qty: "
                + stockLevel.getQty() + ", Delta: " + dto.getQuantityDelta());
        }

        int oldQty = stockLevel.getQty();
        stockLevel.setQty(newQty);
        StockLevel saved = stockLevelRepository.save(stockLevel);

        auditService.log("STOCK_ADJUSTMENT", "StockLevel", saved.getId(),
            "Adjusted SKU: " + product.getSku() + " at " + dto.getLocation() +
            " from " + oldQty + " to " + newQty + " (delta: " + dto.getQuantityDelta() + "). Reason: " + dto.getReason());

        return saved;
    }

    @Transactional
    public StockTransfer transferStock(StockTransferRequestDTO dto) {
        if (dto.getSourceLocation().trim().equalsIgnoreCase(dto.getDestLocation().trim())) {
            throw new BadRequestException("Source and destination locations must be different");
        }

        if (dto.getQuantity() <= 0) {
            throw new BadRequestException("Transfer quantity must be greater than zero");
        }

        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + dto.getProductId()));

        StockLevel sourceStock = stockLevelRepository.findByProductIdAndLocation(dto.getProductId(), dto.getSourceLocation())
            .orElseThrow(() -> new InsufficientStockException("No stock entry found at source location: " + dto.getSourceLocation()));

        if (sourceStock.getQty() < dto.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock at " + dto.getSourceLocation() +
                ". Available: " + sourceStock.getQty() + ", Requested: " + dto.getQuantity());
        }

        // Deduct from source
        sourceStock.setQty(sourceStock.getQty() - dto.getQuantity());
        stockLevelRepository.save(sourceStock);

        // Add to destination
        StockLevel destStock = stockLevelRepository.findByProductIdAndLocation(dto.getProductId(), dto.getDestLocation())
            .orElseGet(() -> new StockLevel(product, dto.getDestLocation(), 0, 10, 5));
        destStock.setQty(destStock.getQty() + dto.getQuantity());
        stockLevelRepository.save(destStock);

        // Record transfer
        String transferNumber = "TRF-" + System.currentTimeMillis();
        StockTransfer transfer = new StockTransfer(
            transferNumber,
            product,
            dto.getSourceLocation(),
            dto.getDestLocation(),
            dto.getQuantity(),
            StockTransfer.TransferStatus.COMPLETED,
            dto.getNotes()
        );
        StockTransfer savedTransfer = stockTransferRepository.save(transfer);

        auditService.log("STOCK_TRANSFER", "StockTransfer", savedTransfer.getId(),
            "Transferred " + dto.getQuantity() + " units of " + product.getSku() +
            " from " + dto.getSourceLocation() + " to " + dto.getDestLocation());

        return savedTransfer;
    }

    public List<StockTransfer> findAllTransfers() {
        return stockTransferRepository.findAll();
    }

    @Transactional
    public StockTransfer createTransfer(StockTransfer transfer) {
        StockTransfer saved = stockTransferRepository.save(transfer);
        auditService.log("STOCK_TRANSFER_CREATED", "StockTransfer", saved.getId(),
            "Created transfer " + saved.getTransferNumber() + " for SKU: " + saved.getProduct().getSku());
        return saved;
    }
}
