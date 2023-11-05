package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.service.IFileService;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizationServiceImplTest {

    @InjectMocks
    private OrganizationServiceImpl organizationService;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private IUserService userService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("given valid organization, save organization and create admin user")
    void givenValidOrganization_SaveOrganizationAndCreateAdminUser() {
        UUID orgId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setCode("orgCode");
        organization.setId(orgId);

        when(organizationRepository.existsByCode("orgCode")).thenReturn(false);
        when(organizationRepository.save(organization)).thenReturn(organization);

        IUserResource.UserDto adminUserDto = new IUserResource.UserDto();
        adminUserDto.setUsername("orgcode_admin");
        adminUserDto.setPassword("123456aA@");
        adminUserDto.setOrgId(orgId.toString());
        adminUserDto.setIsCreateUserOrg(true);

        User user = new User();
        user.setUsername(adminUserDto.getUsername());
        user.setPassword(adminUserDto.getPassword());

        when(userService.createUser(adminUserDto)).thenReturn(user);

        Organization result = organizationService.save(organization);

        assertEquals("orgCode", result.getCode());

        verify(organizationRepository, times(1)).save(organization);
        verify(userService, times(1)).createUser(adminUserDto);
    }

    @Test
    @DisplayName("given organization with existing code, throw HttpClientErrorException")
    void givenOrganizationWithExistingCode_ThrowException() {
        Organization organization = new Organization();
        organization.setCode("orgCode");

        when(organizationRepository.existsByCode("orgCode")).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> organizationService.save(organization));
    }

    @Test
    @DisplayName("given organization with empty code, throw HttpClientErrorException")
    void givenOrganizationWithEmptyCode_ThrowException() {
        Organization organization = new Organization();

        assertThrows(HttpClientErrorException.class, () -> organizationService.save(organization));
    }

    @Test
    @DisplayName("given null organization, throw HttpClientErrorException")
    void givenNullOrganization_ThrowException() {
        assertThrows(NullPointerException.class, () -> organizationService.save(null));
    }

}
