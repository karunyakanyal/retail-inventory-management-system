package com.hcl.rims.service;

import com.hcl.rims.dto.KpiSummaryDTO;
import com.hcl.rims.dto.SalesTrendDTO;
import com.hcl.rims.model.OrderLine;
import com.hcl.rims.model.Product;
import com.hcl.rims.model.SalesOrder;
import com.hcl.rims.model.StockLevel;
import com.hcl.rims.repo.ProductRepository;
import com.hcl.rims.repo.SalesOrderRepository;
import com.hcl.rims.repo.StockLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final StockLevelRepository stockLevelRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;

    public ReportService(StockLevelRepository stockLevelRepository,
                         SalesOrderRepository salesOrderRepository,
                         ProductRepository productRepository) {
        this.stockLevelRepository = stockLevelRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
    }

    public KpiSummaryDTO getKpiSummary() {
        List<StockLevel> stockLevels = stockLevelRepository.findAll();
        BigDecimal totalStockValue = BigDecimal.ZERO;
        long totalItems = 0;
        long stockoutCount = 0;
        long overstockCount = 0;

        for (StockLevel sl : stockLevels) {
            int qty = sl.getQty() != null ? sl.getQty() : 0;
            totalItems += qty;
            if (qty == 0) {
                stockoutCount++;
            }
            if (qty > (sl.getReorderPoint() * 3) || qty >= 200) {
                overstockCount++;
            }

            Product p = sl.getProduct();
            if (p != null) {
                BigDecimal unitPrice = p.getCostPrice() != null ? p.getCostPrice() : p.getPrice();
                if (unitPrice != null) {
                    totalStockValue = totalStockValue.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
                }
            }
        }

        List<SalesOrder> orders = salesOrderRepository.findAll();
        long totalOrders = orders.size();
        long fulfilledOrders = orders.stream()
            .filter(o -> o.getStatus() == SalesOrder.OrderStatus.SHIPPED || o.getStatus() == SalesOrder.OrderStatus.DELIVERED)
            .count();

        double fulfilmentRate = (totalOrders > 0)
            ? Math.round(((double) fulfilledOrders / totalOrders) * 1000.0) / 10.0
            : 100.0;

        return new KpiSummaryDTO(
            totalStockValue,
            totalItems,
            stockoutCount,
            overstockCount,
            totalOrders,
            fulfilledOrders,
            fulfilmentRate
        );
    }

    public List<SalesTrendDTO> getSalesTrends() {
        List<SalesOrder> orders = salesOrderRepository.findAll();
        Map<LocalDate, List<SalesOrder>> byDate = orders.stream()
            .collect(Collectors.groupingBy(o -> o.getOrderDate().toLocalDate()));

        List<SalesTrendDTO> trends = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        byDate.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                LocalDate date = entry.getKey();
                List<SalesOrder> dailyOrders = entry.getValue();
                BigDecimal dailyTotal = dailyOrders.stream()
                    .map(SalesOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                trends.add(new SalesTrendDTO(date.format(fmt), dailyOrders.size(), dailyTotal));
            });

        return trends;
    }

    public String exportInventoryCsv() {
        List<StockLevel> list = stockLevelRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Product_ID,SKU,Product_Name,Location,Quantity,Reorder_Point,Safety_Stock,Unit_Price,Total_Value\n");

        for (StockLevel s : list) {
            Product p = s.getProduct();
            BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            BigDecimal val = price.multiply(BigDecimal.valueOf(s.getQty()));

            sb.append(p.getId()).append(",");
            sb.append(escape(p.getSku())).append(",");
            sb.append(escape(p.getName())).append(",");
            sb.append(escape(s.getLocation())).append(",");
            sb.append(s.getQty()).append(",");
            sb.append(s.getReorderPoint()).append(",");
            sb.append(s.getSafetyStock()).append(",");
            sb.append(price).append(",");
            sb.append(val).append("\n");
        }
        return sb.toString();
    }

    public String exportOrdersCsv() {
        List<SalesOrder> orders = salesOrderRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Order_Number,Customer,Order_Date,Status,Location,Stock_Allocated,Total_Amount,Total_Items\n");

        for (SalesOrder o : orders) {
            String customerName = o.getCustomer() != null ? o.getCustomer().getName() : "Guest";
            int totalItems = o.getLines().stream().mapToInt(OrderLine::getQuantity).sum();

            sb.append(escape(o.getOrderNumber())).append(",");
            sb.append(escape(customerName)).append(",");
            sb.append(o.getOrderDate()).append(",");
            sb.append(o.getStatus()).append(",");
            sb.append(escape(o.getLocation())).append(",");
            sb.append(o.isStockAllocated()).append(",");
            sb.append(o.getTotalAmount()).append(",");
            sb.append(totalItems).append("\n");
        }
        return sb.toString();
    }

    private String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
