package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.service.IAuditLogService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditLogServiceImpl extends GenericServiceImpl<AuditLog, UUID> implements IAuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.init(auditLogRepository);
    }

}
