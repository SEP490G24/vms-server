package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IAuditLogService extends IGenericService<AuditLog, UUID> {

    Page<IAuditLogController.AuditLogFilterDTO> filter(Pageable pageable
        , List<String> organizations
        , List<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd
        , String createdBy, String tableName, String keyword);

    List<IAuditLogController.AuditLogFilterDTO> filter(List<String> organizations
        , List<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd
        , String createdBy, String tableName, String keyword);
}
