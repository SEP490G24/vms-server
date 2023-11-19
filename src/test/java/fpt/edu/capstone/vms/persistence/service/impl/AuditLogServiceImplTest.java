package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

        // Mock the repository filter method
        when(auditLogRepository.filter(any(List.class), any(List.class), any(Constants.AuditType.class),
            any(LocalDateTime.class), any(LocalDateTime.class), any(String.class), any(String.class), any(String.class)))
            .thenReturn(new ArrayList());

        // Call the method under test
        List<IAuditLogController.AuditLogFilterDTO> result = auditLogService.filter(organizations, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword
        );

        // Assert
        assertEquals(null, result);
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

        // Verify that the result is an empty page
        assertEquals(null, result);

        // You can add more assertions if needed
    }
}
