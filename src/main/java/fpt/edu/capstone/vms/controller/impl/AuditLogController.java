package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.service.impl.AuditLogServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class AuditLogController implements IAuditLogController {
    private final AuditLogServiceImpl auditLogService;

    @Override
    public ResponseEntity<AuditLog> findById(UUID id) {
        return ResponseEntity.ok(auditLogService.findById(id));
    }

    @Override
    public ResponseEntity<AuditLog> delete(UUID id) {
        return auditLogService.delete(id);
    }

    @Override
    public ResponseEntity<?> filter(AuditLogFilter filter, boolean isPageable, Pageable pageable) {
        return isPageable ? ResponseEntity.ok(
            auditLogService.filter(
                pageable,
                filter.getOrganizationId(),
                filter.getSiteId(),
                filter.getAuditType(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getCreateBy(),
                filter.getTableName(),
                filter.getKeyword())) : ResponseEntity.ok(
            auditLogService.filter(
                filter.getOrganizationId(),
                filter.getSiteId(),
                filter.getAuditType(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getCreateBy(),
                filter.getTableName(),
                filter.getKeyword()));
    }

}
