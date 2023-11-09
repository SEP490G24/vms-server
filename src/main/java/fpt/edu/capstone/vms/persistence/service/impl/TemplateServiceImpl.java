package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ITemplateController;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.service.ITemplateService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TemplateServiceImpl extends GenericServiceImpl<Template, UUID> implements ITemplateService {

    private final TemplateRepository templateRepository;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;


    public TemplateServiceImpl(TemplateRepository templateRepository, SiteRepository siteRepository, ModelMapper mapper) {
        this.templateRepository = templateRepository;
        this.siteRepository = siteRepository;
        this.mapper = mapper;
        this.init(templateRepository);
    }

    @Override
    public Template update(Template templateInfo, UUID id) {
        var template = templateRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(template))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found template");
        templateRepository.save(template.update(templateInfo));
        return template;
    }

    @Override
    @Transactional
    public Template create(ITemplateController.TemplateDto templateDto) {
        if (ObjectUtils.isEmpty(templateDto))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Object is empty");
        if (StringUtils.isEmpty(templateDto.getSiteId().toString()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");
        var template = mapper.map(templateDto, Template.class);
        template.setEnable(true);
        templateRepository.save(template);
        return template;
    }

    @Override
    public Page<Template> filter(Pageable pageable, List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        List<UUID> sites = getListSite(siteId);
        return templateRepository.filter(
            pageable,
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Template> filter(List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        List<UUID> sites = getListSite(siteId);
        return templateRepository.filter(
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null);
    }

    private List<UUID> getListSite(List<String> siteId) {

        if (SecurityUtils.getOrgId() == null && siteId != null) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this.");
        }
        List<UUID> sites = new ArrayList<>();
        if (SecurityUtils.getOrgId() != null) {
            if (siteId == null) {
                siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId())).forEach(o -> {
                    sites.add(o.getId());
                });
            } else {
                siteId.forEach(o -> {
                    if (!SecurityUtils.checkSiteAuthorization(siteRepository, o)) {
                        throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this.");
                    }
                    sites.add(UUID.fromString(o));
                });
            }
        } else {
            sites.add(UUID.fromString(SecurityUtils.getSiteId()));
        }

        return sites;
    }
}
