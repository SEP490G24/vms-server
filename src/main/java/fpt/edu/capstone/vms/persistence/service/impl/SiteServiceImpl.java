package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMap;
import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMapPk;
import fpt.edu.capstone.vms.persistence.repository.SiteDepartmentMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ISiteService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SiteServiceImpl extends GenericServiceImpl<Site, UUID> implements ISiteService {

    private final SiteRepository siteRepository;
    private final SiteDepartmentMapRepository siteDepartmentMapRepository;
    private final ModelMapper mapper;

    public SiteServiceImpl(SiteRepository siteRepository, SiteDepartmentMapRepository siteDepartmentMapRepository, ModelMapper mapper) {
        this.siteRepository = siteRepository;
        this.siteDepartmentMapRepository = siteDepartmentMapRepository;
        this.mapper = mapper;
        this.init(siteRepository);
    }

    @Override
    public Site save(Site entity) {
        try {
            if (StringUtils.isEmpty(entity.getOrganizationId().toString())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "OrganizationId is null");
            }
            entity.setEnable(true);
            return siteRepository.save(entity);
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(), e.getMessage());
        }
    }

    @Override
    public Site updateSite(ISiteController.UpdateSiteInfo updateSite, UUID id) {
        var siteEntity = siteRepository.findById(id).orElse(null);
        var update = mapper.map(updateSite, Site.class);
        if (ObjectUtils.isEmpty(siteEntity))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found site by id: " + id);
        if (!UUID.fromString(SecurityUtils.getOrgId()).equals(siteEntity.getOrganizationId())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The current user is not in organization with organizationId = " +siteEntity.getOrganization());
        }
        siteRepository.save(siteEntity.update(update));
        if (updateSite.getDepartmentId() != null) {
            SiteDepartmentMap siteDepartmentMap = new SiteDepartmentMap();
            SiteDepartmentMapPk pk = new SiteDepartmentMapPk(UUID.fromString(updateSite.getDepartmentId()), siteEntity.getId());
            siteDepartmentMap.setId(pk);
            siteDepartmentMapRepository.save(siteDepartmentMap);
        }
        return siteEntity;
    }

    @Override
    public Page<Site> filter(int pageNumber, List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return siteRepository.filter(
            PageRequest.of(pageNumber, Constants.PAGE_SIZE),
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }

}
