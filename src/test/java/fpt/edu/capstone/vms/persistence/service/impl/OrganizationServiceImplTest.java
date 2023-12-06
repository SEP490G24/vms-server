package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.service.IPermissionService;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_REALM_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_RESOURCE_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.REALM_ADMIN;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_ORGANIZATION;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private IRoleService roleService;
    @Mock
    private IPermissionService iPermissionService;
    @Mock
    private AuditLogRepository auditLogRepository;
    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityUtils.UserDetails userDetails = new SecurityUtils.UserDetails();
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority(PREFIX_REALM_ROLE + REALM_ADMIN),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_ORGANIZATION),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_SITE)
        );

        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        MockitoAnnotations.openMocks(this);
        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);
        // Mock the behavior of authentication.getAuthorities() using thenAnswer
        when(authentication.getAuthorities()).thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> {
            userDetails.setRealmAdmin(false);
            userDetails.setOrganizationAdmin(false);
            userDetails.setSiteAdmin(false);

            // Iterate over the authorities and set flags in userDetails
            for (GrantedAuthority grantedAuthority : authorities) {
                switch (grantedAuthority.getAuthority()) {
                    case PREFIX_REALM_ROLE + REALM_ADMIN:
                        userDetails.setRealmAdmin(true);
                        break;
                    case PREFIX_RESOURCE_ROLE + SCOPE_ORGANIZATION:
                        userDetails.setOrganizationAdmin(true);
                        break;
                    case PREFIX_RESOURCE_ROLE + SCOPE_SITE:
                        userDetails.setSiteAdmin(true);
                        break;
                }
            }

            return authorities;
        });


        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
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


        User user = new User();
        user.setUsername(adminUserDto.getUsername());
        List<String> roles = new ArrayList<>();
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setCode(organization.getCode().toUpperCase() + "_" + "ADMIN");
        roleDto.setDescription("Role này là role admin của tổ chức " + organization.getName());
        roles.add(roleDto.getCode());
        adminUserDto.setRoles(roles);

        when(iPermissionService.findAllByModuleId("339f9a15-bacf-48dd-acd6-87c482ebb36e")).thenReturn(new ArrayList<>());
        when(iPermissionService.findAllByModuleId("75366af1-57bd-4115-b672-b2de7fa40a7d")).thenReturn(new ArrayList<>());
        when(roleService.create(roleDto)).thenReturn(roleDto);
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
    }

    @Test
    @DisplayName("given organization with existing code, throw HttpClientErrorException")
    void givenOrganizationWithExistingCode_ThrowException() {
        Organization organization = new Organization();
        organization.setCode("orgCode");

        when(organizationRepository.existsByCode("orgCode")).thenReturn(true);

        assertThrows(CustomException.class, () -> organizationService.save(organization));
    }

    @Test
    @DisplayName("given organization with empty code, throw HttpClientErrorException")
    void givenOrganizationWithEmptyCode_ThrowException() {
        Organization organization = new Organization();

        assertThrows(CustomException.class, () -> organizationService.save(organization));
    }

    @Test
    @DisplayName("given null organization, throw HttpClientErrorException")
    void givenNullOrganization_ThrowException() {
        assertThrows(NullPointerException.class, () -> organizationService.save(null));
    }

    @Test
    void updateWithNullIdTest() {
        // Arrange
        Organization entity = new Organization(/* initialize your Organization */);
        UUID id = null;

        // Act and Assert
        assertThrows(NullPointerException.class, () -> organizationService.update(entity, id),
            "The Id of organization is null");
    }

    @Test
    @DisplayName("given code organization exist, throw HttpClientErrorException")
    public void givenUpdateWithExistingCode_ThrowException() {
        // Arrange
        Organization entity = new Organization();
        entity.setCode("existingCode");
        UUID id = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(organizationRepository.existsByCode("existingCode")).thenReturn(true);

        // Act and Assert
        assertThrows(CustomException.class, () -> {
            organizationService.update(entity, id);
        });

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
        assertThrows(CustomException.class, () -> {
            organizationService.update(entity, id);
        });

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
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(organizationRepository.findById(id)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(CustomException.class, () -> {
            organizationService.update(entity, id);
        });
    }

    @Test
    @DisplayName("given organization, update organization")
    public void testUpdateWithNameChangeAndSuccessfulDelete() {
        UUID id = UUID.randomUUID();
        // Arrange
        Organization entity = new Organization();

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

    @Test
    void filterPageableTest() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<String> names = Arrays.asList("Org1", "Org2");
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createdBy = "John Doe";
        String lastUpdatedBy = "Jane Doe";
        Boolean enable = true;

        // Mock the behavior of the organizationRepository
        List<Organization> expectedContent = Arrays.asList(
            new Organization(/* initialize your Organization */));
        Page<Organization> expectedResult = new PageImpl<>(expectedContent);

        when(organizationRepository.filter(
            eq(pageable), eq(names), eq(createdOnStart), eq(createdOnEnd),
            eq(createdBy), eq(lastUpdatedBy), eq(enable), eq(null)))
            .thenReturn(expectedResult);

        // Act
        Page<Organization> result = organizationService.filter(
            pageableSort, names, createdOnStart, createdOnEnd,
            createdBy, lastUpdatedBy, enable, null);

        // Assert
        assertEquals(expectedResult, result);

    }

    @Test
    void filterTest() {
        // Arrange
        List<String> names = Arrays.asList("Org1", "Org2");
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createdBy = "John Doe";
        String lastUpdatedBy = "Jane Doe";
        Boolean enable = true;

        // Mock the behavior of the organizationRepository
        List<Organization> expectedResult = Arrays.asList(
            new Organization(/* initialize your Organization */));

        when(organizationRepository.filter(
            eq(names), eq(createdOnStart), eq(createdOnEnd),
            eq(createdBy), eq(lastUpdatedBy), eq(enable), eq(null)))
            .thenReturn(expectedResult);

        // Act
        List<Organization> result = organizationService.filter(
            names, createdOnStart, createdOnEnd,
            createdBy, lastUpdatedBy, enable, null);

        // Assert
        assertEquals(expectedResult, result);

        // Verify that the filter method of organizationRepository was called with the correct arguments
        verify(organizationRepository).filter(
            eq(names), eq(createdOnStart), eq(createdOnEnd),
            eq(createdBy), eq(lastUpdatedBy), eq(enable), eq(null));
    }
}
