//package fpt.edu.capstone.vms.util;
//
//import fpt.edu.capstone.vms.constants.Constants;
//import fpt.edu.capstone.vms.persistence.entity.Site;
//import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
//import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.web.client.HttpClientErrorException;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.when;
//@ExtendWith(MockitoExtension.class)
//@Tag("UnitTest")
//@DisplayName("SecurityUtils Unit Tests")
//class SecurityUtilsTest {
//
//    @Mock
//    private Authentication authentication;
//
//    @Mock
//    private Jwt jwt;
//
//    @Mock
//    private SiteRepository siteRepository;
//
//    @Mock
//    private DepartmentRepository departmentRepository;
//
//    @InjectMocks
//    private SecurityUtils securityUtils;
//
//    @BeforeEach
//    void setUp() {
//        when(authentication.getPrincipal()).thenReturn(jwt);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//
//
//    @Test
//    @DisplayName("given valid Authentication, when getUserDetails, then UserDetails is returned")
//    void givenValidAuthentication_whenGetUserDetails_thenUserDetailsReturned() {
//        // Given
//        when(authentication.getPrincipal()).thenReturn(jwt);
//        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("John Doe");
//        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("john.doe");
//        // ... (mock other claims and authorities as needed)
//
//        // When
//        SecurityUtils.UserDetails userDetails = securityUtils.getUserDetails();
//
//        // Then
//        assertNotNull(userDetails);
//        assertEquals("John Doe", userDetails.getName());
//        assertEquals("john.doe", userDetails.getPreferredUsername());
//        // ... (assert other details)
//    }
//
//    @Test
//    @DisplayName("given valid Authentication, when getOrgId, then orgId is returned")
//    void givenValidAuthentication_whenGetOrgId_thenOrgIdReturned() {
//        // Given
//        when(authentication.getPrincipal()).thenReturn(jwt);
//        when(jwt.getClaim("name")).thenReturn("organization123");
//
//        // When
//        String orgId = securityUtils.getOrgId();
//
//        // Then
//        assertNotNull(orgId);
//        assertEquals("organization123", orgId);
//    }
//
//
//
//
//    @Test
//    @DisplayName("given valid siteId, when checkSiteAuthorization, then true is returned")
//    void givenValidSiteId_whenCheckSiteAuthorization_thenTrueReturned() {
//        // Given
//        when(authentication.getPrincipal()).thenReturn(jwt);
//        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("organization123");
//        when(siteRepository.existsByIdAndOrganizationId(any(), any())).thenReturn(true);
//
//        // When
//        boolean result = securityUtils.checkSiteAuthorization(siteRepository, "site123");
//
//        // Then
//        assertTrue(result);
//    }
//
//    @Test
//    @DisplayName("given invalid siteId, when checkSiteAuthorization, then false is returned")
//    void givenInvalidSiteId_whenCheckSiteAuthorization_thenFalseReturned() {
//        // Given
//        when(authentication.getPrincipal()).thenReturn(jwt);
//        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("organization123");
//        when(siteRepository.existsByIdAndOrganizationId(any(), any())).thenReturn(false);
//
//        // When
//        boolean result = securityUtils.checkSiteAuthorization(siteRepository, "invalidSiteId");
//
//        // Then
//        assertFalse(result);
//    }
//
//    @Test
//    @DisplayName("given valid siteId and siteId list, when getListSite, then list of UUIDs is returned")
//    void givenValidSiteIdAndList_whenGetListSite_thenListOfUUIDsReturned() {
//        // Given
//        when(authentication.getPrincipal()).thenReturn(jwt);
//        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("organization123");
//        when(siteRepository.findAllByOrganizationId(any())).thenReturn(List.of(new Site()));
//
//        // When
//        List<UUID> result = securityUtils.getListSite(siteRepository, List.of("site123"));
//
//        // Then
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//        assertEquals(1, result.size());
//        assertEquals(UUID.class, result.get(0).getClass());
//    }
//
//    @Test
//    @DisplayName("given null orgId and siteId list, when getListSite, then HttpClientErrorException is thrown")
//    void givenNullOrgIdAndSiteIdList_whenGetListSite_thenHttpClientErrorExceptionThrown() {
//        // Given
//        when(authentication.getPrincipal()).thenReturn(jwt);
//        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn(null);
//
//        // When and Then
//        assertThrows(HttpClientErrorException.class, () -> securityUtils.getListSite(siteRepository, List.of("site123")));
//    }
//
//    // Add more test cases for other methods as needed
//
//    // You can also add tests for checkDepartmentInSite, getSiteId, and other methods.
//
//    // Consider adding tests for edge cases, invalid input, etc.
//}
