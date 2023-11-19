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
import org.mockito.ArgumentMatchers;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("given siteId and settingGroupId, when settings exist, return DTO with settings")
    void givenSiteIdAndGroupId_WhenSettingsExist_ThenReturnDTOWithSettings() {
        String siteId = "06eb43a7-6ea8-4744-8231-760559fe2c08";
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

        List<String> sites = new ArrayList<>();
        sites.add("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);

        ISettingSiteMapController.SettingSiteDTO result = settingSiteMapService.findAllBySiteIdAndGroupId(Math.toIntExact(settingGroupId), sites);

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

        when(settingSiteMapRepository.findAllBySiteIdAndGroupId(siteId, settingGroupId))
            .thenReturn(Collections.emptyList());

        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);

        List<String> sites = new ArrayList<>();
        ISettingSiteMapController.SettingSiteDTO result = settingSiteMapService.findAllBySiteIdAndGroupId(settingGroupId, sites);

        assertEquals(null, result.getSiteId());
        assertEquals(null, result.getSettingGroupId());
        assertEquals(null, result.getSettings());
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
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo = ISettingSiteMapController
            .SettingSiteInfo.builder().settingId(1).siteId(UUID.randomUUID().toString()).value("").build();


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
        SecurityUtils.UserDetails userDetails = mock(SecurityUtils.UserDetails.class);
        userDetails.setOrganizationAdmin(false);
        when(userDetails.isOrganizationAdmin()).thenReturn(false);
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(true);
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        // Mocking repository responses
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(site));
        when(settingRepository.existsById(any(Long.class))).thenReturn(true);

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
//        assertDoesNotThrow(() -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));
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

    @Test
    void testCreateOrUpdateSettingSiteMapWithInvalidSettingId() {
        // ... (similar setup as previous tests)

        // Creating a SettingSiteInfo object with an invalid settingId for testing
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo =
            ISettingSiteMapController.SettingSiteInfo.builder().settingId(null).siteId("06eb43a7-6ea8-4744-8231-760559fe2c08").value("abc").build();
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(true);


        // Testing the method and expecting a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));

        // Verifying the exception status code and message
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 SettingId or siteId is not null!!", exception.getMessage());

        // Verifying interactions with dependencies
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verify(settingSiteMapRepository, never()).save(any(SettingSiteMap.class));
    }

    @Test
    void testCreateOrUpdateSettingSiteMapWithNonExistingSite() {
        // ... (similar setup as previous tests)

        // Mocking a scenario where the site is not found
        when(siteRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());

        // Creating a SettingSiteInfo object with an invalid settingId for testing
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo =
            ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId("06eb43a7-6ea8-4744-8231-760559fe2c08").value("abc").build();
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(true);

        when(settingRepository.existsById(ArgumentMatchers.any())).thenReturn(true);

        // Testing the method and expecting a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));

        // Verifying the exception status code and message
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 SiteId is not correct in database!!", exception.getMessage());

        // Verifying interactions with dependencies
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verify(settingSiteMapRepository, never()).save(any(SettingSiteMap.class));
    }

    @Test
    void testCreateOrUpdateSettingSiteMapWithEmptyValue() {
        // ... (similar setup as previous tests)

        ISettingSiteMapController.SettingSiteInfo settingSiteInfo =
            ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId("06eb43a7-6ea8-4744-8231-760559fe2c08").value("").build();
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(true);

        // Testing the method and expecting a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));

        // Verifying the exception status code and message
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 Value is empty", exception.getMessage());

        // Verifying interactions with dependencies
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verify(settingSiteMapRepository, never()).save(any(SettingSiteMap.class));
    }

    @Test
    void testCreateOrUpdateSettingSiteMapWithNonExistingSetting() {
        // ... (similar setup as previous tests)

        when(siteRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(new Site()));

        // Mocking a scenario where the setting is not found
        when(settingRepository.existsById(ArgumentMatchers.any())).thenReturn(false);

        ISettingSiteMapController.SettingSiteInfo settingSiteInfo =
            ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId("06eb43a7-6ea8-4744-8231-760559fe2c08").value("aaa").build();
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(true);

        // Testing the method and expecting a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));

        // Verifying the exception status code and message
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 SettingId is not correct in database!!", exception.getMessage());

        // Verifying interactions with dependencies
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verify(settingSiteMapRepository, never()).save(any(SettingSiteMap.class));
    }


    @Test
    void testCreateOrUpdateSettingSiteMapWithUnauthorizedSite() {
        // ... (similar setup as previous tests)

        // Mocking a scenario where site authorization fails
        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(false);

        ISettingSiteMapController.SettingSiteInfo settingSiteInfo =
            ISettingSiteMapController.SettingSiteInfo.builder().settingId(1).siteId("06eb43a7-6ea8-4744-8231-760559fe2c08").value("aaa").build();
        when(SecurityUtils.checkSiteAuthorization(siteRepository, settingSiteInfo.getSiteId())).thenReturn(false);

        // Testing the method and expecting a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));

        // Verifying the exception status code and message
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("403 You don't have permission to do this", exception.getMessage());

        // Verifying interactions with dependencies
        verify(siteRepository, never()).findById(any());
        verify(settingRepository, never()).existsById(any());
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verify(settingSiteMapRepository, never()).save(any(SettingSiteMap.class));
    }

    @Test
    void testCreateOrUpdateSettingSiteMapWithNullObject() {
        // Creating a null SettingSiteInfo object for testing
        ISettingSiteMapController.SettingSiteInfo settingSiteInfo = null;
        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);

        // Testing the method and expecting a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> settingSiteMapService.createOrUpdateSettingSiteMap(settingSiteInfo));

        // Verifying the exception status code and message
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 Object is null", exception.getMessage());

        // Verifying interactions with dependencies
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verify(settingSiteMapRepository, never()).save(any(SettingSiteMap.class));
    }

    @Test
    void testFindAllBySiteIdAndGroupId_ThrowNotPermission() {
        // Arrange
        String siteId = "06eb43a7-6ea8-4744-8231-760559fe2c08";
        Integer settingGroupId = 1;

        // Mock checkSiteAuthorization to allow access
        List<String> sites = new ArrayList<>();
        sites.add("06eb43a7-6ea8-4744-8231-760559fe2c04");
        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c04")).thenReturn(false);

        // Testing the method and expecting a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> settingSiteMapService.findAllBySiteIdAndGroupId(settingGroupId, sites));

        // Verifying the exception status code and message
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("403 You don't have permission to do this.", exception.getMessage());
    }
}
