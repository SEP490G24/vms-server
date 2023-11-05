package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class DepartmentServiceImplTest {

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ModelMapper mapper;

    @Mock
    Pageable pageable;

    SecurityContext securityContext;
    Authentication authentication;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
    }


    @Test
    @DisplayName("given incomplete data, when department with null code, then exception is thrown")
    void givenDepartment_WhenSaveWithNullCode_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode(null);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when department with existing siteId, then exception is thrown")
    void givenDepartment_WhenSaveWithExistingCode_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode("existingCode");

        when(departmentRepository.existsByCode(departmentInfo.getCode())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when department with null object, then exception is thrown")
    void givenDepartment_WhenSaveWithNullObject_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = null;

        assertThrows(NullPointerException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when department with null siteId, then exception is thrown")
    void givenDepartment_WhenSaveWithNullSiteId_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode("validCode");
        departmentInfo.setSiteId(null);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when create new department, then department is save")
    void givenDepartment_WhenSaveValidDepartment_ThenCreateNewDepartment() {

        UUID siteId = UUID.randomUUID();
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode(siteId.toString());
        departmentInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Department department = new Department();
        when(departmentRepository.existsByCode(departmentInfo.getCode())).thenReturn(false);
//        when(mapper.map(departmentInfo, Department.class)).thenReturn(department);

        department.setEnable(true);
        department.setCode(departmentInfo.getCode());
        department.setSiteId(siteId);
        Department savedDepartment = departmentRepository.save(department);

        assertNotNull(department);
        assertTrue(department.getEnable());
    }

    @Test
    @DisplayName("given incomplete data, when update department with existing code, then exception is thrown")
    void givenDepartmentId_WhenUpdateWithExistingCode_ThenThrowException() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("existingCode");

        when(departmentRepository.existsByCode(updateDepartmentInfo.getCode())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> departmentService.update(updateDepartmentInfo, id));
    }

    @Test
    @DisplayName("given incomplete data, when update department with non existing department, then exception is thrown")
    void givenDepartmentId_WhenUpdateWithNonExistingDepartment_ThenThrowException() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("newCode");

        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> departmentService.update(updateDepartmentInfo, id));
    }


    @Test
    void givenDepartmentId_WhenUpdateValidDepartment_ThenUpdateDepartment() {
        UUID id = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("newCode");

        Department existingDepartment = new Department();
        when(departmentRepository.findById(id)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.existsByCode(updateDepartmentInfo.getCode())).thenReturn(false);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Department updatedDepartment = new Department();
        updatedDepartment.setCode("newCode");
        when(departmentRepository.save(existingDepartment.update(updateDepartmentInfo))).thenReturn(updatedDepartment);

        assertNotNull(updatedDepartment);
        assertEquals(updateDepartmentInfo.getCode(), updatedDepartment.getCode());
    }

    @Test
    void filter() {
        // Given
        List<String> names = Arrays.asList("Department1", "Department2");
        UUID orgId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String createBy = "admin";
        String lastUpdatedBy = "admin";

        String keyword = "example";

        List<Department> departmentList = List.of();
        when(departmentRepository.filter(names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase())).thenReturn(departmentList);

        // When
        List<Department> filteredSites = departmentService.filter(names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

        // Then
        assertNotNull(filteredSites);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(departmentRepository, times(1)).filter(names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());
    }

    @Test
    void filterPageable() {

        List<String> names = Arrays.asList("Department1", "Department2");
        UUID orgId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String createBy = "admin";
        String lastUpdatedBy = "admin";

        String keyword = "example";

        Page<Department> expectedSitePage = new PageImpl<>(List.of());
        when(departmentRepository.filter(pageable, names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase())).thenReturn(expectedSitePage);

        // When
        Page<Department> filteredSites = departmentService.filter(pageable, names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

        // Then
        assertNotNull(filteredSites);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(departmentRepository, times(1)).filter(pageable, names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

    }
}
