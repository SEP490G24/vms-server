package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
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
    private final ModelMapper mapper;


    public SettingSiteMapServiceImpl(SettingSiteMapRepository settingSiteMapRepository, SettingRepository settingRepository, SiteRepository siteRepository, UserRepository userRepository, ModelMapper mapper) {
        this.settingSiteMapRepository = settingSiteMapRepository;
        this.settingRepository = settingRepository;
        this.siteRepository = siteRepository;
        this.userRepository = userRepository;
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
    public SettingSiteMap createOrUpdateSettingSiteMap(ISettingSiteMapController.SettingSiteInfo settingSiteInfo) {

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

        var user = userRepository.findByUsername(SecurityUtils.loginUsername());

        if (!site.get().getOrganizationId().equals(UUID.fromString(SecurityUtils.getOrgId()))) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Please login with account of organization");
        }

        if (ObjectUtils.isEmpty(user) && !user.get().getDepartment().getSiteId().equals(siteId)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Please login before change data setting!!");
        }


        SettingSiteMapPk pk = new SettingSiteMapPk(settingId, siteId);
        SettingSiteMap settingSiteMap = settingSiteMapRepository.findById(pk).orElse(null);
        if (ObjectUtils.isEmpty(settingSiteMap)) {
            SettingSiteMap createSettingSite = new SettingSiteMap();
            createSettingSite.setSettingSiteMapPk(pk);
            createSettingSite.setValue(settingSiteInfo.getValue());
            createSettingSite.setDescription(settingSiteInfo.getDescription());
            createSettingSite.setStatus(true);
            return settingSiteMapRepository.save(createSettingSite);
        } else {
            return settingSiteMapRepository.save(settingSiteMap.update(mapper.map(settingSiteInfo, SettingSiteMap.class)));
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
    public List<SettingSiteMap> getAllSettingSiteBySiteId(String siteId) {
        return settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(UUID.fromString(siteId));
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
        var settingSites = settingSiteMapRepository.findAllBySiteIdAndGroupId(siteId, settingGroupId);
        ISettingSiteMapController.SettingSiteDTO settingSiteDTO = new ISettingSiteMapController.SettingSiteDTO();
        if (!settingSites.isEmpty()) {
            settingSiteDTO.setSiteId(siteId);
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
}
