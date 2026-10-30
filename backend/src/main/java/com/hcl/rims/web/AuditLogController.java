package com.hcl.rims.web;

import com.hcl.rims.model.AuditLog;
import com.hcl.rims.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs(@RequestParam(required = false) String entity) {
        if (entity != null && !entity.isBlank()) {
            return ResponseEntity.ok(auditService.findByEntity(entity));
        }
        return ResponseEntity.ok(auditService.findAll());
    }
}
