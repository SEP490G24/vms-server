package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.excel.ExportAuditLog;
import fpt.edu.capstone.vms.persistence.service.impl.AuditLogServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AllArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.apache.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class AuditLogController implements IAuditLogController {
    private final AuditLogServiceImpl auditLogService;
    private final ExportAuditLog exportAuditLog;
    private final SiteRepository siteRepository;

    @Override
    public ResponseEntity<AuditLog> findById(UUID id) {
        var auditLog = auditLogService.findById(id);
        if (!SecurityUtils.checkOrganizationAuthor(siteRepository, auditLog.getOrganizationId())) {
            throw new HttpClientErrorException(org.springframework.http.HttpStatus.FORBIDDEN, "Not permission");
        }
        return ResponseEntity.ok(auditLog);
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

    @Override
    public ResponseEntity<?> export(AuditLogFilter auditLogFilter) throws JRException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "audit_log.xlsx");
        return ResponseEntity.status(HttpStatus.SC_OK).headers(headers).body(exportAuditLog.export(auditLogFilter));
    }
}
