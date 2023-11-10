package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizationServiceImplTest {

    @InjectMocks
    private OrganizationServiceImpl organizationService;
    @InjectMocks
    private FileServiceImpl fileService;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private IUserService userService;
    @Mock
    private AuditLogRepository auditLogRepository;
    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
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

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            assertEquals(null, auditLog.getSiteId());
            assertEquals(organization.getId().toString(), auditLog.getOrganizationId());
            assertEquals(organization.getId().toString(), auditLog.getPrimaryKey());
            assertEquals("Organization", auditLog.getTableName());
            assertEquals(Constants.AuditType.CREATE, auditLog.getAuditType());
            assertEquals(null, auditLog.getOldValue());
            assertEquals(organization.toString(), auditLog.getNewValue());
            return auditLog;
        });
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

    @Test
    @DisplayName("given code organization exist, throw HttpClientErrorException")
    public void givenUpdateWithExistingCode_ThrowException() {
        // Arrange
        Organization entity = new Organization();
        entity.setCode("existingCode");
        UUID id = UUID.randomUUID();

        when(organizationRepository.existsByCode("existingCode")).thenReturn(true);

        // Act and Assert
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            organizationService.update(entity, id);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        // Verify that no other methods were called on organizationRepository
        verify(organizationRepository, never()).findById(any());
    }

    @Test
    @DisplayName("given organization Mismatched OrgId, throw HttpClientErrorException")
    public void givenUpdateWithMismatchedOrgId_ThrowException() {
        // Arrange
        Organization entity = new Organization();
        UUID id = UUID.randomUUID();

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act and Assert
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            organizationService.update(entity, id);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        // Verify that no other methods were called on organizationRepository
        verify(organizationRepository, never()).findById(any());
    }

    @Test
    @DisplayName("given organization Null Entity, throw HttpClientErrorException")
    public void givenUpdateWithNullEntity_ThrowException() {
        // Arrange
        Organization entity = null;
        UUID id = UUID.randomUUID();

        // Act and Assert
        assertThrows(NullPointerException.class, () -> {
            organizationService.update(entity, id);
        });
    }

    @Test
    public void testUpdateWithNonExistentOrganization() {
        // Arrange
        Organization entity = new Organization();
        UUID id = UUID.randomUUID();

        when(organizationRepository.findById(id)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(NullPointerException.class, () -> {
            organizationService.update(entity, id);
        });
    }

    @Test
    @DisplayName("given organization, update organization")
    public void testUpdateWithNameChangeAndSuccessfulDelete() {
        // Arrange
        Organization entity = new Organization();
        UUID id = UUID.randomUUID();
        entity.setName("newName");
        entity.setId(id);
        String oldImage = "old";
        String newImage = "new";
        Organization exist = new Organization();
        exist.setId(id);
        exist.setName("newName");

        when(organizationRepository.findById(id)).thenReturn(Optional.of(exist));
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn(id.toString());
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(organizationRepository.save(exist.update(entity))).thenReturn(exist);
        // Act
        Organization updatedOrganization = organizationService.update(entity, id);

        // Assert
        assertEquals("newName", updatedOrganization.getName());

        // Verify that deleteImage and other relevant methods were called
        verify(organizationRepository, times(1)).save(any(Organization.class));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}
