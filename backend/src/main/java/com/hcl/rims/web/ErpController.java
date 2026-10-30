package com.hcl.rims.web;

import com.hcl.rims.dto.ProductDTO;
import com.hcl.rims.dto.SalesOrderDTO;
import com.hcl.rims.dto.StockLevelDTO;
import com.hcl.rims.service.ErpService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/erp")
public class ErpController {

    private final ErpService erpService;

    public ErpController(ErpService erpService) {
        this.erpService = erpService;
    }

    // --- Products ---
    @GetMapping("/products/export")
    public ResponseEntity<?> exportProducts(@RequestParam(defaultValue = "json") String format) {
        if ("csv".equalsIgnoreCase(format)) {
            String csv = erpService.exportProductsCsv();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"erp_products.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
        }
        return ResponseEntity.ok(erpService.exportProductsJson());
    }

    @PostMapping("/products/import/json")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importProductsJson(@RequestBody List<ProductDTO> products) {
        int count = erpService.importProductsJson(products);
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully imported " + count + " products from ERP JSON");
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/products/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importProductsCsvFile(@RequestParam("file") MultipartFile file) throws IOException {
        int count = erpService.importProductsCsv(file.getInputStream());
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully imported " + count + " products from ERP CSV");
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/products/import/csv", consumes = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importProductsCsvRaw(@RequestBody String csvData) {
        InputStream stream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        int count = erpService.importProductsCsv(stream);
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully imported " + count + " products from ERP CSV");
        return ResponseEntity.ok(res);
    }

    // --- Stock ---
    @GetMapping("/stock/export")
    public ResponseEntity<?> exportStock(@RequestParam(defaultValue = "json") String format) {
        if ("csv".equalsIgnoreCase(format)) {
            String csv = erpService.exportStockCsv();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"erp_stock.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
        }
        return ResponseEntity.ok(erpService.exportStockJson());
    }

    @PostMapping("/stock/import/json")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importStockJson(@RequestBody List<StockLevelDTO> stockList) {
        int count = erpService.importStockJson(stockList);
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully synchronized " + count + " stock records from ERP JSON");
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/stock/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importStockCsvFile(@RequestParam("file") MultipartFile file) throws IOException {
        int count = erpService.importStockCsv(file.getInputStream());
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully synchronized " + count + " stock records from ERP CSV");
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/stock/import/csv", consumes = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importStockCsvRaw(@RequestBody String csvData) {
        InputStream stream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        int count = erpService.importStockCsv(stream);
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully synchronized " + count + " stock records from ERP CSV");
        return ResponseEntity.ok(res);
    }

    // --- Orders ---
    @GetMapping("/orders/export")
    public ResponseEntity<?> exportOrders(@RequestParam(defaultValue = "json") String format) {
        if ("csv".equalsIgnoreCase(format)) {
            String csv = erpService.exportOrdersCsv();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"erp_orders.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
        }
        return ResponseEntity.ok(erpService.exportOrdersJson());
    }

    @PostMapping("/orders/import/json")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importOrdersJson(@RequestBody List<SalesOrderDTO> orderList) {
        int count = erpService.importOrdersJson(orderList);
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully imported " + count + " sales orders from ERP JSON");
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/orders/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importOrdersCsvFile(@RequestParam("file") MultipartFile file) throws IOException {
        int count = erpService.importOrdersCsv(file.getInputStream());
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully imported " + count + " sales orders from ERP CSV");
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/orders/import/csv", consumes = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importOrdersCsvRaw(@RequestBody String csvData) {
        InputStream stream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        int count = erpService.importOrdersCsv(stream);
        Map<String, Object> res = new HashMap<>();
        res.put("importedCount", count);
        res.put("message", "Successfully imported " + count + " sales orders from ERP CSV");
        return ResponseEntity.ok(res);
    }
}
