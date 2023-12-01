package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Audit Log Service")
@RequestMapping("/api/v1/audit-log")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IAuditLogController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:audit-log:detail')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
    @PreAuthorize("hasRole('r:audit-log:filter')")
    ResponseEntity<?> filter(@RequestBody AuditLogFilter auditLogFilter, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @PostMapping("/export")
    @Operation(summary = "Export audit log")
    @PreAuthorize("hasRole('r:audit-log:export')")
    ResponseEntity<?> export(@RequestBody AuditLogFilter auditLogFilter) throws JRException;

    @Data
    class AuditLogFilterDTO {
        private UUID id;
        private String code;
        private String siteId;
        private String siteName;
        private String organizationId;
        private String organizationName;
        private String primaryKey;
        private String tableName;
        private String auditType;
        private String oldValue;
        private String newValue;
        private String createBy;
        private Date createOn;
        private String lastUpdatedBy;
        private Date lastUpdatedOn;
    }

    @Data
    class AuditLogFilter {
        private List<String> siteId;
        private List<String> organizationId;
        private String tableName;
        private Constants.AuditType auditType;
        private String createBy;
        private LocalDateTime createdOnStart;
        private LocalDateTime createdOnEnd;
        private String keyword;
    }
}
