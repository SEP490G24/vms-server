package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.CommuneRepository;
import fpt.edu.capstone.vms.persistence.repository.DistrictRepository;
import fpt.edu.capstone.vms.persistence.repository.ProvinceRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingSiteMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ISiteService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SiteServiceImpl extends GenericServiceImpl<Site, UUID> implements ISiteService {

    private final SiteRepository siteRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final SettingSiteMapRepository settingSiteMapRepository;
    private final SettingRepository settingRepository;
    private final AuditLogRepository auditLogRepository;
    private final ModelMapper mapper;

    private static final String SITE_TABLE_NAME = "Site";

    public SiteServiceImpl(SiteRepository siteRepository
        , ProvinceRepository provinceRepository
        , DistrictRepository districtRepository
        , CommuneRepository communeRepository
        , SettingSiteMapRepository settingSiteMapRepository
        , SettingRepository settingRepository, AuditLogRepository auditLogRepository, ModelMapper mapper) {
        this.siteRepository = siteRepository;
        this.provinceRepository = provinceRepository;
        this.districtRepository = districtRepository;
        this.communeRepository = communeRepository;
        this.settingSiteMapRepository = settingSiteMapRepository;
        this.settingRepository = settingRepository;
        this.auditLogRepository = auditLogRepository;
        this.mapper = mapper;
        this.init(siteRepository);
    }

    /**
     * The `save` function in Java is used to save a `Site` entity, performing various checks and validations before saving
     * it to the database.
     *
     * @param entity The `entity` parameter is an object of type `Site` that represents the site to be saved.
     * @return The method is returning a Site object.
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Site save(Site entity) {
        try {
            if (StringUtils.isEmpty(entity.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code is null");
            }
            if (siteRepository.existsByCode(entity.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of site is exist");
            }
            if (StringUtils.isEmpty(SecurityUtils.getOrgId())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "OrganizationId is null");
            }

            checkAddress(entity.getProvinceId(), entity.getDistrictId(), entity.getCommuneId());
            entity.setOrganizationId(UUID.fromString(SecurityUtils.getOrgId()));
            entity.setEnable(true);
            var site = siteRepository.save(entity);
            auditLogRepository.save(new AuditLog(site.getId().toString()
                , site.getOrganizationId().toString()
                , site.getId().toString()
                , SITE_TABLE_NAME
                , Constants.AuditType.CREATE
                , null
                , site.toString()));
//            if (!ObjectUtils.isEmpty(site)) addSettingForSite(site.getId());
            return site;
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(e.getStatusCode(), e.getMessage());
        }
    }

    /**
     * The function updates a site's information and returns the updated site entity.
     *
     * @param updateSite The `updateSite` parameter is an object of type `ISiteController.UpdateSiteInfo`. It contains
     * information about the site that needs to be updated, such as the code, name, and address.
     * @param id The `id` parameter is the unique identifier of the site that needs to be updated.
     * @return The method is returning a Site object.
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Site updateSite(ISiteController.UpdateSiteInfo updateSite, UUID id) {

        if (!StringUtils.isEmpty(updateSite.getCode())) {
            if (siteRepository.existsByCode(updateSite.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of site is exist");
            }
        }

        var siteEntity = siteRepository.findById(id).orElse(null);
        var update = mapper.map(updateSite, Site.class);
        if (ObjectUtils.isEmpty(siteEntity))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found site by id: " + id);

        if (SecurityUtils.getOrgId() != null) {
            if (!UUID.fromString(SecurityUtils.getOrgId()).equals(siteEntity.getOrganizationId())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The current user is not in organization with organizationId = " + siteEntity.getOrganization());
            }
        } else {
            if (StringUtils.isEmpty(SecurityUtils.getSiteId()))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The current user is not in site with siteId = " + SecurityUtils.getSiteId());

            if (!SecurityUtils.getSiteId().equals(siteEntity.getId().toString())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The current user is not in site with siteId = " + SecurityUtils.getSiteId());
            }
        }
        var updateS = siteRepository.save(siteEntity.update(update));
        auditLogRepository.save(new AuditLog(siteEntity.getId().toString()
            , siteEntity.getOrganizationId().toString()
            , siteEntity.getId().toString()
            , SITE_TABLE_NAME
            , Constants.AuditType.UPDATE
            , siteEntity.toString()
            , updateS.toString()));
        return siteEntity;
    }

    @Override
    public Page<Site> filter(Pageable pageable, List<String> names, UUID orgId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return siteRepository.filter(
            pageable,
            names,
            orgId,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }

    @Override
    public List<Site> filter(List<String> names, UUID orgId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createdBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return siteRepository.filter(
            names,
            orgId,
            createdOnStart,
            createdOnEnd,
            createdBy,
            lastUpdatedBy,
            enable,
            keyword);
    }

    @Override
    public List<Site> findAllByOrganizationId(String organizationId) {
        return siteRepository.findAllByOrganizationId(UUID.fromString(organizationId));
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Boolean deleteSite(UUID siteId) {
        var siteEntity = siteRepository.findById(siteId).orElse(null);
        if (ObjectUtils.isEmpty(siteEntity))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found site by id: " + siteId);

        if (SecurityUtils.getOrgId() != null) {
            if (!UUID.fromString(SecurityUtils.getOrgId()).equals(siteEntity.getOrganizationId())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The current user is not in organization with organizationId ");
            }
        } else {
            if (StringUtils.isEmpty(SecurityUtils.getSiteId()))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The current user is not in site with siteId = " + SecurityUtils.getSiteId());

            if (!SecurityUtils.getSiteId().equals(siteEntity.getId().toString())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The current user is not in site with siteId = " + SecurityUtils.getSiteId());
            }
        }

        auditLogRepository.save(new AuditLog(siteEntity.getId().toString()
            , siteEntity.getOrganizationId().toString()
            , siteEntity.getId().toString()
            , SITE_TABLE_NAME
            , Constants.AuditType.DELETE
            , siteEntity.toString()
            , null));
        siteRepository.delete(siteEntity);
        return true;
    }


    private void checkAddress(Integer provinceId, Integer districtId, Integer communeId) {

        if (ObjectUtils.isEmpty(provinceId)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Province is null");
        }
        if (ObjectUtils.isEmpty(districtId)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "District is null");
        }
        if (ObjectUtils.isEmpty(communeId)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Commune is null");
        }

        var province = provinceRepository.findById(provinceId).orElse(null);

        if (ObjectUtils.isEmpty(province)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found province");
        }

        var district = districtRepository.findById(districtId).orElse(null);

        if (ObjectUtils.isEmpty(district)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found district");
        }

        if (district.getProvinceId() != province.getId()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "the district not in province please check it again");
        }

        var commune = communeRepository.findById(communeId).orElse(null);

        if (ObjectUtils.isEmpty(commune)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found commune");
        }

        if (commune.getDistrictId() != district.getId()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "the commune not in district please check it again");
        }
    }

    private void addSettingForSite(UUID siteId) {
        var settings = settingRepository.findAll();
        if (!settings.isEmpty()) {
            settings.forEach(o -> {
                SettingSiteMapPk pk = new SettingSiteMapPk();
                pk.setSiteId(siteId);
                pk.setSettingId(o.getId());
                SettingSiteMap settingSiteMap = new SettingSiteMap();
                settingSiteMap.setSettingSiteMapPk(pk);
                settingSiteMap.setStatus(true);
                settingSiteMapRepository.save(settingSiteMap);
            });
        }
    }


}
