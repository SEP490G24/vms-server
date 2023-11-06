package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.UUID;


public interface IAuditLogService extends IGenericService<AuditLog, UUID> {

    void logAudit(Constants.AuditType auditType
        , String siteId
        , String organizationId
        , String primaryKey
        , String tableName
        , String oldValue
        , String newValue);
}
