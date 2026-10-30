package com.hcl.rims.service;

import com.hcl.rims.dto.ProductDTO;
import com.hcl.rims.exception.BadRequestException;
import com.hcl.rims.exception.ResourceNotFoundException;
import com.hcl.rims.model.Category;
import com.hcl.rims.model.Product;
import com.hcl.rims.repo.CategoryRepository;
import com.hcl.rims.repo.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          AuditService auditService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
    }

    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public ProductDTO findDtoById(Long id) {
        return toDTO(findEntityById(id));
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product findEntityById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    public Optional<Product> findByRfidTag(String rfidTag) {
        return productRepository.findByRfidTag(rfidTag);
    }

    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Page<ProductDTO> search(String name, Long categoryId, String barcode,
                                  BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (barcode != null && !barcode.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("barcode"), barcode.trim()));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(this::toDTO);
    }

    public ProductDTO scan(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Scan code cannot be empty");
        }
        String cleanCode = code.trim();
        Product product = productRepository.findByBarcode(cleanCode)
            .or(() -> productRepository.findByRfidTag(cleanCode))
            .orElseThrow(() -> new ResourceNotFoundException("No product found matching barcode or RFID: " + cleanCode));

        return toDTO(product);
    }

    @Transactional
    public Product save(Product product) {
        Product saved = productRepository.save(product);
        auditService.log("SAVE_PRODUCT", "Product", saved.getId(), "Direct save for SKU: " + saved.getSku());
        return saved;
    }

    @Transactional
    public ProductDTO create(ProductDTO dto) {
        if (productRepository.existsBySku(dto.getSku())) {
            throw new BadRequestException("Product with SKU '" + dto.getSku() + "' already exists");
        }

        Product product = new Product();
        mapDtoToEntity(dto, product);
        Product saved = productRepository.save(product);
        auditService.log("CREATE_PRODUCT", "Product", saved.getId(),
            "Created product SKU: " + saved.getSku() + ", name: " + saved.getName());
        return toDTO(saved);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        Product existing = findEntityById(id);

        if (!existing.getSku().equalsIgnoreCase(dto.getSku()) && productRepository.existsBySku(dto.getSku())) {
            throw new BadRequestException("Product with SKU '" + dto.getSku() + "' already exists");
        }

        mapDtoToEntity(dto, existing);
        Product saved = productRepository.save(existing);
        auditService.log("UPDATE_PRODUCT", "Product", saved.getId(),
            "Updated product SKU: " + saved.getSku());
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Product existing = findEntityById(id);
        productRepository.delete(existing);
        auditService.log("DELETE_PRODUCT", "Product", id,
            "Deleted product SKU: " + existing.getSku() + ", name: " + existing.getName());
    }

    public String exportToCsv() {
        List<Product> products = productRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,SKU,Name,Barcode,RFID_Tag,Description,Price,Cost_Price,Category,Active\n");

        for (Product p : products) {
            sb.append(p.getId() != null ? p.getId() : "").append(",");
            sb.append(escapeCsv(p.getSku())).append(",");
            sb.append(escapeCsv(p.getName())).append(",");
            sb.append(escapeCsv(p.getBarcode())).append(",");
            sb.append(escapeCsv(p.getRfidTag())).append(",");
            sb.append(escapeCsv(p.getDescription())).append(",");
            sb.append(p.getPrice() != null ? p.getPrice() : "").append(",");
            sb.append(p.getCostPrice() != null ? p.getCostPrice() : "").append(",");
            sb.append(escapeCsv(p.getCategory() != null ? p.getCategory().getName() : "")).append(",");
            sb.append(p.isActive()).append("\n");
        }

        return sb.toString();
    }

    @Transactional
    public int importFromCsv(InputStream inputStream) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (isFirstLine) {
                    isFirstLine = false;
                    if (line.toLowerCase().startsWith("id,sku") || line.toLowerCase().startsWith("sku,")) {
                        continue;
                    }
                }

                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (parts.length < 7) {
                    continue;
                }

                int offset = parts.length >= 10 ? 1 : 0;
                String sku = unescapeCsv(parts[offset]);
                String name = unescapeCsv(parts[offset + 1]);
                String barcode = unescapeCsv(parts[offset + 2]);
                String rfidTag = unescapeCsv(parts[offset + 3]);
                String description = unescapeCsv(parts[offset + 4]);
                BigDecimal price = new BigDecimal(unescapeCsv(parts[offset + 5]));
                BigDecimal costPrice = parts[offset + 6].trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(unescapeCsv(parts[offset + 6]));
                String categoryName = parts.length > offset + 7 ? unescapeCsv(parts[offset + 7]) : "";

                Category category = null;
                if (!categoryName.isEmpty()) {
                    category = categoryRepository.findByName(categoryName)
                        .orElseGet(() -> categoryRepository.save(new Category(categoryName, "Auto-created during CSV import")));
                }

                Optional<Product> optProduct = productRepository.findBySku(sku);
                Product product;
                if (optProduct.isPresent()) {
                    product = optProduct.get();
                } else {
                    product = new Product();
                    product.setSku(sku);
                }
                product.setName(name);
                product.setBarcode(barcode.isEmpty() ? null : barcode);
                product.setRfidTag(rfidTag.isEmpty() ? null : rfidTag);
                product.setDescription(description);
                product.setPrice(price);
                product.setCostPrice(costPrice);
                if (category != null) {
                    product.setCategory(category);
                }
                product.setActive(true);

                productRepository.save(product);
                count++;
            }

            auditService.log("BULK_CSV_IMPORT", "Product", null, "Imported " + count + " products from CSV");
            return count;
        } catch (Exception e) {
            throw new BadRequestException("Failed to process CSV import: " + e.getMessage());
        }
    }

    private void mapDtoToEntity(ProductDTO dto, Product entity) {
        entity.setSku(dto.getSku());
        entity.setName(dto.getName());
        entity.setBarcode(dto.getBarcode());
        entity.setRfidTag(dto.getRfidTag());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setCostPrice(dto.getCostPrice());
        entity.setActive(dto.isActive());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));
            entity.setCategory(category);
        } else if (dto.getCategoryName() != null && !dto.getCategoryName().trim().isEmpty()) {
            Category category = categoryRepository.findByName(dto.getCategoryName().trim())
                .orElseGet(() -> categoryRepository.save(new Category(dto.getCategoryName().trim(), "Auto-created category")));
            entity.setCategory(category);
        }
    }

    public ProductDTO toDTO(Product p) {
        return new ProductDTO(
            p.getId(),
            p.getSku(),
            p.getName(),
            p.getBarcode(),
            p.getRfidTag(),
            p.getDescription(),
            p.getPrice(),
            p.getCostPrice(),
            p.getCategory() != null ? p.getCategory().getId() : null,
            p.getCategory() != null ? p.getCategory().getName() : null,
            p.isActive()
        );
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String unescapeCsv(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).replace("\"\"", "\"");
        }
        return trimmed;
    }
}
