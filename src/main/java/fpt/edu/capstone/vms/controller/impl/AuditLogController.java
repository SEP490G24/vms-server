package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.service.impl.AuditLogServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(auditLogService.findAll());
    }

}
