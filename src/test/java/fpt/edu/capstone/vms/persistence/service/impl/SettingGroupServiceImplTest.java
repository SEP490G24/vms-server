package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.SettingGroup;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingGroupRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SettingGroupServiceImplTest {

    @Mock
    private SettingGroupRepository settingGroupRepository;
    @InjectMocks
    private SettingGroupServiceImpl settingGroupService;

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

    }

    @Test
    public void testUpdateWithValidEntityAndExistingId() {
        // Mock data
        Long id = 1L;
        SettingGroup existingSettingGroup = new SettingGroup();
        existingSettingGroup.setId(id);

        SettingGroup updatedEntity = new SettingGroup();
        // Set properties for the updated entity

        when(settingGroupRepository.findById(id)).thenReturn(java.util.Optional.of(existingSettingGroup));
        when(settingGroupRepository.save(any(SettingGroup.class))).thenReturn(existingSettingGroup);

        // Call the method to test
        SettingGroup result = settingGroupService.update(updatedEntity, id);

        // Assertions
        assertEquals(existingSettingGroup, result);

        // Verify that findById and save methods were called
        verify(settingGroupRepository, times(1)).findById(id);
        verify(settingGroupRepository, times(1)).save(any(SettingGroup.class));
    }

    @Test()
    public void testUpdateWithEmptyEntity() {
        // Call the method with an empty entity, expecting an exception

        assertThrows(CustomException.class, () -> settingGroupService.update(null, 1L));

    }

    @Test()
    public void testUpdateWithNonExistingId() {
        // Mock data
        Long id = 1L;

        when(settingGroupRepository.findById(id)).thenReturn(java.util.Optional.empty());

        // Call the method with a non-existing ID, expecting an exception
        assertThrows(CustomException.class, () -> settingGroupService.update(new SettingGroup(), id));

    }
}
