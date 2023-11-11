package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.service.IAuditLogService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogServiceImpl extends GenericServiceImpl<AuditLog, UUID> implements IAuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.init(auditLogRepository);
    }

    @Override
    public Page<IAuditLogController.AuditLogFilterDTO> filter(Pageable pageable, List<String> organizations, List<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createdBy, String tableName, String keyword) {
        return auditLogRepository.filter(pageable, organizations, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);
    }

    @Override
    public List<IAuditLogController.AuditLogFilterDTO> filter(List<String> organizations, List<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createdBy, String tableName, String keyword) {
        return auditLogRepository.filter(organizations, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);
    }
}
