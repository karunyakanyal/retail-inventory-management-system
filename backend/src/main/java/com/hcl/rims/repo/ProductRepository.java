package com.hcl.rims.repo;

import com.hcl.rims.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    Optional<Product> findByRfidTag(String rfidTag);
    Optional<Product> findByBarcodeOrRfidTag(String barcode, String rfidTag);
    List<Product> findByCategoryId(Long categoryId);
    boolean existsBySku(String sku);
}
