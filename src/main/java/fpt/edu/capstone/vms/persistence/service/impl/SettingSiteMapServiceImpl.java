package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingSiteMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingSiteMapService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SettingSiteMapServiceImpl extends GenericServiceImpl<SettingSiteMap, SettingSiteMapPk> implements ISettingSiteMapService {

    private final SettingSiteMapRepository settingSiteMapRepository;
    private final SettingRepository settingRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ModelMapper mapper;
    private static final String SETTING_SITE_TABLE_NAME = "Setting Site Map";

    public SettingSiteMapServiceImpl(SettingSiteMapRepository settingSiteMapRepository, SettingRepository settingRepository, SiteRepository siteRepository, UserRepository userRepository, AuditLogRepository auditLogRepository, ModelMapper mapper) {
        this.settingSiteMapRepository = settingSiteMapRepository;
        this.settingRepository = settingRepository;
        this.siteRepository = siteRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.mapper = mapper;
        this.init(this.settingSiteMapRepository);
    }

    /**
     * The function creates or updates a setting-site mapping based on the provided setting and site information.
     *
     * @param settingSiteInfo The parameter `settingSiteInfo` is an object of type
     * `ISettingSiteMapController.SettingSiteInfo`. It contains information related to a setting site, such as the setting
     * ID, site ID, value, and description.
     * @return The method is returning a `SettingSiteMap` object.
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public SettingSiteMap createOrUpdateSettingSiteMap(ISettingSiteMapController.SettingSiteInfo settingSiteInfo) {

        var userDetails = SecurityUtils.getUserDetails();
        if (ObjectUtils.isEmpty(settingSiteInfo)) {
            throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);
        }
        if (settingSiteInfo.getSettingId() == null) {
            throw new CustomException(ErrorApp.SETTING_ID_NULL);
        }

        var _siteId = userDetails.isOrganizationAdmin() ? settingSiteInfo.getSiteId() : userDetails.getSiteId();
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, _siteId)) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }

        if (StringUtils.isEmpty(settingSiteInfo.getValue())) {
            throw new CustomException(ErrorApp.SETTING_VALUE_NULL);
        }

        Long settingId = Long.valueOf(settingSiteInfo.getSettingId());

        var site = siteRepository.findById(UUID.fromString(_siteId));
        if (site.isEmpty())
            throw new CustomException(ErrorApp.SITE_NOT_FOUND);

        if (!settingRepository.existsById(settingId))
            throw new CustomException(ErrorApp.SETTING_NOT_FOUND);

        SettingSiteMapPk pk = new SettingSiteMapPk(settingId, UUID.fromString(_siteId));
        SettingSiteMap settingSiteMap = settingSiteMapRepository.findById(pk).orElse(null);
        if (ObjectUtils.isEmpty(settingSiteMap)) {
            SettingSiteMap createSettingSite = new SettingSiteMap();
            createSettingSite.setSettingSiteMapPk(pk);
            createSettingSite.setValue(settingSiteInfo.getValue());
            createSettingSite.setDescription(settingSiteInfo.getDescription());
            createSettingSite.setStatus(true);

            auditLogRepository.save(new AuditLog(_siteId.toString()
                , site.get().getOrganizationId().toString()
                , pk.toString()
                , SETTING_SITE_TABLE_NAME
                , Constants.AuditType.CREATE
                , null
                , createSettingSite.toString()));
            return settingSiteMapRepository.save(createSettingSite);
        } else {
            var settingSiteUpdate = settingSiteMapRepository.save(settingSiteMap.update(mapper.map(settingSiteInfo, SettingSiteMap.class)));
            auditLogRepository.save(new AuditLog(_siteId.toString()
                , site.get().getOrganizationId().toString()
                , pk.toString()
                , SETTING_SITE_TABLE_NAME
                , Constants.AuditType.UPDATE
                , settingSiteInfo.toString()
                , settingSiteUpdate.toString()));
            return settingSiteUpdate;
        }
    }

    /**
     * The function retrieves setting sites based on the given site ID, group ID, and list of sites.
     *
     * @param settingGroupId The settingGroupId parameter is an Integer representing the ID of the setting group.
     * @param sites          A list of site names (strings)
     * @return The method is returning an instance of the class `ISettingSiteMapController.SettingSiteDTO`.
     */
    @Override
    public ISettingSiteMapController.SettingSiteDTO findAllBySiteIdAndGroupId(Integer settingGroupId, List<String> sites) {
        List<String> _sites = SecurityUtils.getListSiteToString(siteRepository, sites);
        ISettingSiteMapController.SettingSiteDTO settingSiteDTO = new ISettingSiteMapController.SettingSiteDTO();
        _sites.forEach(siteId -> {
            var settingSites = settingSiteMapRepository.findAllBySiteIdAndGroupId(siteId.trim(), settingGroupId);
            if (!settingSites.isEmpty()) {
                settingSiteDTO.setSiteId(siteId.trim());
                settingSiteDTO.setSettingGroupId(Long.valueOf(settingGroupId));
                Map<String, String> setting = new HashMap<>();
                settingSites.forEach(o -> {
                    if (StringUtils.isEmpty(o.getPropertyValue())) {
                        setting.put(o.getCode(), o.getDefaultPropertyValue());
                    } else {
                        setting.put(o.getCode(), o.getPropertyValue());
                    }
                });
                settingSiteDTO.setSettings(setting);
            }
        });
        return settingSiteDTO;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Boolean setDefaultValueBySite(String siteId) {

        var userDetails = SecurityUtils.getUserDetails();
        var _siteId = userDetails.isOrganizationAdmin() ? siteId : userDetails.getSiteId();

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, _siteId)) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }

        var site = siteRepository.findById(UUID.fromString(_siteId)).orElse(null);
        var settingSites = settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(UUID.fromString(_siteId));
        if (!settingSites.isEmpty()) {
            settingSites.forEach(o -> {
                settingSiteMapRepository.delete(o);
                auditLogRepository.save(new AuditLog(_siteId
                    , site.getOrganizationId().toString()
                    , o.getSettingSiteMapPk().toString()
                    , SETTING_SITE_TABLE_NAME
                    , Constants.AuditType.DELETE
                    , o.toString()
                    , null));
            });
            return true;
        }
        return false;
    }

    @Override
    public ISettingSiteMapController.SettingSiteMapDTO findBySiteIdAndCode(String siteId, String code) {
        ISettingSiteMapController.SettingSiteMapDTO settingSiteMapDTO = new ISettingSiteMapController.SettingSiteMapDTO();
        var userDetails = SecurityUtils.getUserDetails();
        var _siteId = userDetails.isOrganizationAdmin() ? siteId : userDetails.getSiteId();
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, _siteId)) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        var setting = settingRepository.findByCode(code);
        var settingSite = settingSiteMapRepository.findBySettingSiteMapPk_SiteIdAndSettingSiteMapPk_SettingId(UUID.fromString(_siteId), setting.getId());
        settingSiteMapDTO.setCode(code);
        if (settingSite != null) {
            settingSiteMapDTO.setValue(settingSite.getValue());
        } else {
            settingSiteMapDTO.setValue(setting.getDefaultValue());
        }
        return settingSiteMapDTO;
    }


}
