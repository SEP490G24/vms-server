package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IAuditLogService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogServiceImpl extends GenericServiceImpl<AuditLog, UUID> implements IAuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final SiteRepository siteRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, SiteRepository siteRepository) {
        this.auditLogRepository = auditLogRepository;
        this.siteRepository = siteRepository;
        this.init(auditLogRepository);
    }

    @Override
    public Page<IAuditLogController.AuditLogFilterDTO> filter(Pageable pageable, List<String> organizations, List<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createdBy, String tableName, String keyword) {

        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));

        if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
            List<String> siteList = SecurityUtils.getListSiteToString(siteRepository, sites);
            return auditLogRepository.filter(pageableSort, null, siteList, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);
        } else if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            return auditLogRepository.filter(pageableSort, organizations, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);
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

}
