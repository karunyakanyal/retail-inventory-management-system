package com.hcl.rims.service;

import com.hcl.rims.dto.*;
import com.hcl.rims.exception.BadRequestException;
import com.hcl.rims.exception.ResourceNotFoundException;
import com.hcl.rims.model.*;
import com.hcl.rims.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ErpService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;
    private final ReportService reportService;
    private final AuditService auditService;

    public ErpService(ProductService productService,
                      ProductRepository productRepository,
                      StockLevelRepository stockLevelRepository,
                      SalesOrderRepository salesOrderRepository,
                      CustomerRepository customerRepository,
                      OrderService orderService,
                      ReportService reportService,
                      AuditService auditService) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.orderService = orderService;
        this.reportService = reportService;
        this.auditService = auditService;
    }

    // --- Product ERP Integration ---
    public List<ProductDTO> exportProductsJson() {
        return productService.findAll();
    }

    public String exportProductsCsv() {
        return productService.exportToCsv();
    }

    @Transactional
    public int importProductsJson(List<ProductDTO> dtos) {
        int count = 0;
        for (ProductDTO dto : dtos) {
            Optional<Product> existing = productRepository.findBySku(dto.getSku());
            if (existing.isPresent()) {
                productService.update(existing.get().getId(), dto);
            } else {
                productService.create(dto);
            }
            count++;
        }
        auditService.log("ERP_IMPORT_PRODUCTS_JSON", "Product", null, "Imported " + count + " products via ERP JSON");
        return count;
    }

    @Transactional
    public int importProductsCsv(InputStream inputStream) {
        int count = productService.importFromCsv(inputStream);
        auditService.log("ERP_IMPORT_PRODUCTS_CSV", "Product", null, "Imported " + count + " products via ERP CSV");
        return count;
    }

    // --- Stock ERP Integration ---
    public List<StockLevelDTO> exportStockJson() {
        return stockLevelRepository.findAll().stream()
            .map(s -> new StockLevelDTO(
                s.getId(),
                s.getProduct().getId(),
                s.getProduct().getSku(),
                s.getProduct().getName(),
                s.getLocation(),
                s.getQty(),
                s.getReorderPoint(),
                s.getSafetyStock(),
                s.getUpdatedAt()
            )).collect(Collectors.toList());
    }

    public String exportStockCsv() {
        List<StockLevel> list = stockLevelRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Product_SKU,Location,Quantity,Reorder_Point,Safety_Stock\n");
        for (StockLevel sl : list) {
            sb.append(escape(sl.getProduct().getSku())).append(",");
            sb.append(escape(sl.getLocation())).append(",");
            sb.append(sl.getQty()).append(",");
            sb.append(sl.getReorderPoint()).append(",");
            sb.append(sl.getSafetyStock()).append("\n");
        }
        return sb.toString();
    }

    @Transactional
    public int importStockJson(List<StockLevelDTO> dtos) {
        int count = 0;
        for (StockLevelDTO dto : dtos) {
            Product product = null;
            if (dto.getProductId() != null) {
                product = productRepository.findById(dto.getProductId()).orElse(null);
            }
            if (product == null && dto.getProductSku() != null) {
                product = productRepository.findBySku(dto.getProductSku()).orElse(null);
            }
            if (product == null) {
                continue;
            }

            final Product finalProduct = product;
            StockLevel stock = stockLevelRepository.findByProductIdAndLocation(product.getId(), dto.getLocation())
                .orElseGet(() -> new StockLevel(finalProduct, dto.getLocation(), 0,
                    dto.getReorderPoint() != null ? dto.getReorderPoint() : 10,
                    dto.getSafetyStock() != null ? dto.getSafetyStock() : 5));

            stock.setQty(dto.getQty());
            if (dto.getReorderPoint() != null) stock.setReorderPoint(dto.getReorderPoint());
            if (dto.getSafetyStock() != null) stock.setSafetyStock(dto.getSafetyStock());
            stockLevelRepository.save(stock);
            count++;
        }
        auditService.log("ERP_IMPORT_STOCK_JSON", "StockLevel", null, "Synchronized " + count + " stock records via ERP JSON");
        return count;
    }

    @Transactional
    public int importStockCsv(InputStream inputStream) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (first) {
                    first = false;
                    if (line.toLowerCase().startsWith("product_sku") || line.toLowerCase().startsWith("sku")) {
                        continue;
                    }
                }

                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                String sku = parts[0].trim();
                String location = parts[1].trim();
                int qty = Integer.parseInt(parts[2].trim());
                int reorderPoint = parts.length > 3 ? Integer.parseInt(parts[3].trim()) : 10;
                int safetyStock = parts.length > 4 ? Integer.parseInt(parts[4].trim()) : 5;

                Optional<Product> pOpt = productRepository.findBySku(sku);
                if (pOpt.isEmpty()) continue;

                Product product = pOpt.get();
                StockLevel stock = stockLevelRepository.findByProductIdAndLocation(product.getId(), location)
                    .orElseGet(() -> new StockLevel(product, location, 0, reorderPoint, safetyStock));

                stock.setQty(qty);
                stock.setReorderPoint(reorderPoint);
                stock.setSafetyStock(safetyStock);
                stockLevelRepository.save(stock);
                count++;
            }
            auditService.log("ERP_IMPORT_STOCK_CSV", "StockLevel", null, "Synchronized " + count + " stock records via ERP CSV");
            return count;
        } catch (Exception e) {
            throw new BadRequestException("Failed to process ERP stock CSV import: " + e.getMessage());
        }
    }

    // --- Order ERP Integration ---
    public List<SalesOrderDTO> exportOrdersJson() {
        return orderService.findAllSalesOrderDTOs();
    }

    public String exportOrdersCsv() {
        return reportService.exportOrdersCsv();
    }

    @Transactional
    public int importOrdersJson(List<SalesOrderDTO> dtos) {
        int count = 0;
        for (SalesOrderDTO dto : dtos) {
            orderService.createSalesOrderFromDto(dto);
            count++;
        }
        auditService.log("ERP_IMPORT_ORDERS_JSON", "SalesOrder", null, "Imported " + count + " sales orders via ERP JSON");
        return count;
    }

    @Transactional
    public int importOrdersCsv(InputStream inputStream) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (first) {
                    first = false;
                    if (line.toLowerCase().startsWith("order_number") || line.toLowerCase().startsWith("ordernumber")) {
                        continue;
                    }
                }

                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String orderNum = parts[0].trim();
                String customerEmail = parts[1].trim();
                String location = parts[2].trim();
                String sku = parts[3].trim();
                int qty = Integer.parseInt(parts[4].trim());

                Product product = productRepository.findBySku(sku)
                    .orElseThrow(() -> new ResourceNotFoundException("Product with SKU not found: " + sku));

                Customer customer = customerRepository.findByEmail(customerEmail)
                    .orElseGet(() -> customerRepository.save(new Customer("Customer " + customerEmail, customerEmail, "", "", "", "", "")));

                SalesOrderDTO dto = new SalesOrderDTO();
                dto.setOrderNumber(orderNum);
                dto.setCustomerId(customer.getId());
                dto.setLocation(location);
                OrderLineDTO lineDTO = new OrderLineDTO(null, product.getId(), product.getSku(), product.getName(), qty, product.getPrice(), product.getPrice().multiply(BigDecimal.valueOf(qty)));
                dto.setLines(List.of(lineDTO));

                orderService.createSalesOrderFromDto(dto);
                count++;
            }
            auditService.log("ERP_IMPORT_ORDERS_CSV", "SalesOrder", null, "Imported " + count + " sales orders via ERP CSV");
            return count;
        } catch (Exception e) {
            throw new BadRequestException("Failed to process ERP orders CSV import: " + e.getMessage());
        }
    }

    private String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
