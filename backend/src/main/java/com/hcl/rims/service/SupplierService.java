package com.hcl.rims.service;

import com.hcl.rims.dto.SupplierDTO;
import com.hcl.rims.dto.SupplierPerformanceDTO;
import com.hcl.rims.exception.BadRequestException;
import com.hcl.rims.exception.ResourceNotFoundException;
import com.hcl.rims.model.PurchaseOrder;
import com.hcl.rims.model.Supplier;
import com.hcl.rims.repo.PurchaseOrderRepository;
import com.hcl.rims.repo.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AuditService auditService;

    public SupplierService(SupplierRepository supplierRepository,
                           PurchaseOrderRepository purchaseOrderRepository,
                           AuditService auditService) {
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.auditService = auditService;
    }

    public List<SupplierDTO> findAll() {
        return supplierRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public SupplierDTO findById(Long id) {
        return supplierRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
    }

    public Supplier findEntityById(Long id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
    }

    @Transactional
    public SupplierDTO create(SupplierDTO dto) {
        if (supplierRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Supplier with name '" + dto.getName() + "' already exists");
        }

        Supplier supplier = new Supplier(
            dto.getName(),
            dto.getContactPerson(),
            dto.getEmail(),
            dto.getPhone(),
            dto.getAddress(),
            dto.getCity(),
            dto.getState()
        );
        Supplier saved = supplierRepository.save(supplier);
        auditService.log("CREATE_SUPPLIER", "Supplier", saved.getId(), "Created supplier: " + saved.getName());
        return toDTO(saved);
    }

    @Transactional
    public SupplierDTO update(Long id, SupplierDTO dto) {
        Supplier supplier = findEntityById(id);
        if (!supplier.getName().equalsIgnoreCase(dto.getName()) && supplierRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Supplier with name '" + dto.getName() + "' already exists");
        }

        supplier.setName(dto.getName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setCity(dto.getCity());
        supplier.setState(dto.getState());

        Supplier saved = supplierRepository.save(supplier);
        auditService.log("UPDATE_SUPPLIER", "Supplier", saved.getId(), "Updated supplier: " + saved.getName());
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = findEntityById(id);
        supplierRepository.delete(supplier);
        auditService.log("DELETE_SUPPLIER", "Supplier", id, "Deleted supplier: " + supplier.getName());
    }

    public SupplierPerformanceDTO getPerformance(Long supplierId) {
        Supplier supplier = findEntityById(supplierId);
        List<PurchaseOrder> pos = purchaseOrderRepository.findBySupplierId(supplierId);

        long totalPOs = pos.size();
        List<PurchaseOrder> receivedList = pos.stream()
            .filter(po -> po.getStatus() == PurchaseOrder.POStatus.RECEIVED)
            .toList();

        long receivedPOs = receivedList.size();
        long onTimePOs = receivedList.stream()
            .filter(po -> {
                if (po.getExpectedDate() == null || po.getReceivedDate() == null) {
                    return true;
                }
                return !po.getReceivedDate().isAfter(po.getExpectedDate());
            })
            .count();

        double onTimeRate = (receivedPOs > 0)
            ? Math.round(((double) onTimePOs / receivedPOs) * 1000.0) / 10.0
            : (totalPOs > 0 ? 0.0 : 100.0);

        return new SupplierPerformanceDTO(
            supplier.getId(),
            supplier.getName(),
            totalPOs,
            receivedPOs,
            onTimePOs,
            onTimeRate
        );
    }

    public List<SupplierPerformanceDTO> getAllPerformance() {
        return supplierRepository.findAll().stream()
            .map(s -> getPerformance(s.getId()))
            .collect(Collectors.toList());
    }

    public SupplierDTO toDTO(Supplier s) {
        return new SupplierDTO(
            s.getId(),
            s.getName(),
            s.getContactPerson(),
            s.getEmail(),
            s.getPhone(),
            s.getAddress(),
            s.getCity(),
            s.getState()
        );
    }
}
