package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;
    @Mock
    private SiteRepository siteRepository;
    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        MockitoAnnotations.initMocks(this);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    void filterTest() {
        // Arrange
        List<String> organizations = Arrays.asList("Org1", "Org2");
        List<String> sites = Arrays.asList("Site1", "Site2");
        Constants.AuditType auditType = Constants.AuditType.CREATE;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createdBy = "John Doe";
        String tableName = "SomeTable";
        String keyword = "SearchKeyword";

        // Mock the behavior of the auditLogRepository
        List<IAuditLogController.AuditLogFilterDTO> expectedResult = Arrays.asList(
            new IAuditLogController.AuditLogFilterDTO(/* initialize your DTO */));

        when(auditLogRepository.filter(
            eq(organizations), eq(sites), eq(auditType),
            eq(createdOnStart), eq(createdOnEnd), eq(createdBy),
            eq(tableName), eq(keyword)))
            .thenReturn(expectedResult);

        // Act
        List<IAuditLogController.AuditLogFilterDTO> result = auditLogService.filter(
            organizations, sites, auditType, createdOnStart,
            createdOnEnd, createdBy, tableName, keyword);

        // Assert
        assertEquals(expectedResult, result);

        // Verify that the filter method of auditLogRepository was called with the correct arguments
        verify(auditLogRepository).filter(
            eq(organizations), eq(sites), eq(auditType),
            eq(createdOnStart), eq(createdOnEnd), eq(createdBy),
            eq(tableName), eq(keyword));
    }


    @Test
    void testFilterForOrganizationAdmin() {
        // Mock data
        Pageable pageable = Pageable.unpaged();
        List<String> organizations = Arrays.asList("org1", "org2");
        List<String> sites = Arrays.asList("site1", "site2");
        Constants.AuditType auditType = Constants.AuditType.CREATE;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createdBy = "mocked_username";
        String tableName = "table";
        String keyword = "search";

        // Mock the repository filter method
        when(auditLogRepository.filter(any(Pageable.class), any(List.class), any(List.class), any(Constants.AuditType.class),
            any(LocalDateTime.class), any(LocalDateTime.class), any(String.class), any(String.class), any(String.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Call the method under test
        Page<IAuditLogController.AuditLogFilterDTO> result = auditLogService.filter(
            pageable, organizations, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword
        );

        // Verify that the repository filter method was called with the correct arguments
        Mockito.verify(auditLogRepository).filter(pageable, organizations, null, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword);

        // Verify that the result is an empty page
        assertEquals(null, result);

        // You can add more assertions if needed
    }
}
