package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SettingServiceImplTest {
    @Mock
    private SettingRepository settingRepository;
    @InjectMocks

    private SettingServiceImpl settingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("given non-existent setting, when updating, then throw exception")
    void givenNonExistentSetting_WhenUpdate_ThenThrowException() {
        Long id = 1L;
        Setting entity = new Setting();
        entity.setCode("newCode");

        when(settingRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThrows(CustomException.class, () -> settingService.update(entity, id));
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

        assertThrows(CustomException.class, () -> settingService.update(entity, id));
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
        assertThrows(CustomException.class, () -> settingService.update(entity, id));
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
