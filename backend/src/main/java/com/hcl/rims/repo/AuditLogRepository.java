package com.hcl.rims.repo;

import com.hcl.rims.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName);
    List<AuditLog> findAllByOrderByTimestampDesc();
}
