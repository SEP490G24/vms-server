package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class DepartmentServiceImplTest {

    private DepartmentServiceImpl departmentService;

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private SiteRepository siteRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    private ModelMapper mapper;

    @Mock
    Pageable pageable;

    SecurityContext securityContext;
    Authentication authentication;

    @Mock
    private UserRepository userRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        mapper = mock(ModelMapper.class);
        departmentService = new DepartmentServiceImpl(departmentRepository, mapper, siteRepository, auditLogRepository, userRepository);
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    @DisplayName("given incomplete data, when department with null code, then exception is thrown")
    void givenDepartment_WhenSaveWithNullCode_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode("");
        departmentInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        assertThrows(CustomException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when department with null object, then exception is thrown")
    void givenDepartment_WhenSaveWithNullObject_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = null;

        assertThrows(CustomException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    void testCreateDepartment_SiteNotFound() {
        // Arrange
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        // Mock siteRepository behavior
        when(siteRepository.findById(UUID.fromString(departmentInfo.getSiteId().toString()))).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(CustomException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    void testCreateDepartment_NotPermission() {
        // Arrange
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c07");
        // Mock siteRepository behavior
        when(siteRepository.findById(UUID.fromString(departmentInfo.getSiteId().toString()))).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(CustomException.class, () -> departmentService.createDepartment(departmentInfo));
    }


    @Test
    @DisplayName("given incomplete data, when department with null siteId, then exception is thrown")
    void givenDepartment_WhenSaveWithNullSiteId_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode("validCode");

        assertThrows(CustomException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when create new department, then department is save")
    void givenDepartment_WhenSaveValidDepartment_ThenCreateNewDepartment() {

        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode("code");
        departmentInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        Department department = new Department();
        department.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(departmentRepository.existsByCodeAndSiteId(departmentInfo.getCode(), UUID.fromString(departmentInfo.getSiteId()))).thenReturn(false);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        when(siteRepository.findById(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")))
            .thenReturn(Optional.of(site)); // Adjust based on your actual Site entity

        // Mock departmentRepository save method

        // Mock auditLogRepository save method
        when(auditLogRepository.save(Mockito.any())).thenReturn(new AuditLog());
        when(departmentRepository.save(department)).thenReturn(department);
        when(mapper.map(departmentInfo, Department.class)).thenReturn(department);
        department.setEnable(true);
        // Act
        Department result = departmentService.createDepartment(departmentInfo);

        assertNotNull(department);

    }

    @Test
    @DisplayName("given incomplete data, when update department with existing code, then exception is thrown")
    void givenDepartmentId_WhenUpdateWithExistingCode_ThenThrowException() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("existingCode");
        updateDepartmentInfo.setSiteId(UUID.randomUUID());

        when(departmentRepository.existsByCodeAndSiteId(updateDepartmentInfo.getCode(), updateDepartmentInfo.getSiteId())).thenReturn(true);
        assertThrows(CustomException.class, () -> departmentService.update(updateDepartmentInfo, id));
    }

    @Test
    @DisplayName("given incomplete data, when update department with non existing department, then exception is thrown")
    void givenDepartmentId_WhenUpdateWithNonExistingDepartment_ThenThrowException() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("newCode");

        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> departmentService.update(updateDepartmentInfo, id));
    }


    @Test
    void givenDepartmentId_WhenUpdateValidDepartment_ThenUpdateDepartment() {
        UUID id = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("newCode");
        updateDepartmentInfo.setEnable(true);

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Department existingDepartment = new Department();
        existingDepartment.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        existingDepartment.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(departmentRepository.findById(id)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.existsByCodeAndSiteId(updateDepartmentInfo.getCode(), id)).thenReturn(false);
        existingDepartment.setSite(site);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(departmentRepository.save(existingDepartment.update(updateDepartmentInfo))).thenReturn(existingDepartment);
        Department updatedDepartment = new Department();
        updatedDepartment.setCode("newCode");

        when(siteRepository.findById(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"))).thenReturn(Optional.of(site));

        when(departmentRepository.save(existingDepartment.update(updateDepartmentInfo))).thenReturn(updatedDepartment);

        Department result = departmentService.update(updateDepartmentInfo, id);
        assertNotNull(result);
        assertEquals(updateDepartmentInfo.getCode(), updatedDepartment.getCode());

    }

    @Test
    void filter() {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<String> names = Arrays.asList("Department1", "Department2");
        List<String> siteId = Arrays.asList("06eb43a7-6ea8-4744-8231-760559fe2c08", "06eb43a7-6ea8-4744-8231-760559fe2c09");
        List<UUID> sites = Arrays.asList(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"), UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);
        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c09")).thenReturn(true);

        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String createBy = "admin";
        String lastUpdatedBy = "admin";

        String keyword = "example";

        List<Department> departmentList = List.of();
        when(departmentRepository.filter(names, sites, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase())).thenReturn(departmentList);

        // When
        List<Department> filteredSites = departmentService.filter(names, siteId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

        // Then
        assertNotNull(filteredSites);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(departmentRepository, times(1)).filter(names, sites, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());
    }

    @Test
    void filterPageable() {

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<String> names = Arrays.asList("Department1", "Department2");
        List<String> siteId = Arrays.asList("06eb43a7-6ea8-4744-8231-760559fe2c08", "06eb43a7-6ea8-4744-8231-760559fe2c09");
        List<UUID> sites = Arrays.asList(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"), UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);
        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c09")).thenReturn(true);

        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String createBy = "admin";
        String lastUpdatedBy = "admin";

        String keyword = "example";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Department> expectedSitePage = new PageImpl<>(List.of());
        when(departmentRepository.filter(pageable, names, sites, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase())).thenReturn(expectedSitePage);

        // When
        Page<Department> filteredSites = departmentService.filter(pageableSort, names, siteId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

        // Then
        assertNotNull(filteredSites);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(departmentRepository, times(1)).filter(pageable, names, sites, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

    }

    @Test
    public void testFilterWithNoOrgIdAndNonNullSiteId() {
        List<String> siteId = new ArrayList<>();
        try {
            departmentService.filter(new ArrayList<>(), siteId, null, null, null, null, null, null);
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            assertEquals("403 You don't have permission to do this.", ex.getMessage());
        }
    }

    @Test
    public void testFilterWithOrgIdAndNullSiteId() {
        when(siteRepository.findAllByOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"))).thenReturn(new ArrayList<>());
        List<Department> departments = new ArrayList<>();
        when(departmentRepository.filter(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(departments);
        List<String> siteId = new ArrayList<>();
        List<Department> result = departmentService.filter(new ArrayList<>(), siteId, null, null, null, null, null, null);
        assertEquals(departments, result);
    }

    @Test
    void testFindAllBySiteId_Forbidden() {
        // Arrange
        String siteId = "06eb43a7-6ea8-4744-8231-760559fe2c07";
        // Act and Assert
        assertThrows(CustomException.class, () -> departmentService.FindAllBySiteId(siteId));
    }

    @Test
    void testDeleteDepartment() {
        // Mock input data
        UUID departmentId = UUID.randomUUID();

        // Mock department repository behavior
        Department department = new Department();
        department.setId(departmentId);
        department.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")); // Set a valid siteId for authorization check
        when(departmentRepository.findById(departmentId)).thenReturn(java.util.Optional.of(department));

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(siteRepository.findById(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"))).thenReturn(Optional.of(site));
        department.setSite(site);
        // Call the method
        assertDoesNotThrow(() -> departmentService.deleteDepartment(departmentId));
    }

    @Test
    void testDeleteDepartmentWithNoPermission() {
        // Mock input data
        UUID departmentId = UUID.randomUUID();

        // Mock department repository behavior
        Department department = new Department();
        department.setId(departmentId);
        department.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07")); // Set an invalid siteId for authorization check
        when(departmentRepository.findById(departmentId)).thenReturn(java.util.Optional.of(department));

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> departmentService.deleteDepartment(departmentId));
    }

    @Test
    void testFindByDepartmentId() {
        // Mock input data
        UUID departmentId = UUID.randomUUID();

        // Mock department repository behavior
        Department department = new Department();
        department.setId(departmentId);
        department.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")); // Set a valid siteId for authorization check
        when(departmentRepository.findById(departmentId)).thenReturn(java.util.Optional.of(department));

        // Call the method
        IDepartmentController.DepartmentFilterDTO result = departmentService.findByDepartmentId(departmentId);

    }

    @Test
    void testFindByDepartmentIdWithNoPermission() {
        // Mock input data
        UUID departmentId = UUID.randomUUID();

        // Mock department repository behavior
        Department department = new Department();
        department.setId(departmentId);
        department.setSiteId(UUID.randomUUID()); // Set an invalid siteId for authorization check
        when(departmentRepository.findById(departmentId)).thenReturn(java.util.Optional.of(department));

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> departmentService.findByDepartmentId(departmentId));

        // Verify the interactions
        verify(departmentRepository, times(1)).findById(departmentId);
    }

}
