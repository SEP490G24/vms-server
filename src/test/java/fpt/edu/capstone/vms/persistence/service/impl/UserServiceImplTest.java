package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@TestInstance(PER_CLASS)
@ActiveProfiles("test")
@Tag("UnitTest")
@DisplayName("User Service Unit Tests")
class UserServiceImplTest {

    UserServiceImpl userService;
    SecurityContext securityContext;
    Authentication authentication;

    SiteRepository siteRepository;
    UserRepository userRepository;
    FileRepository fileRepository;
    FileServiceImpl fileService;
    IUserResource userResource;
    IRoleResource roleResource;

    ModelMapper mapper;
    DepartmentRepository departmentRepository;
    AuditLogRepository auditLogRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        siteRepository = mock(SiteRepository.class);
        userRepository = mock(UserRepository.class);
        fileRepository = mock(FileRepository.class);
        fileService = mock(FileServiceImpl.class);
        userResource = mock(IUserResource.class);
        roleResource = mock(IRoleResource.class);
        mapper = mock(ModelMapper.class);
        departmentRepository = mock(DepartmentRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        userService = new UserServiceImpl(userRepository, fileRepository, fileService, userResource, siteRepository, mapper, departmentRepository, auditLogRepository, roleResource);
    }

    @Test
    void testFilter() {
        // Mock SecurityContext and Authentication
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<String> usernames = new ArrayList<>();
        String role = "ROLE_USER";
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "search";
        List<String> departmentIds = new ArrayList<>();
        List<UUID> departmentIds1 = new ArrayList<>();
        List<String> siteIds = new ArrayList<>();
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;
        // Mock the result you expect from userRepository.filter
        Page<IUserController.UserFilterResponse> expectedPage = new PageImpl<>(new ArrayList<>());


        //when(userService.getListDepartments(siteIds, departmentIds)).thenReturn(departmentIds1);

        when(userRepository.filter(pageable, usernames
            , role, createdOnStart, createdOnEnd
            , enable, keyword, departmentIds1
            , provinceId, districtId, communeId))
            .thenReturn(expectedPage);

        // Call the actual method
        Page<IUserController.UserFilterResponse> result = userService.filter(
            pageableSort,
            usernames,
            role,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword,
            departmentIds,
            siteIds,
            provinceId,
            districtId,
            communeId
        );

        // Verify that the result is as expected
        assertEquals(null, result);
    }

    @Test
    void testCreateAdmin() {
        // Mock input data
        IUserResource.UserDto userDto = new IUserResource.UserDto();
        userDto.setUsername("admin");
        userDto.setPassword("password");
        userDto.setRoles(Arrays.asList("ADMIN"));

        User userEntity = new User();
        userEntity.setUsername("admin");
        userEntity.setOpenid("keycloakUserId");
        // Mock external service calls
        when(userResource.create(any(IUserResource.UserDto.class))).thenReturn("keycloakUserId");
        when(mapper.map(userDto, User.class)).thenReturn(userEntity);
        // Mock repository save calls
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        // Mock repository save for audit log
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(mock(AuditLog.class));

        // Call the method
        User result = userService.createAdmin(userDto);

        // Verify the interactions and assertions
        verify(userResource, times(1)).create(userDto);
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));

        // Add more assertions based on the expected behavior of your method
        assertEquals("admin", result.getId()); // Adjust this based on the actual behavior of your method
    }

    @Test
    void testCreateUser() {
        // Mock SecurityContext and Authentication
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock input data
        IUserResource.UserDto userDto = new IUserResource.UserDto();
        userDto.setDepartmentId(UUID.randomUUID()); // Set the departmentId accordingly
        userDto.setUsername("testUser");
        userDto.setRoles(Arrays.asList("ROLE_USER"));

        Department department = new Department();
        department.setSiteId(UUID.fromString("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad")); // Set the siteId accordingly
        department.setCode("VMS");
        when(departmentRepository.findById(userDto.getDepartmentId())).thenReturn(Optional.of(department));


        Site site = new Site();
        site.setCode("VMS"); // Set the code accordingly (e.g. VMS)
        site.setOrganizationId(UUID.randomUUID()); // Set the organizationId accordingly
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(site));

        department.setSite(site);
        // Mock external service calls
        User userEntity = new User();
        userEntity.setUsername("admin");
        userEntity.setOpenid("keycloakUserId");
        // Mock external service calls
        when(userResource.create(any(IUserResource.UserDto.class))).thenReturn("keycloakUserId");
        when(mapper.map(userDto, User.class)).thenReturn(userEntity);
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        // Call the method
        User result = userService.createUser(userDto);

        // Mock repository save calls

        // Mock repository save for audit log
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(mock(AuditLog.class));


        // Verify the interactions and assertions
        verify(siteRepository, times(1)).findById(any(UUID.class));
        verify(userResource, times(1)).create(any(IUserResource.UserDto.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        assertEquals("keycloakUserId", result.getOpenid());
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testUpdateUser() throws NotFoundException {

        // Mock SecurityContext and Authentication
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock input data
        IUserResource.UserDto userDto = new IUserResource.UserDto();
        userDto.setUsername("testUser");
        userDto.setRoles(Arrays.asList("ROLE_USER"));
        userDto.setAvatar("newAvatar");

        User existingUser = new User();
        existingUser.setId("Test"); // Set the user ID accordingly
        existingUser.setUsername("testUser");
        existingUser.setAvatar("oldAvatar");
        existingUser.setOpenid("mockedKcUserId");
        existingUser.setAvatar("oldAvatar");
        existingUser.setDepartment(new Department()); // Set department accordingly

        when(userResource.create(any(IUserResource.UserDto.class))).thenReturn("keycloakUserId");
        when(mapper.map(userDto, User.class)).thenReturn(existingUser);

        Site site = new Site();
        site.setId(UUID.randomUUID());
        site.setOrganizationId(UUID.randomUUID()); // Set organizationId accordingly
        existingUser.getDepartment().setSiteId(UUID.randomUUID()); // Set siteId accordingly

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(existingUser));
        when(userResource.update(any(IUserResource.UserDto.class))).thenReturn(true);
        when(siteRepository.findById(any(UUID.class))).thenReturn(Optional.of(site));

        // Call the method
        User result = null;
        result = userService.updateUser(userDto);


        // Verify the interactions and assertions
        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(userResource, times(1)).update(any(IUserResource.UserDto.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        assertEquals("oldAvatar", result.getAvatar());
        // Add assertions based on the expected structure and content of the result
    }


    @Test
    void testChangePasswordUser() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Mock input data
        String username = "testUser";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User existingUser = new User();
        existingUser.setId("username"); // Set the user ID accordingly
        existingUser.setUsername(username);
        existingUser.setOpenid("mockedKcUserId");

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(existingUser));
        when(userResource.verifyPassword(any(String.class), any(String.class))).thenReturn(true);


        userService.changePasswordUser(username, oldPassword, newPassword);

        // Verify the interactions
        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(userResource, times(1)).verifyPassword(any(String.class), any(String.class));
        verify(userResource, times(1)).changePassword(any(String.class), any(String.class));
    }

    @Test
    void testChangePasswordUserWithInvalidOldPassword() {
        // Mock input data
        String username = "testUser";
        String oldPassword = "invalidOldPassword";
        String newPassword = "newPassword";

        User existingUser = new User();
        existingUser.setId("username"); // Set the user ID accordingly
        existingUser.setUsername(username);
        existingUser.setOpenid("mockedKcUserId");

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(existingUser));
        when(userResource.verifyPassword(any(String.class), any(String.class))).thenReturn(false);

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> userService.changePasswordUser(username, oldPassword, newPassword));

        // Verify the interactions
        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(userResource, times(1)).verifyPassword(any(String.class), any(String.class));
        verify(userResource, never()).changePassword(any(String.class), any(String.class));
    }
}
