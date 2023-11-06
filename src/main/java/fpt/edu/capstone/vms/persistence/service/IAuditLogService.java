package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.UUID;


public interface IAuditLogService extends IGenericService<AuditLog, UUID> {

}
