package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingSiteMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SettingSiteMapServiceImplTest {

    @InjectMocks
    private SettingSiteMapServiceImpl settingSiteMapService;
    @InjectMocks
    private SettingServiceImpl settingService;
    @Mock
    private SettingSiteMapRepository settingSiteMapRepository;
    @Mock
    private SettingRepository settingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SiteRepository siteRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("given siteId and settingGroupId, when settings exist, return DTO with settings")
    void givenSiteIdAndGroupId_WhenSettingsExist_ThenReturnDTOWithSettings() {
        String siteId = "exampleSite";
        String code1 = "Test_setting_1";
        String code2 = "Test_setting_2";
        Long settingGroupId = 1L;

        ISettingSiteMapController.SettingSite setting1 = new ISettingSiteMapController.SettingSite();
        setting1.setStatus(true);
        setting1.setCode(code1);
        setting1.setSettingGroupId(settingGroupId);
        setting1.setPropertyValue("value1");


        ISettingSiteMapController.SettingSite setting2 = new ISettingSiteMapController.SettingSite();
        setting2.setStatus(true);
        setting2.setSettingGroupId(settingGroupId);
        setting2.setCode(code2);
        setting2.setPropertyValue("value2");

        List<ISettingSiteMapController.SettingSite> settingSiteDTOS = new ArrayList<>();
        settingSiteDTOS.add(setting1);
        settingSiteDTOS.add(setting2);

        when(settingSiteMapRepository.findAllBySiteIdAndGroupId(siteId, Math.toIntExact(settingGroupId)))
            .thenReturn(settingSiteDTOS);

        Map<String, String> settings = new HashMap<>();
        ISettingSiteMapController.SettingSiteDTO settingSiteDTO = new ISettingSiteMapController.SettingSiteDTO();
        settingSiteDTO.setSiteId(siteId);
        settingSiteDTO.setSettingGroupId(settingGroupId);
        settingSiteDTOS.forEach(o -> {
            settings.put(o.getCode(), o.getPropertyValue());
        });
        settingSiteDTO.setSettings(settings);


        ISettingSiteMapController.SettingSiteDTO result = settingSiteMapService.findAllBySiteIdAndGroupId(siteId, Math.toIntExact(settingGroupId));

        assertEquals(siteId, result.getSiteId());
        assertEquals(settingGroupId.longValue(), result.getSettingGroupId());
        assertEquals("value1", result.getSettings().get(code1));
        assertEquals("value2", result.getSettings().get(code2));
    }

    @Test
    @DisplayName("given siteId and settingGroupId, when no settings exist, return empty DTO")
    void givenSiteIdAndGroupId_WhenNoSettingsExist_ThenReturnEmptyDTO() {
        String siteId = "exampleSite";
        Integer settingGroupId = 1;

        when(settingSiteMapRepository.findAllBySiteIdAndGroupId(siteId, settingGroupId))
            .thenReturn(Collections.emptyList());

        ISettingSiteMapController.SettingSiteDTO result = settingSiteMapService.findAllBySiteIdAndGroupId(siteId, settingGroupId);

        assertEquals(null, result.getSiteId());
        assertEquals(null, result.getSettingGroupId());
        assertEquals(null, result.getSettings());
    }

    @Test
    @DisplayName("given siteId, when setting groups exist, return DTO list")
    void givenSiteId_WhenSettingGroupsExist_ThenReturnDTOList() {
        UUID siteId = UUID.randomUUID();
        Object[] group1 = {1L}; // Simulate group 1
        Object[] group2 = {2L}; // Simulate group 2

        List<Object[]> settingGroupId = new ArrayList<>();
        settingGroupId.add(group1);
        settingGroupId.add(group2);
        when(settingRepository.findAllDistinctGroupIdBySiteId(UUID.randomUUID()))
            .thenReturn(settingGroupId);

        List<ISettingSiteMapController.SettingSiteDTO> settingSiteDTOs = new ArrayList<>();
        for (Object[] results : settingGroupId) {
            Map<String, String> settings = new HashMap<>();
            ISettingSiteMapController.SettingSiteDTO settingSiteDTO = new ISettingSiteMapController.SettingSiteDTO();
            settingSiteDTO.setSiteId(siteId.toString());
            settingSiteDTO.setSettingGroupId((Long) results[0]);
            settings.put("ABC", "ABC");
            settingSiteDTO.setSettings(settings);
            settingSiteDTOs.add(settingSiteDTO);
        }

        when(settingSiteMapService.getAllSettingSiteBySiteId(siteId.toString()))
            .thenReturn(settingSiteDTOs);

        assertEquals(2, settingSiteDTOs.size());
        assertEquals(1L, settingSiteDTOs.get(0).getSettingGroupId());
        assertEquals(2L, settingSiteDTOs.get(1).getSettingGroupId());
    }

    @Test
    @DisplayName("given siteId, when no setting groups exist, return empty DTO list")
    void givenSiteId_WhenNoSettingGroupsExist_ThenReturnEmptyDTOList() {
        UUID siteId = UUID.randomUUID();

        when(settingRepository.findAllDistinctGroupIdBySiteId(siteId))
            .thenReturn(Collections.emptyList());

        List<ISettingSiteMapController.SettingSiteDTO> result = settingSiteMapService.getAllSettingSiteBySiteId(siteId.toString());

        assertEquals(0, result.size());
    }


}
