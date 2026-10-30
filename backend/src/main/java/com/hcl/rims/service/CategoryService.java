package com.hcl.rims.service;

import com.hcl.rims.dto.CategoryDTO;
import com.hcl.rims.exception.BadRequestException;
import com.hcl.rims.exception.ResourceNotFoundException;
import com.hcl.rims.model.Category;
import com.hcl.rims.repo.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    public CategoryService(CategoryRepository categoryRepository, AuditService auditService) {
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
    }

    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public CategoryDTO findById(Long id) {
        return categoryRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    public Category findEntityById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
        }
        Category category = new Category(dto.getName(), dto.getDescription());
        Category saved = categoryRepository.save(category);
        auditService.log("CREATE_CATEGORY", "Category", saved.getId(), "Created category: " + saved.getName());
        return toDTO(saved);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category existing = findEntityById(id);
        if (!existing.getName().equalsIgnoreCase(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
        }
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        Category saved = categoryRepository.save(existing);
        auditService.log("UPDATE_CATEGORY", "Category", saved.getId(), "Updated category: " + saved.getName());
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Category existing = findEntityById(id);
        categoryRepository.delete(existing);
        auditService.log("DELETE_CATEGORY", "Category", id, "Deleted category: " + existing.getName());
    }

    public CategoryDTO toDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getDescription());
    }
}
