package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
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

        if (StringUtils.isEmpty(settingSiteInfo.getValue())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Value is empty");
        }

        if (ObjectUtils.isEmpty(settingSiteInfo)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is null");
        }

        if (settingSiteInfo.getSettingId() == null || StringUtils.isEmpty(settingSiteInfo.getSiteId())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SettingId or siteId is not null!!");
        }

        Long settingId = Long.valueOf(settingSiteInfo.getSettingId());
        UUID siteId = UUID.fromString(settingSiteInfo.getSiteId());

        var site = siteRepository.findById(siteId);
        if (site.isEmpty())
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SiteId is not correct in database!!");

        if (!settingRepository.existsById(settingId))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SettingId is not correct in database!!");

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId.toString())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You don't have permission to do this");
        }

        SettingSiteMapPk pk = new SettingSiteMapPk(settingId, siteId);
        SettingSiteMap settingSiteMap = settingSiteMapRepository.findById(pk).orElse(null);
        if (ObjectUtils.isEmpty(settingSiteMap)) {
            SettingSiteMap createSettingSite = new SettingSiteMap();
            createSettingSite.setSettingSiteMapPk(pk);
            createSettingSite.setValue(settingSiteInfo.getValue());
            createSettingSite.setDescription(settingSiteInfo.getDescription());
            createSettingSite.setStatus(true);

            auditLogRepository.save(new AuditLog(siteId.toString()
                , site.get().getOrganizationId().toString()
                , pk.toString()
                , SETTING_SITE_TABLE_NAME
                , Constants.AuditType.CREATE
                , null
                , createSettingSite.toString()));
            return settingSiteMapRepository.save(createSettingSite);
        } else {
            var settingSiteUpdate = settingSiteMapRepository.save(settingSiteMap.update(mapper.map(settingSiteInfo, SettingSiteMap.class)));
            auditLogRepository.save(new AuditLog(siteId.toString()
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
     * The function returns a list of SettingSiteMap objects based on the given siteId.
     *
     * @param siteId The siteId parameter is a unique identifier for a site. It is expected to be a string representation
     * of a UUID (Universally Unique Identifier).
     * @return The method is returning a List of SettingSiteMap objects.
     */
    @Override
    public List<ISettingSiteMapController.SettingSiteDTO> getAllSettingSiteBySiteId(String siteId) {
        List<ISettingSiteMapController.SettingSiteDTO> settingSiteDTOs = new ArrayList<>();
        List<Object[]> objects = settingRepository.findAllDistinctGroupIdBySiteId(UUID.fromString(siteId));
        if (!objects.isEmpty()) {
            for (Object[] results : objects) {
                var settingSiteDTO = findAllBySiteIdAndGroupId(siteId, Math.toIntExact((Long) results[0]));
                settingSiteDTOs.add(settingSiteDTO);
            }
        }
        return settingSiteDTOs;
    }


    /**
     * The function retrieves a list of SettingSiteDTO objects based on the provided siteId and settingGroupId.
     *
     * @param siteId The siteId parameter is a String that represents the ID of a site.
     * @param settingGroupId The settingGroupId parameter is an Integer that represents the ID of a setting group.
     * @return The method is returning a list of `ISettingSiteMapController.SettingSiteDTO` objects.
     */
    @Override
    public ISettingSiteMapController.SettingSiteDTO findAllBySiteIdAndGroupId(String siteId, Integer settingGroupId) {
        var userDetails = SecurityUtils.getUserDetails();
        var _siteId = userDetails.isOrganizationAdmin() ? siteId : userDetails.getSiteId();
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, _siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this");
        }
        var settingSites = settingSiteMapRepository.findAllBySiteIdAndGroupId(_siteId, settingGroupId);
        ISettingSiteMapController.SettingSiteDTO settingSiteDTO = new ISettingSiteMapController.SettingSiteDTO();
        if (!settingSites.isEmpty()) {
            settingSiteDTO.setSiteId(_siteId);
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
        return settingSiteDTO;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Boolean setDefaultValueBySite(String siteId) {

        var userDetails = SecurityUtils.getUserDetails();
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to do this");
        }
        var _siteId = userDetails.isOrganizationAdmin() ? siteId : userDetails.getSiteId();

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


}
