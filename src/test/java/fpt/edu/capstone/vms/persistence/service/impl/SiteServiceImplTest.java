package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SiteServiceImplTest {

    @Mock
    SiteRepository siteRepository;

    @Mock
    private SecurityUtils securityUtils;


    @InjectMocks
    SiteServiceImpl siteService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
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

//    @Test
//    @DisplayName("given site, when save site, then site is retrieved")
//    void givenSite_WhenSaveValidSite_ThenSiteRetrieved() {
//        Site site = new Site();
//        site.setCode("validCode");
//        when(siteRepository.existsByCode(anyString())).thenReturn(false);
//
//        Site savedSite = siteService.save(site);
//
//        assertNotNull(savedSite);
//        assertEquals(site.getCode(), savedSite.getCode());
//        assertTrue(savedSite.getEnable());
//        verify(siteRepository, times(1)).save(site);
//    }

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


//    @Test
//    @DisplayName("given site, when update site, then site is retrieved")
//    void givenSite_WhenUpdateValidSite_ThenSiteRetrieved() {
//        UUID orgId = UUID.randomUUID();
//        String code = "validCode";
//        UUID id = UUID.randomUUID();
//
//        Site existingSite = new Site();
//        existingSite.setId(id);
//        existingSite.setCode(code);
//        existingSite.setOrganizationId(orgId);
//
//        Site updateSite = new Site();
//        updateSite.setCode("newCode");
//
//        when(siteRepository.existsByCode(updateSite.getCode())).thenReturn(false);
//        when(siteRepository.findById(id)).thenReturn(Optional.of(existingSite));
//        when(securityUtils.getOrgId()).thenReturn(orgId.toString());
//
//        Site updatedSite = siteService.update(updateSite, id);
//
//        assertNotNull(updatedSite);
//        assertEquals(updateSite.getCode(), updatedSite.getCode());
//        verify(siteRepository, times(1)).save(existingSite.update(updateSite));
//    }

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

        assertThrows(NullPointerException.class, () -> siteService.save(updateSite));
    }
}
