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

   /* @Test
    public void testFilter() {
        // Mock dữ liệu thử nghiệm
        Pageable pageable = PageRequest.of(0, 10);
        List<String> usernames = Arrays.asList("username1", "username2");
        String role = "ADMIN";
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(1);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "keyword";
        List<String> departmentIds = Arrays.asList("departmentId1", "departmentId2");
        List<String> siteIds = Arrays.asList("siteId1", "siteId2");
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;

        // Mock đối tượng userRepository
        Mockito.when(userRepository.filter(
                pageable,
                usernames,
                role,
                createdOnStart,
                createdOnEnd,
                enable,
                keyword,
                departments,
                provinceId,
                districtId,
                communeId))
            .thenReturn(new PageImpl<>(Arrays.asList(new IUserController.UserFilterResponse())));

        // Gọi hàm filter()
        Page<IUserController.UserFilterResponse> userFilterResponses = userService.filter(pageable, usernames, role, createdOnStart, createdOnEnd, enable, keyword, departmentIds, siteIds, provinceId, districtId, communeId);

        // Kiểm tra kết quả trả về
        assertThat(userFilterResponses).isNotNull();
        assertThat(userFilterResponses.getTotalElements()).isEqualTo(1);

        // Kiểm tra kết quả trả về cụ thể
        assertThat(userFilterResponses.getContent().get(0).getUsername()).isEqualTo("username1");
    }*/

}
