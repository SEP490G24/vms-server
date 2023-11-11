package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AuditLogRepositoryCustom {

    Page<IAuditLogController.AuditLogFilterDTO> filter(Pageable pageable,
                                                       @Param("organizations") @Nullable Collection<String> organizations,
                                                       @Param("sites") @Nullable Collection<String> sites,
                                                       @Param("auditType") @Nullable Constants.AuditType auditType,
                                                       @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                                                       @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                                                       @Param("createdBy") @Nullable String createdBy,
                                                       @Param("tableName") @Nullable String tableName,
                                                       @Param("keyword") @Nullable String keyword);

    List<IAuditLogController.AuditLogFilterDTO> filter(@Param("organizations") @Nullable Collection<String> organizations,
                                                       @Param("sites") @Nullable Collection<String> sites,
                                                       @Param("auditType") @Nullable Constants.AuditType auditType,
                                                       @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                                                       @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                                                       @Param("createdBy") @Nullable String createdBy,
                                                       @Param("tableName") @Nullable String tableName,
                                                       @Param("keyword") @Nullable String keyword);
}
