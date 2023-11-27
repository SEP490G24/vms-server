package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@TestInstance(PER_CLASS)
@ActiveProfiles("test")
@Tag("UnitTest")
@DisplayName("User Service Unit Tests")
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;
    SecurityContext securityContext;
    Authentication authentication;

    @Mock
    SiteRepository siteRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    FileRepository fileRepository;
    @Mock
    FileServiceImpl fileService;
    @Mock
    IUserResource userResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    ModelMapper mapper;
    @Mock
    DepartmentRepository departmentRepository;
    @Mock
    AuditLogRepository auditLogRepository;


    @Test
    void testFilter() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
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

//        // Verify that userRepository.filter was called with the correct parameters
//        verify(userRepository).filter(
//            pageable,
//            usernames,
//            role,
//            createdOnStart,
//            createdOnEnd,
//            enable,
//            keyword,
//            departmentIds1, // Since getListDepartments is mocked, an empty list is expected here
//            provinceId,
//            districtId,
//            communeId
//        );

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
}
