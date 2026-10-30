package com.hcl.rims.web;

import com.hcl.rims.dto.ProductDTO;
import com.hcl.rims.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        if (page == null && size == null && name == null && categoryId == null
                && barcode == null && minPrice == null && maxPrice == null) {
            List<ProductDTO> all = productService.findAll();
            return ResponseEntity.ok(all);
        }

        int pageNum = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 20;

        String[] sortParams = sort.split(",");
        String sortProperty = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc"))
            ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortProperty));
        Page<ProductDTO> searchResults = productService.search(name, categoryId, barcode, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(searchResults);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findDtoById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        return productService.findBySku(sku)
            .map(productService::toDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductDTO> getProductByBarcode(@PathVariable String barcode) {
        return productService.findByBarcode(barcode)
            .map(productService::toDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rfid/{rfidTag}")
    public ResponseEntity<ProductDTO> getProductByRfid(@PathVariable String rfidTag) {
        return productService.findByRfidTag(rfidTag)
            .map(productService::toDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO dto) {
        ProductDTO created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        ProductDTO updated = productService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        String csv = productService.exportToCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"products.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importCsvFile(@RequestParam("file") MultipartFile file) throws IOException {
        int imported = productService.importFromCsv(file.getInputStream());
        Map<String, Object> response = new HashMap<>();
        response.put("importedCount", imported);
        response.put("message", "Successfully imported " + imported + " products from CSV");
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/import", consumes = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> importCsvRaw(@RequestBody String csvData) {
        InputStream stream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        int imported = productService.importFromCsv(stream);
        Map<String, Object> response = new HashMap<>();
        response.put("importedCount", imported);
        response.put("message", "Successfully imported " + imported + " products from CSV");
        return ResponseEntity.ok(response);
    }
}
