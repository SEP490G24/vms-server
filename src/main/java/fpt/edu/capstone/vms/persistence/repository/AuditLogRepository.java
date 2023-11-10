package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends GenericRepository<AuditLog, UUID>, AuditLogRepositoryCustom {
}
