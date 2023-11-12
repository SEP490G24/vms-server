package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@ActiveProfiles("test")
@Tag("UnitTest")
@DisplayName("User Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    Pageable pageable;
    @Mock
    SecurityContext securityContext;
    @Mock
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

    @Mock
    ModelMapper mapper;
    @Mock
    DepartmentRepository departmentRepository;
    @Mock
    AuditLogRepository auditLogRepository;

/*    @Test
    void testFilter() {
        // Mock input parameters
        Pageable pageable = mock(Pageable.class);
        List<String> usernames = new ArrayList<>();
        String role = "ROLE_USER";
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "search";
        List<String> departmentIds = new ArrayList<>();
        departmentIds.add("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        List<String> siteIds = new ArrayList<>();
        siteIds.add("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;

        // Mock the result you expect from userRepository.filter
        List<IUserController.UserFilterResponse> expectedResult = new ArrayList<>();
        Page<IUserController.UserFilterResponse> expectedPage = new PageImpl<>(expectedResult);

        // Mock dependencies
        when(userRepository.filter(pageable, ))
            .thenReturn(expectedPage);
        when(userService.getListDepartments(siteIds, departmentIds)).thenReturn(new ArrayList<>());

        // Call the actual method
        Page<IUserController.UserFilterResponse> result = userService.filter(
            pageable,
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

        // Verify that userRepository.filter was called with the correct parameters
        verify(userRepository).filter(
            pageable,
            usernames,
            role,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword,
            new ArrayList<>(), // Since getListDepartments is mocked, an empty list is expected here
            provinceId,
            districtId,
            communeId
        );

        // Verify that the result is as expected
        assertEquals(expectedPage, result);
    }*/

}
