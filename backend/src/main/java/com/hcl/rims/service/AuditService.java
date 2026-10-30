package com.hcl.rims.service;

import com.hcl.rims.model.AuditLog;
import com.hcl.rims.repo.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLog log(String action, String entityName, Long entityId, String details) {
        String performedBy = "SYSTEM";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            performedBy = auth.getName();
        }

        AuditLog logEntry = new AuditLog(action, entityName, entityId, performedBy, details);
        return auditLogRepository.save(logEntry);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> findByEntity(String entityName) {
        return auditLogRepository.findByEntityNameOrderByTimestampDesc(entityName);
    }
}
