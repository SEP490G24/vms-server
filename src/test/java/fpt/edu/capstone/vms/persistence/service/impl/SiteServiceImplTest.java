package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Commune;
import fpt.edu.capstone.vms.persistence.entity.District;
import fpt.edu.capstone.vms.persistence.entity.Province;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.CommuneRepository;
import fpt.edu.capstone.vms.persistence.repository.DistrictRepository;
import fpt.edu.capstone.vms.persistence.repository.ProvinceRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.repository.SettingSiteMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
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
import java.util.Optional;
import java.util.UUID;

import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_REALM_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_RESOURCE_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.REALM_ADMIN;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_ORGANIZATION;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    AuditLogRepository auditLogRepository;

    @Mock
    SettingRepository settingRepository;

    @Mock
    SettingSiteMapRepository settingSiteMapRepository;
    @Mock
    Pageable pageable;

    ModelMapper mapper;
    SiteServiceImpl siteService;
    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        mapper = mock(ModelMapper.class);

        siteService = new SiteServiceImpl(siteRepository, provinceRepository, districtRepository, communeRepository, settingSiteMapRepository, settingRepository, auditLogRepository, mapper);
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
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);
        // Mock the behavior of authentication.getAuthorities() using thenAnswer
        when(authentication.getAuthorities()).thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> {
            userDetails.setRealmAdmin(false);
            userDetails.setOrganizationAdmin(false);
            userDetails.setSiteAdmin(false);

            // Iterate over the authorities and set flags in userDetails
            for (GrantedAuthority grantedAuthority : authorities) {
                switch (grantedAuthority.getAuthority()) {
                    case PREFIX_REALM_ROLE + REALM_ADMIN:
                        userDetails.setRealmAdmin(true);
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
        when(siteRepository.existsByCodeAndOrganizationId("Test", site.getOrganizationId())).thenReturn(false);

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
        savedSite.setId(UUID.randomUUID());
        savedSite.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        savedSite.setCode("validCode");
        savedSite.setEnable(true);
        when(siteRepository.save(site)).thenReturn(savedSite);

        siteService.save(site);

        assertNotNull(savedSite);
        assertEquals(site.getCode(), savedSite.getCode());
        assertTrue(savedSite.getEnable());
    }

    @Test
    @DisplayName("given site, when save site with null code, then throw exception")
    void givenSite_WhenSaveWithNullCode_ThenThrowException() {
        Site site = new Site();
        assertThrows(CustomException.class, () -> siteService.save(site));
    }

    @Test
    @DisplayName("given site, when save site with duplicate code, then throw exception")
    void givenSite_WhenSaveWithDuplicateCode_ThenThrowException() {
        Site site = new Site();
        site.setCode("duplicateCode");
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(siteRepository.existsByCodeAndOrganizationId("test", site.getOrganizationId())).thenReturn(true);

        assertThrows(CustomException.class, () -> siteService.save(site));
    }


    @Test
    @DisplayName("given site, when update site, then site is retrieved")
    void givenSite_WhenUpdateValidSite_ThenSiteRetrieved() {
        UUID orgId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        String code = "validCode";
        UUID id = UUID.randomUUID();

        ISiteController.UpdateSiteInfo updateSiteInfo = new ISiteController.UpdateSiteInfo();

        Site existingSite = new Site();
        existingSite.setId(id);
        existingSite.setName("oldName");
        existingSite.setCode(code);
        existingSite.setOrganizationId(orgId);

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByCodeAndOrganizationId("true", orgId)).thenReturn(false);

        when(siteRepository.findById(id)).thenReturn(Optional.of(existingSite));
        when(siteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any())).thenReturn(new AuditLog());


        Site result = siteService.updateSite(updateSiteInfo, id);

        assertNotNull(result);
        // Verify that siteRepository.findById was called with the correct parameters
        verify(siteRepository).findById(id);

        // Verify that siteRepository.save was called with the correct parameters
        verify(siteRepository).save(any());

        // Verify that auditLogRepository.save was called with the correct parameters
        verify(auditLogRepository).save(any());
    }


    @Test
    void filter() {
        // Given
        List<String> names = Arrays.asList("Site1", "Site2");
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String createBy = "admin";
        String lastUpdatedBy = "admin";
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 70;

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        String keyword = "example";

        List<Site> expectedSites = List.of();
        when(siteRepository.filter(names, UUID.fromString(SecurityUtils.getOrgId()), createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, provinceId, districtId, communeId, keyword.toUpperCase())).thenReturn(expectedSites);

        // When
        List<Site> filteredSites = siteService.filter(names, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, provinceId, districtId, communeId, keyword.toUpperCase());

        // Then
        assertNotNull(filteredSites);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(siteRepository, times(1)).filter(names, UUID.fromString(SecurityUtils.getOrgId()), createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, provinceId, districtId, communeId, keyword.toUpperCase());
    }

    @Test
    void filterPageable() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<String> names = Arrays.asList("Site1", "Site2");
        UUID orgId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String createBy = "admin";
        String lastUpdatedBy = "admin";

        String keyword = "example";
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 70;

        Page<Site> expectedSitePage = new PageImpl<>(List.of());
        when(siteRepository.filter(pageable, names, UUID.fromString(SecurityUtils.getOrgId()), createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, provinceId, districtId, communeId, keyword.toUpperCase())).thenReturn(expectedSitePage);

        // When
        Page<Site> filteredSites = siteService.filter(pageableSort, names, createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, provinceId, districtId, communeId, keyword.toUpperCase());

        // Then
        assertNotNull(filteredSites);
        // Add assertions to check the content of the filteredRooms, depending on the expected behavior
        verify(siteRepository, times(1)).filter(pageable, names, UUID.fromString(SecurityUtils.getOrgId()), createdOnStart, createdOnEnd, createBy, lastUpdatedBy, enable, provinceId, districtId, communeId, keyword.toUpperCase());

    }


    @Test
    void deleteSiteSuccessfulTest() {
        // Arrange
        UUID siteId = UUID.randomUUID(); // a valid UUID
        Site siteEntity = new Site(/* initialize your Site entity */);
        siteEntity.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        siteEntity.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock the behavior of the siteRepository
        when(siteRepository.findById(eq(siteId))).thenReturn(Optional.of(siteEntity));

        // Mock the behavior of SecurityUtils or any other necessary mocks for a successful delete

        // Mock the behavior of settingSiteMapRepository
        List<SettingSiteMap> siteSettingMaps = new ArrayList<>(); // Add some setting maps if needed
        when(settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(eq(siteEntity.getId()))).thenReturn(siteSettingMaps);
        when(auditLogRepository.save(any())).thenReturn(new AuditLog());

        // Act
        boolean result = siteService.deleteSite(siteId);

        // Assert
        assertTrue(result);

        // Verify that the delete method of siteRepository was called with the correct arguments
        verify(siteRepository).delete(eq(siteEntity));

        // Verify that the save method of auditLogRepository was called with the correct arguments
        verify(auditLogRepository).save(
            argThat(auditLog -> auditLog.getAuditType() == Constants.AuditType.DELETE &&
                auditLog.getSiteId().equals(siteEntity.getId().toString()) &&
                auditLog.getOrganizationId().equals(siteEntity.getOrganizationId().toString()) &&
                auditLog.getTableName().equals("Site") &&
                auditLog.getOldValue().equals(siteEntity.toString()) &&
                auditLog.getNewValue() == null)
        );

    }

    @Test
    void deleteSiteNotFoundTest() {
        // Arrange
        UUID siteId = UUID.randomUUID(); // a valid UUID

        // Mock the behavior of the siteRepository
        when(siteRepository.findById(eq(siteId))).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.deleteSite(siteId));
    }

    @Test
    void deleteSiteBadRequestOrganizationMismatchTest() {
        // Arrange
        UUID siteId = UUID.randomUUID(); // a valid UUID
        Site siteEntity = new Site(/* initialize your Site entity */);
        siteEntity.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);


        // Mock the behavior of the siteRepository
        when(siteRepository.findById(eq(siteId))).thenReturn(Optional.of(siteEntity));


        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.deleteSite(siteId));
    }

    @Test
    void deleteSiteBadRequestSiteIdMismatchTest() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Arrange
        UUID siteId = UUID.randomUUID(); // a valid UUID
        Site siteEntity = new Site(/* initialize your Site entity */);
        siteEntity.setId(siteId);

        // Mock the behavior of the siteRepository
        when(siteRepository.findById(eq(siteId))).thenReturn(Optional.of(siteEntity));

        // Mock the behavior of SecurityUtils or any other necessary mocks for a mismatched siteId

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.deleteSite(siteId));
    }

    @Test
    void deleteSiteSettingMapTest() {
        // Arrange
        UUID siteId = UUID.randomUUID();
        Site siteEntity = new Site(/* initialize your Site entity */);
        siteEntity.setId(siteId);
        siteEntity.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        SettingSiteMap settingSiteMapEntity = new SettingSiteMap();
        settingSiteMapEntity.setSettingSiteMapPk(new SettingSiteMapPk(1L, siteId));

        List<SettingSiteMap> siteSettingMaps = new ArrayList<>(); // Add some setting maps if needed
        siteSettingMaps.add(settingSiteMapEntity);
        // Mock the behavior of settingSiteMapRepository
        when(settingSiteMapRepository.findAllBySettingSiteMapPk_SiteId(eq(siteEntity.getId()))).thenReturn(siteSettingMaps);
        when(auditLogRepository.save(any())).thenReturn(new AuditLog());
        // Act
        siteService.deleteSiteSettingMap(siteEntity);

        // Verify that the save method of auditLogRepository was called for each setting map
        for (SettingSiteMap settingSiteMap : siteSettingMaps) {
            verify(auditLogRepository).save(
                argThat(auditLog -> auditLog.getAuditType() == Constants.AuditType.DELETE &&
                    auditLog.getSiteId().equals(siteEntity.getId().toString()) &&
                    auditLog.getOrganizationId().equals(siteEntity.getOrganizationId().toString()) &&
                    auditLog.getTableName().equals("SettingSiteMap") &&
                    auditLog.getOldValue().equals(settingSiteMap.toString()) &&
                    auditLog.getNewValue() == null)
            );
        }
        verify(settingSiteMapRepository).findAllBySettingSiteMapPk_SiteId(eq(siteEntity.getId()));
    }

    @Test
    void testCheckAddress_Success() {
        // Arrange
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;

        Province province = new Province(provinceId, "Province");
        District district = new District(districtId, "District", provinceId, province);
        Commune commune = new Commune(communeId, "Commune", districtId, district);
        // Mock repository behaviors
        when(provinceRepository.findById(provinceId)).thenReturn(Optional.of(province));
        when(districtRepository.findById(districtId)).thenReturn(Optional.of(district));
        when(communeRepository.findById(communeId)).thenReturn(Optional.of(commune));

        // Act
        siteService.checkAddress(provinceId, districtId, communeId);

        // Assert
        // No exceptions should be thrown, and the method should complete successfully
    }

    @Test
    void testCheckAddress_ProvinceNotFound() {
        // Arrange
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;

        // Mock provinceRepository behavior for not found province
        when(provinceRepository.findById(provinceId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.checkAddress(provinceId, districtId, communeId));
    }

    @Test
    void testCheckAddress_ProvinceIdNotFound() {
        // Arrange
        Integer provinceId = null;
        Integer districtId = 2;
        Integer communeId = 3;

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.checkAddress(provinceId, districtId, communeId));
    }

    @Test
    void testCheckAddress_DistrictNotFound() {
        // Arrange
        Integer provinceId = 1;
        Integer districtId = null;
        Integer communeId = 3;

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.checkAddress(provinceId, districtId, communeId));
    }

    @Test
    void testCheckAddress_DistrictIdNotFound() {
        // Arrange
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;

        // Mock repository behaviors
        when(provinceRepository.findById(provinceId)).thenReturn(Optional.of(new Province(provinceId, "Province")));
        when(districtRepository.findById(districtId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.checkAddress(provinceId, districtId, communeId));
    }

    @Test
    void testCheckAddress_CommuneNotFound() {
        // Arrange
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;

        Province province = new Province(provinceId, "Province");
        District district = new District(districtId, "District", provinceId, province);

        // Mock repository behaviors
        when(provinceRepository.findById(provinceId)).thenReturn(Optional.of(province));
        when(districtRepository.findById(districtId)).thenReturn(Optional.of(district));
        when(communeRepository.findById(communeId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.checkAddress(provinceId, districtId, communeId));
    }

    @Test
    void testCheckAddress_CommuneIdNotFound() {
        // Arrange
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = null;

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.checkAddress(provinceId, districtId, communeId));
    }

    @Test
    void testCheckAddress_WrongDistrictProvince() {
        // Arrange
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;


        Province province = new Province(provinceId, "Province");
        Province provinceEx = new Province(4, "Province");
        District district = new District(districtId, "District", 4, provinceEx);

        // Mock repository behaviors
        when(provinceRepository.findById(provinceId)).thenReturn(Optional.of(province));
        when(districtRepository.findById(districtId)).thenReturn(Optional.of(district)); // Wrong province ID

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.checkAddress(provinceId, districtId, communeId));
    }

    @Test
    void testCheckAddress_WrongCommuneDistrict() {
        // Arrange
        Integer provinceId = 1;
        Integer districtId = 2;
        Integer communeId = 3;

        Province province = new Province(provinceId, "Province");
        District district = new District(districtId, "District", provinceId, province);
        District districtEX = new District(4, "District", provinceId, province);
        Commune commune = new Commune(communeId, "Commune", 4, districtEX);

        // Mock repository behaviors
        when(provinceRepository.findById(provinceId)).thenReturn(Optional.of(province));
        when(districtRepository.findById(districtId)).thenReturn(Optional.of(district));
        when(communeRepository.findById(communeId)).thenReturn(Optional.of(commune)); // Wrong district ID

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.checkAddress(provinceId, districtId, communeId));
    }

    @Test
    void testUpdateSite_SiteNotFound() {
        // Arrange
        UUID siteId = UUID.randomUUID();

        ISiteController.UpdateSiteInfo updateSiteInfo = new ISiteController.UpdateSiteInfo();
        updateSiteInfo.setName("Updated Site");
        Site site = new Site();
        site.setId(siteId);
        site.setAddress("abc");
        when(mapper.map(updateSiteInfo, Site.class)).thenReturn(site);
        when(siteRepository.findById(siteId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.updateSite(updateSiteInfo, siteId));
        verify(siteRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void testUpdateSite_CurrentUserInSiteIdNull() {
        // Arrange
        UUID siteId = UUID.randomUUID();

        ISiteController.UpdateSiteInfo updateSiteInfo = new ISiteController.UpdateSiteInfo();
        updateSiteInfo.setName("Updated Site");

        Site existingSite = new Site();
        existingSite.setId(siteId);
        existingSite.setOrganizationId(UUID.randomUUID());

        when(mapper.map(updateSiteInfo, Site.class)).thenReturn(existingSite);
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(existingSite));

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.updateSite(updateSiteInfo, siteId));
        verify(siteRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void testUpdateSite_NoPermissionWithSiteId() {
        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Arrange
        UUID siteId = UUID.randomUUID();

        ISiteController.UpdateSiteInfo updateSiteInfo = new ISiteController.UpdateSiteInfo();
        updateSiteInfo.setName("Updated Site");

        Site existingSite = new Site();
        existingSite.setId(siteId);

        when(mapper.map(updateSiteInfo, Site.class)).thenReturn(existingSite);
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(existingSite));

        // Act and Assert
        assertThrows(CustomException.class, () -> siteService.updateSite(updateSiteInfo, siteId));
        verify(siteRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void testFindByIdWithValidOrgId() {
        UUID id = UUID.randomUUID();
        UUID orgId = UUID.fromString("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");

        // Mock the behavior of siteRepository.findById to return a Site with the specified organizationId
        Site site = new Site();
        site.setId(id);
        site.setOrganizationId(orgId);
        when(siteRepository.findById(id)).thenReturn(java.util.Optional.of(site));

        // Act
        Site result = siteService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());

        // Verify that siteRepository.findById was called with the correct parameters
        verify(siteRepository).findById(id);
    }

//    @Test
//    void testFindByIdWithInvalidOrgId() {
//        // Arrange
//        UUID id = UUID.randomUUID();
//        UUID expectedOrgId = UUID.randomUUID();
//
//        // Mock the behavior of siteRepository.findById to return a Site with a different organizationId
//        Site site = new Site();
//        site.setId(id);
//        site.setOrganizationId(expectedOrgId);
//        when(siteRepository.findById(id)).thenReturn(java.util.Optional.of(site));
//
//        // Act and Assert
//        assertThrows(CustomException.class, () -> siteService.findById(id));
//
//        // Verify that siteRepository.findById was called with the correct parameters
//        verify(siteRepository).findById(id);
//    }

//    @Test
//    void testFindByIdWithInvalidSiteId() {
//        // Create a mock Jwt object with the necessary claims
//        Jwt jwt = mock(Jwt.class);
//        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
//        when(authentication.getPrincipal()).thenReturn(jwt);
//
//        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//
//        // Arrange
//        UUID id = UUID.randomUUID();
//        UUID expectedOrgId = UUID.randomUUID();
//
//        // Mock the behavior of siteRepository.findById to return a Site with a different organizationId
//        Site site = new Site();
//        site.setId(id);
//        site.setOrganizationId(expectedOrgId);
//        when(siteRepository.findById(id)).thenReturn(java.util.Optional.of(site));
//
//        // Act and Assert
//        assertThrows(CustomException.class, () -> siteService.findById(id));
//
//        // Verify that siteRepository.findById was called with the correct parameters
//        verify(siteRepository).findById(id);
//    }
}
