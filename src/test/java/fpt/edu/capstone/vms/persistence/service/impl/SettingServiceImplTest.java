package fpt.edu.capstone.vms.persistence.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import static org.junit.jupiter.api.Assertions.*;
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
}
