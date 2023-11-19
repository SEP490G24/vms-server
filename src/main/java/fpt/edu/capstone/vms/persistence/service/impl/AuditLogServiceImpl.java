package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IAuditLogService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogServiceImpl extends GenericServiceImpl<AuditLog, UUID> implements IAuditLogService {

    static final String PATH_FILE = "/jasper/audit-log.jrxml";
    private final AuditLogRepository auditLogRepository;
    private final SiteRepository siteRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, SiteRepository siteRepository) {
        this.auditLogRepository = auditLogRepository;
        this.siteRepository = siteRepository;
        this.init(auditLogRepository);
    }

    @Override
    public Page<IAuditLogController.AuditLogFilterDTO> filter(Pageable pageable, List<String> organizations, List<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createdBy, String tableName, String keyword) {

        if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
            List<String> siteList = SecurityUtils.getListSiteToString(siteRepository, sites);
            return auditLogRepository.filter(pageable, null, siteList, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);
        } else if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            return auditLogRepository.filter(pageable, organizations, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);
        } else {
            return null;
        }
    }

    @Override
    public List<IAuditLogController.AuditLogFilterDTO> filter(List<String> organizations, List<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createdBy, String tableName, String keyword) {
        if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
            List<String> siteList = SecurityUtils.getListSiteToString(siteRepository, sites);
            return auditLogRepository.filter(null, siteList, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);
        } else if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            return auditLogRepository.filter(organizations, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);
        } else {
            return null;
        }
    }

    @Override
    public ByteArrayResource export(IAuditLogController.AuditLogFilter auditLogFilter) throws JRException {
        Pageable pageable = PageRequest.of(0, 99999);
        Page<IAuditLogController.AuditLogFilterDTO> listData = filter(pageable, auditLogFilter.getOrganizationId(),
            auditLogFilter.getSiteId(), auditLogFilter.getAuditType(), auditLogFilter.getCreatedOnStart(), auditLogFilter.getCreatedOnEnd(),
            auditLogFilter.getCreateBy(), auditLogFilter.getTableName(), auditLogFilter.getKeyword());
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream(PATH_FILE));

            JRBeanCollectionDataSource listDataSource = new JRBeanCollectionDataSource(
                listData.getContent().size() == 0 ? Collections.singletonList(new IAuditLogController.AuditLogFilterDTO()) : listData.getContent());
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("tableDataset", listDataSource);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
            exporter.exportReport();

            byte[] excelBytes = byteArrayOutputStream.toByteArray();
            return new ByteArrayResource(excelBytes);
        } catch (Exception e) {
            return null;
        }
    }

}
