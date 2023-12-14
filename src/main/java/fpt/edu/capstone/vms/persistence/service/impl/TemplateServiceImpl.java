package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ITemplateController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingSiteMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.service.ITemplateService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TemplateServiceImpl extends GenericServiceImpl<Template, UUID> implements ITemplateService {

    private final TemplateRepository templateRepository;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;
    private static final String TEMPLATE_TABLE_NAME = "Template";
    private final AuditLogRepository auditLogRepository;

    private final SettingSiteMapRepository settingSiteMapRepository;

    public TemplateServiceImpl(TemplateRepository templateRepository, SiteRepository siteRepository, ModelMapper mapper, AuditLogRepository auditLogRepository, SettingSiteMapRepository settingSiteMapRepository) {
        this.templateRepository = templateRepository;
        this.siteRepository = siteRepository;
        this.mapper = mapper;
        this.auditLogRepository = auditLogRepository;
        this.settingSiteMapRepository = settingSiteMapRepository;
        this.init(templateRepository);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Template update(Template templateInfo, UUID id) {
        var template = templateRepository.findById(id).orElse(null);
        var oldTemplate = template;
        if (ObjectUtils.isEmpty(template))
            throw new CustomException(ErrorApp.TEMPLATE_NOT_FOUND);

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, template.getSiteId().toString())) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }

        var site = siteRepository.findById(template.getSiteId()).orElse(null);
        var templateUpdate = templateRepository.save(template.update(templateInfo));
        if (templateUpdate.getEnable() == false) {
            var settingSite = settingSiteMapRepository.findByValue(templateUpdate.getId().toString());
            if (settingSite != null) {
                settingSiteMapRepository.delete(settingSite);
            }
        }
        auditLogRepository.save(new AuditLog(template.getSiteId().toString()
            , site.getOrganizationId().toString()
            , template.getId().toString()
            , TEMPLATE_TABLE_NAME
            , Constants.AuditType.UPDATE
            , oldTemplate.toString()
            , template.toString()));
        return template;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Template create(ITemplateController.TemplateDto templateDto) {
        if (ObjectUtils.isEmpty(templateDto))
            throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);
        if (SecurityUtils.getOrgId() != null) {
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, templateDto.getSiteId().toString())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
        } else {
            templateDto.setSiteId(UUID.fromString(SecurityUtils.getSiteId()));
        }

        if (SecurityUtils.getUserDetails().isOrganizationAdmin()) {
            if (templateRepository.existsByCodeAndSiteId(templateDto.getCode(), templateDto.getSiteId())) {
                throw new CustomException(ErrorApp.TEMPLATE_DUPLICATE);
            }
        } else if (SecurityUtils.getUserDetails().isSiteAdmin()) {
            if (templateRepository.existsByCodeAndSiteId(templateDto.getCode(), UUID.fromString(SecurityUtils.getSiteId()))) {
                throw new CustomException(ErrorApp.TEMPLATE_DUPLICATE);
            }
        }

        var site = siteRepository.findById(templateDto.getSiteId()).orElse(null);
        var template = mapper.map(templateDto, Template.class);
        template.setEnable(true);
        var templateNew = templateRepository.save(template);
        auditLogRepository.save(new AuditLog(template.getSiteId().toString()
            , site.getOrganizationId().toString()
            , templateNew.getId().toString()
            , TEMPLATE_TABLE_NAME
            , Constants.AuditType.CREATE
            , null
            , templateNew.toString()));
        return template;
    }

    @Override
    public Page<Template> filter(Pageable pageable, List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, Constants.TemplateType type, String keyword) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteId);
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return templateRepository.filter(
            pageableSort,
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            enable,
            type,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Template> filter(List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, Constants.TemplateType type, String keyword) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteId);
        return templateRepository.filter(
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            enable,
            type,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Template> finAllBySiteId(String siteId) {
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        return templateRepository.findAllBySiteIdAndEnableIsTrue(UUID.fromString(siteId));
    }


    @Override
    public List<Template> finAllBySiteIdAndType(String siteId, Constants.TemplateType type) {
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        return templateRepository.findAllBySiteIdAndEnableIsTrueAndType(UUID.fromString(siteId), type);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public void deleteTemplate(UUID id) {
        var template = templateRepository.findById(id).orElse(null);
        if (template == null) {
            throw new CustomException(ErrorApp.TEMPLATE_ERROR_IN_PROCESS_DELETE);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, template.getSiteId().toString())) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        if (settingSiteMapRepository.findByValue(template.getId().toString()) != null) {
            throw new CustomException(ErrorApp.TEMPLATE_IS_USING_CAN_NOT_DELETE);
        }
        var site = siteRepository.findById(template.getSiteId()).orElse(null);
        auditLogRepository.save(new AuditLog(site.getId().toString()
            , site.getOrganizationId().toString()
            , template.getId().toString()
            , TEMPLATE_TABLE_NAME
            , Constants.AuditType.DELETE
            , template.toString()
            , null));
        templateRepository.delete(template);
    }
}
