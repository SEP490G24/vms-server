package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SettingServiceImplTest {
    @Mock
    private SettingRepository settingRepository;
    @InjectMocks
    private SettingServiceImpl settingService;

    private TemplateServiceImpl templateService;
    private TemplateRepository templateRepository;
    private SiteRepository siteRepository;
    private ModelMapper mapper;
    private AuditLogRepository auditLogRepository;

    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        templateRepository = mock(TemplateRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        mapper = mock(ModelMapper.class);
        templateService = new TemplateServiceImpl(templateRepository, siteRepository, mapper, auditLogRepository);

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
    @DisplayName("given non-existent setting, when updating, then throw exception")
    void givenNonExistentSetting_WhenUpdate_ThenThrowException() {
        Long id = 1L;
        Setting entity = new Setting();
        entity.setCode("newCode");

        when(settingRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> settingService.update(entity, id));
    }

    @Test
    @DisplayName("given existing code, when updating with duplicate code, then throw exception")
    void givenExistingCode_WhenUpdateWithDuplicateCode_ThenThrowException() {
        Long id = 1L;
        Setting entity = new Setting();
        entity.setCode("duplicateCode");

        Setting existingSetting = new Setting();
        existingSetting.setId(id); // Simulate an existing setting with the same code
        when(settingRepository.findById(id)).thenReturn(java.util.Optional.of(existingSetting));
        when(settingRepository.existsByCode(entity.getCode())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> settingService.update(entity, id));
    }

    @Test
    @DisplayName("given valid input, when updating, return the updated setting")
    void givenValidInput_WhenUpdate_ThenReturnUpdatedSetting() {
        Long id = 1L;
        Setting entity = new Setting();
        entity.setCode("newCode");

        Setting existingSetting = new Setting();
        existingSetting.setId(id);
        when(settingRepository.findById(id)).thenReturn(java.util.Optional.of(existingSetting));
        when(settingRepository.existsByCode(entity.getCode())).thenReturn(false);
        when(settingRepository.save(existingSetting)).thenReturn(existingSetting); // Simulate successful save

        Setting updatedSetting = settingService.update(entity, id);
        assertEquals(entity.getCode(), updatedSetting.getCode());
        // Add more assertions if needed
    }

    @Test
    @DisplayName("given existing code, when updating with duplicate code, then throw exception")
    void givenExistingCode_WhenUpdateWithNullEntity_ThenThrowException() {
        Long id = 1L;
        Setting entity = null;
        assertThrows(HttpClientErrorException.class, () -> settingService.update(entity, id));
    }

    @Test
    public void testFindAllByGroupIdAndSiteIdWithNoSettings() {
        // Mock data
        Integer groupId = 1;
        String siteId = "exampleSiteId";

        // No settings in the repository
        when(settingRepository.findAllByGroupId(any(Long.class))).thenReturn(Collections.emptyList());

        // Call the method to test
        List<Setting> resultSettings = settingService.findAllByGroupIdAndSiteId(groupId, siteId);

        // Assertions for no settings

        // Verify that the repository method was called
        verify(settingRepository, times(1)).findAllByGroupId(any(Long.class));
    }

    @Test
    public void testFindAllByGroupIdAndSiteIdWithNonApiSetting() {
        // Similar to the previous test but for a non-API setting

        // Mock data
        Integer groupId = 1;
        String siteId = "exampleSiteId";

        Setting nonApiSetting = new Setting();
        nonApiSetting.setType(Constants.SettingType.SWITCH);
        nonApiSetting.setCode(Constants.SettingCode.MAIL_SMTP_STARTTLS_ENABLE);

        List<Setting> settings = Collections.singletonList(nonApiSetting);

        when(settingRepository.findAllByGroupId(any(Long.class))).thenReturn(settings);

        // Call the method to test
        List<Setting> resultSettings = settingService.findAllByGroupIdAndSiteId(groupId, siteId);

        // Assertions for non-API setting

        // Verify that the repository method was called
        verify(settingRepository, times(1)).findAllByGroupId(any(Long.class));

    }

}
