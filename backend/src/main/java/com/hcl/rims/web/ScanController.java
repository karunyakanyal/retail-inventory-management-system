package com.hcl.rims.web;

import com.hcl.rims.dto.ProductDTO;
import com.hcl.rims.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final ProductService productService;

    public ScanController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<ProductDTO> scanCode(@PathVariable String code) {
        ProductDTO product = productService.scan(code);
        return ResponseEntity.ok(product);
    }
}
