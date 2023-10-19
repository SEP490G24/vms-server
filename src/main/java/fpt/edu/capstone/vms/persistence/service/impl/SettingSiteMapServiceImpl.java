package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.entity.User;
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

import java.util.List;
import java.util.Optional;
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

    @Override
    public List<SettingSiteMap> getAllSettingSiteBySiteId(String siteId) {
        return settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(UUID.fromString(siteId));
    }
}
