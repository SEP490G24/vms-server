package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
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

import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_REALM_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_RESOURCE_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.REALM_ADMIN;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_ORGANIZATION;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    }


    @Test
    void filterTest() {
        Jwt jwt = mock(Jwt.class);

        SecurityUtils.UserDetails userDetails = new SecurityUtils.UserDetails();
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority(PREFIX_REALM_ROLE + REALM_ADMIN),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_ORGANIZATION),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_SITE)
        );

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn(null);
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);
        // Mock the behavior of authentication.getAuthorities() using thenAnswer
        when(authentication.getAuthorities()).thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> {
            userDetails.setRealmAdmin(false);
            userDetails.setOrganizationAdmin(true);
            userDetails.setSiteAdmin(true);

            // Iterate over the authorities and set flags in userDetails
            for (GrantedAuthority grantedAuthority : authorities) {
                switch (grantedAuthority.getAuthority()) {
                    case PREFIX_REALM_ROLE + REALM_ADMIN:
                        userDetails.setRealmAdmin(false);
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
        // Arrange
        List<String> sites = new ArrayList<>();
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
        List<IAuditLogController.AuditLogFilterDTO> result = auditLogService.filter(null, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword
        );

        // Assert
        assertEquals(List.of(), result);
    }

    @Test
    void testFilterForOrganizationAdmin() {
        Jwt jwt = mock(Jwt.class);

        SecurityUtils.UserDetails userDetails = new SecurityUtils.UserDetails();
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority(PREFIX_REALM_ROLE + REALM_ADMIN),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_ORGANIZATION),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_SITE)
        );

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn(null);
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);
        // Mock the behavior of authentication.getAuthorities() using thenAnswer
        when(authentication.getAuthorities()).thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> {
            userDetails.setRealmAdmin(false);
            userDetails.setOrganizationAdmin(true);
            userDetails.setSiteAdmin(true);

            // Iterate over the authorities and set flags in userDetails
            for (GrantedAuthority grantedAuthority : authorities) {
                switch (grantedAuthority.getAuthority()) {
                    case PREFIX_REALM_ROLE + REALM_ADMIN:
                        userDetails.setRealmAdmin(false);
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
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<String> sites = new ArrayList<>();
        Constants.AuditType auditType = Constants.AuditType.CREATE;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createdBy = "mocked_username";
        String tableName = "table";
        String keyword = "search";

        Page<IAuditLogController.AuditLogFilterDTO> mockedPage = new PageImpl<>(List.of());
        // Mock repository behavior
        when(auditLogRepository.filter(
            pageable, null, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword
        )).thenReturn(mockedPage);

        // Mock mapper behavior// Act
        Page<IAuditLogController.AuditLogFilterDTO> result = auditLogService.filter(
            pageableSort, null, sites, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword
        );
        List<String> sites1 = new ArrayList<>();
        sites1.add("06eb43a7-6ea8-4744-8231-760559fe2c07");
        // Assert
        assertEquals(null, result);
        verify(auditLogRepository).filter(
            pageable, null, sites1, auditType, createdOnStart, createdOnEnd, createdBy, tableName, keyword
        );
    }

}
