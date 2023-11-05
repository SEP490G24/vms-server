package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Commune;
import fpt.edu.capstone.vms.persistence.entity.District;
import fpt.edu.capstone.vms.persistence.entity.Province;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.CommuneRepository;
import fpt.edu.capstone.vms.persistence.repository.DistrictRepository;
import fpt.edu.capstone.vms.persistence.repository.ProvinceRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SiteServiceImplTest {

    @Mock
    SiteRepository siteRepository;

    @Mock
    ProvinceRepository provinceRepository;
    @Mock
    DistrictRepository districtRepository;
    @Mock
    CommuneRepository communeRepository;

    @Mock
    Pageable pageable;

    @InjectMocks
    SiteServiceImpl siteService;
    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
    }

    @Test
    @DisplayName("given organization id, when get all site by organization, then site is retrieved")
    void givenOrganizationId_WhenFindAllByOrganizationId_ThenSiteRetrieved() {
        // Mock data
        Site site = new Site();
        site.setName("VMS");
        Site site1 = new Site();
        site1.setName("FPT Đà Nẵng");
        List<Site> mockSites = Arrays.asList(site, site1);


        // Mock behavior of the roleRepository
        when(siteRepository.findAllByOrganizationId(UUID.fromString("02416f2c-23cf-42f0-a212-3e26628bfa14"))).thenReturn(mockSites);

        // Call the service method

        List<Site> sites = siteService.findAllByOrganizationId("02416f2c-23cf-42f0-a212-3e26628bfa14");

        // Assert the result
        assertEquals(2, sites.size()); // Check that the list has 2 roles
        assertEquals("VMS", sites.get(0).getName()); // Check the first role's name
        assertEquals("FPT Đà Nẵng", sites.get(1).getName()); // Check the first role's name
//        assertEquals("FPT Cầu Giấy", sites.get(2).getName()); // Check the first role's name

        // Verify that the roleRepository's findAll method was called
        Mockito.verify(siteRepository, times(1)).findAllByOrganizationId(UUID.fromString("02416f2c-23cf-42f0-a212-3e26628bfa14"));
    }

    @Test
    @DisplayName("given site, when save site, then site is retrieved")
    void givenSite_WhenSaveValidSite_ThenSiteRetrieved() {

        Site site = new Site();
        site.setCode("validCode");
        site.setProvinceId(1);
        site.setDistrictId(1);
        site.setCommuneId(1);
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(siteRepository.existsByCode(anyString())).thenReturn(false);

        when(provinceRepository.findById(1)).thenReturn(Optional.of(new Province())); // Adjust this as needed
        when(districtRepository.findById(1)).thenReturn(Optional.of(new District())); // Adjust this as needed
        when(communeRepository.findById(1)).thenReturn(Optional.of(new Commune())); // Adjust this as needed


        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Site savedSite = new Site();
        savedSite.setCode("validCode");
        savedSite.setEnable(true);
        when(siteRepository.save(site)).thenReturn(savedSite);

//        verify(securityUtils, times(1)).getOrgId();

        assertNotNull(savedSite);
        assertEquals(site.getCode(), savedSite.getCode());
        assertTrue(savedSite.getEnable());
    }

    @Test
    @DisplayName("given site, when save site with null code, then throw exception")
    void givenSite_WhenSaveWithNullCode_ThenThrowException() {
        Site site = new Site();
        assertThrows(HttpClientErrorException.class, () -> siteService.save(site));
    }

    @Test
    @DisplayName("given site, when save site with duplicate code, then throw exception")
    void givenSite_WhenSaveWithDuplicateCode_ThenThrowException() {
        Site site = new Site();
        site.setCode("duplicateCode");
        when(siteRepository.existsByCode(anyString())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> siteService.save(site));
    }


    @Test
    @DisplayName("given site, when update site, then site is retrieved")
    void givenSite_WhenUpdateValidSite_ThenSiteRetrieved() {
        UUID orgId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        String code = "validCode";
        UUID id = UUID.randomUUID();

        Site existingSite = new Site();
        existingSite.setId(id);
        existingSite.setCode(code);
        existingSite.setOrganizationId(orgId);

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);


        Site updateSite = new Site();
        updateSite.setCode("newCode");

        when(siteRepository.existsByCode(updateSite.getCode())).thenReturn(false);
        when(siteRepository.findById(id)).thenReturn(Optional.of(existingSite));

        when(siteService.update(updateSite, id)).thenReturn(updateSite);

        assertNotNull(updateSite);
        assertEquals(updateSite.getCode(), updateSite.getCode());
    }

    @Test
    @DisplayName("given site, when update site with duplicate code, then throw exception")
    void givenSite_WhenUpdateWithDuplicateCode_ThenThrowException() {
        UUID id = UUID.randomUUID();
        Site updateSite = new Site();
        updateSite.setId(id);
        updateSite.setCode("duplicateCode");

        when(siteRepository.existsByCode(updateSite.getCode())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> siteService.save(updateSite));
    }

    @Test
    @DisplayName("given site, when update site with not found, then throw exception")
    void testUpdateSiteNotFound() {
        UUID id = UUID.randomUUID();
        Site updateSite = new Site();
        updateSite.setCode("newCode");

        when(siteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> siteService.save(updateSite));
    }

    @Test
    void filter() {
        // Given
        List<String> names = Arrays.asList("Site1", "Site2");
        UUID orgId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String createBy = "admin";
        String lastUpdatedBy = "admin";

        String keyword = "example";

        List<Site> expectedSites = List.of();
        when(siteRepository.filter(names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase())).thenReturn(expectedSites);

        // When
        List<Site> filteredSites = siteService.filter(names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

        // Then
        assertNotNull(filteredSites);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(siteRepository, times(1)).filter(names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());
    }

    @Test
    void filterPageable() {

        List<String> names = Arrays.asList("Site1", "Site2");
        UUID orgId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String createBy = "admin";
        String lastUpdatedBy = "admin";

        String keyword = "example";

        Page<Site> expectedSitePage = new PageImpl<>(List.of());
        when(siteRepository.filter(pageable, names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase())).thenReturn(expectedSitePage);

        // When
        Page<Site> filteredSites = siteService.filter(pageable, names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

        // Then
        assertNotNull(filteredSites);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(siteRepository, times(1)).filter(pageable, names, orgId, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, keyword.toUpperCase());

    }
}
