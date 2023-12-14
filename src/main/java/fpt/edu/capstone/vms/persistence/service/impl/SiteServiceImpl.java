package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
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
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private static final String SITE_SETTING_MAP_TABLE_NAME = "SettingSiteMap";


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
        if (StringUtils.isEmpty(entity.getCode())) {
            throw new CustomException(ErrorApp.SITE_CODE_NULL);
        }
        if (siteRepository.existsByCodeAndOrganizationId(entity.getCode(), UUID.fromString(SecurityUtils.getOrgId()))) {
            throw new CustomException(ErrorApp.SITE_CODE_EXIST);
        }

        checkAddress(entity.getProvinceId(), entity.getDistrictId(), entity.getCommuneId());
        entity.setOrganizationId(UUID.fromString(SecurityUtils.getOrgId()));
        var site = siteRepository.save(entity);
        site.setEnable(true);
        auditLogRepository.save(new AuditLog(site.getId().toString()
            , site.getOrganizationId().toString()
            , site.getId().toString()
            , SITE_TABLE_NAME
            , Constants.AuditType.CREATE
            , null
            , site.toString()));
        return site;
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

        var siteEntity = siteRepository.findById(id).orElse(null);
        var update = mapper.map(updateSite, Site.class);
        if (ObjectUtils.isEmpty(siteEntity))
            throw new CustomException(ErrorApp.SITE_NOT_FOUND);

        if (SecurityUtils.getOrgId() != null) {
            if (!UUID.fromString(SecurityUtils.getOrgId()).equals(siteEntity.getOrganizationId())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
        } else {
            if (!SecurityUtils.getSiteId().equals(siteEntity.getId().toString())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
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
    public Page<Site> filter(Pageable pageable
        , List<String> names
        , LocalDateTime createdOnStart
        , LocalDateTime createdOnEnd
        , String createBy
        , String lastUpdatedBy
        , Boolean enable
        , Integer provinceId
        , Integer districtId
        , Integer communeId
        , String keyword) {
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return siteRepository.filter(
            pageableSort,
            names,
            UUID.fromString(SecurityUtils.getOrgId()),
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            provinceId,
            districtId,
            communeId,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Site> filter(List<String> names
        , LocalDateTime createdOnStart
        , LocalDateTime createdOnEnd
        , String createdBy
        , String lastUpdatedBy
        , Boolean enable
        , Integer provinceId
        , Integer districtId
        , Integer communeId
        , String keyword) {
        return siteRepository.filter(
            names,
            UUID.fromString(SecurityUtils.getOrgId()),
            createdOnStart,
            createdOnEnd,
            createdBy,
            lastUpdatedBy,
            enable,
            provinceId,
            districtId,
            communeId,
            keyword != null ? keyword.toUpperCase() : null);
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
            throw new CustomException(ErrorApp.SITE_NOT_FOUND);

        if (SecurityUtils.getOrgId() != null) {
            if (!UUID.fromString(SecurityUtils.getOrgId()).equals(siteEntity.getOrganizationId())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
        } else {
            if (!SecurityUtils.getSiteId().equals(siteEntity.getId().toString())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
        }

        auditLogRepository.save(new AuditLog(siteEntity.getId().toString()
            , siteEntity.getOrganizationId().toString()
            , siteEntity.getId().toString()
            , SITE_TABLE_NAME
            , Constants.AuditType.DELETE
            , siteEntity.toString()
            , null));
        deleteSiteSettingMap(siteEntity);
        siteRepository.delete(siteEntity);
        return true;
    }

    public void deleteSiteSettingMap(Site site) {
        var siteSettingMaps = settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(site.getId());
        if (siteSettingMaps != null) {
            siteSettingMaps.forEach(o -> {
                auditLogRepository.save(new AuditLog(site.getId().toString()
                    , site.getOrganizationId().toString()
                    , o.getId().toString()
                    , SITE_SETTING_MAP_TABLE_NAME
                    , Constants.AuditType.DELETE
                    , o.toString()
                    , null));
                settingSiteMapRepository.delete(o);
            });
        }
    }

    @Override
    public Site findById(String id) {
        String siteId = "";
        if (SecurityUtils.getOrgId() != null) {
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, id)) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
            siteId = id;
        } else {
            siteId = SecurityUtils.getSiteId();
        }
        Site site = siteRepository.findById(UUID.fromString(siteId)).orElse(null);
        return site;
    }

    public void checkAddress(Integer provinceId, Integer districtId, Integer communeId) {

        if (ObjectUtils.isEmpty(provinceId)) {
            throw new CustomException(ErrorApp.PROVINCE_NULL);
        }
        if (ObjectUtils.isEmpty(districtId)) {
            throw new CustomException(ErrorApp.DISTRICT_NULL);
        }
        if (ObjectUtils.isEmpty(communeId)) {
            throw new CustomException(ErrorApp.COMMUNE_NULL);
        }

        var province = provinceRepository.findById(provinceId).orElse(null);

        if (ObjectUtils.isEmpty(province)) {
            throw new CustomException(ErrorApp.PROVINCE_NOT_FOUND);
        }

        var district = districtRepository.findById(districtId).orElse(null);

        if (ObjectUtils.isEmpty(district)) {
            throw new CustomException(ErrorApp.DISTRICT_NOT_FOUND);
        }

        if (!Objects.equals(district.getProvinceId(), province.getId())) {
            throw new CustomException(ErrorApp.DISTRICT_NOT_FOUND_BY_PROVINCE);
        }

        var commune = communeRepository.findById(communeId).orElse(null);

        if (ObjectUtils.isEmpty(commune)) {
            throw new CustomException(ErrorApp.COMMUNE_NOT_FOUND);
        }

        if (!Objects.equals(commune.getDistrictId(), district.getId())) {
            throw new CustomException(ErrorApp.COMMUNE_NOT_FOUND_BY_DISTRICT);
        }
    }



}
