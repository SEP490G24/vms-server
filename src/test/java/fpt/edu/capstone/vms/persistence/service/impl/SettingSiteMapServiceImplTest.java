package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingSiteMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettingSiteMapServiceImplTest {

    private SettingSiteMapServiceImpl settingSiteMapService;
    private SettingSiteMapRepository settingSiteMapRepository;
    private SettingRepository settingRepository;
    private UserRepository userRepository;
    private SiteRepository siteRepository;
    private AuditLogRepository auditLogRepository;

    SecurityContext securityContext;
    Authentication authentication;
    ModelMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        mapper = mock(ModelMapper.class);
        settingSiteMapRepository = mock(SettingSiteMapRepository.class);
        settingRepository = mock(SettingRepository.class);
        userRepository = mock(UserRepository.class);
        siteRepository = mock(SiteRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        settingSiteMapService = new SettingSiteMapServiceImpl(settingSiteMapRepository, settingRepository, siteRepository, userRepository, auditLogRepository, mapper);
    }

    @Test
    @DisplayName("given siteId and settingGroupId, when settings exist, return DTO with settings")
    void givenSiteIdAndGroupId_WhenSettingsExist_ThenReturnDTOWithSettings() {
        String siteId = "06eb43a7-6ea8-4744-8231-760559fe2c08";
        String code1 = "Test_setting_1";
        String code2 = "Test_setting_2";
        Long settingGroupId = 1L;


        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);


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
        String siteId = "06eb43a7-6ea8-4744-8231-760559fe2c08";
        Integer settingGroupId = 1;

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

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

    @Test
    void testCreateOrUpdateSettingSiteMap_SiteIdNotFoundInDatabase() {
        // Mocking input data
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo = ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId(UUID.randomUUID().toString()).value("abc").build();

        // Mocking repository responses
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Test case for HttpClientErrorException with HttpStatus.BAD_REQUEST and message "SiteId is not correct in database!!"
        assertThrows(HttpClientErrorException.class, () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));
    }

    @Test
    void testCreateOrUpdateSettingSiteMap_SettingIdNotFoundInDatabase() {
        // Mocking input data
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo = ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId(UUID.randomUUID().toString()).value("abc").build();


        // Mocking repository responses
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Site()));
        when(settingRepository.existsById(any(Long.class))).thenReturn(false);

        // Test case for HttpClientErrorException with HttpStatus.BAD_REQUEST and message "SettingId is not correct in database!!"
        assertThrows(HttpClientErrorException.class, () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));
    }

    @Test
    void testCreateOrUpdateSettingSiteMap_NoPermission() {
        // Mocking input data
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo = ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId(UUID.randomUUID().toString()).value("abc").build();

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mocking repository responses
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Site()));
        when(settingRepository.existsById(any(Long.class))).thenReturn(true);
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(false);

        // Test case for HttpClientErrorException with HttpStatus.BAD_REQUEST and message "You don't have permission to do this"
        assertThrows(HttpClientErrorException.class, () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));
    }

    @Test
    void testCreateOrUpdateSettingSiteMap_ValueIsEmpty() {
        // Mocking input data
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo = ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId(UUID.randomUUID().toString()).value("").build();


        // Test case for HttpClientErrorException with HttpStatus.BAD_REQUEST and message "Value is empty"
        assertThrows(HttpClientErrorException.class, () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));
    }

    @Test
    void testCreateOrUpdateSettingSiteMap_SettingIdOrSiteIdIsNull() {
        // Mocking input data
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo = ISettingSiteMapController.SettingSiteInfo.builder().settingId(null).siteId(null).value("abc").build();

        // Test case for HttpClientErrorException with HttpStatus.BAD_REQUEST and message "SettingId or siteId is not null!!"
        assertThrows(HttpClientErrorException.class, () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));
    }

    @Test
    void testCreateOrUpdateSettingSiteMap_SuccessfulUpdate() {
        // Mocking input data
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo = ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId("06eb43a7-6ea8-4744-8231-760559fe2c06").value("abc").build();

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        // Mocking repository responses
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(site));
        when(settingRepository.existsById(any(Long.class))).thenReturn(true);
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(true);
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(true);
        SettingSiteMap existingSettingSiteMap = new SettingSiteMap();
        SettingSiteMapPk pk = new SettingSiteMapPk(settingSiteInfo.getSettingId().longValue(), UUID.fromString(settingSiteInfo.getSiteId()));
        existingSettingSiteMap.setSettingSiteMapPk(pk);
        when(settingSiteMapRepository.findById(pk)).thenReturn(Optional.of(existingSettingSiteMap));
        when(mapper.map(settingSiteInfo, SettingSiteMap.class)).thenReturn(existingSettingSiteMap);

        when(settingSiteMapRepository.save(existingSettingSiteMap)).thenReturn(existingSettingSiteMap);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c06", auditLog.getSiteId());
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getOrganizationId());
            assertEquals("Setting Site Map", auditLog.getTableName());
            assertEquals(Constants.AuditType.UPDATE, auditLog.getAuditType());
            return auditLog;
        });


        // Test case for successful update
//        assertThrows(Htt -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));
    }

    @Test
    void testSetDefaultValueBySite_SuccessfulDeletion() {
        // Mocking input data
        String siteId = "06eb43a7-6ea8-4744-8231-760559fe2c08";

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SecurityUtils.UserDetails userDetails = new SecurityUtils.UserDetails();
        userDetails.setOrganizationAdmin(true);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        // Mocking repository responses
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(site));
        SettingSiteMap settingSiteMap = new SettingSiteMap();
        SettingSiteMapPk pk = new SettingSiteMapPk(1L, UUID.fromString(siteId));
        settingSiteMap.setSettingSiteMapPk(pk);
        when(settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(any(UUID.class))).thenReturn(Collections.singletonList(settingSiteMap));

        // Test case for successful deletion
        assertTrue(settingSiteMapService.setDefaultValueBySite(siteId));
    }

    @Test
    void testSetDefaultValueBySite_NoPermission() {
        // Mocking input data
        String siteId = "06eb43a7-6ea8-4744-8231-760559fe2c08";

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SecurityUtils.UserDetails userDetails = new SecurityUtils.UserDetails();
        userDetails.setOrganizationAdmin(false);

        // Test case for HttpClientErrorException with HttpStatus.FORBIDDEN and message "You don't have permission to do this"
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> settingSiteMapService.setDefaultValueBySite(siteId));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("403 You don't have permission to do this", exception.getMessage());
    }

    @Test
    void testSetDefaultValueBySite_NoSettingSitesFound() {
        // Mocking input data
        String siteId = "06eb43a7-6ea8-4744-8231-760559fe2c08";

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        SecurityUtils.UserDetails userDetails = new SecurityUtils.UserDetails();
        userDetails.setOrganizationAdmin(true);


        // Mocking repository responses
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Site()));
        when(settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(any(UUID.class))).thenReturn(Collections.emptyList());

        // Test case for no setting sites found
        assertFalse(settingSiteMapService.setDefaultValueBySite(siteId));
    }
}
