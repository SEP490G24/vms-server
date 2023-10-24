package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

    @Mock
    SiteRepository siteRepository;

    @InjectMocks
    SiteServiceImpl siteService;

    @BeforeEach
    public void setup() {
//        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllByOrganizationId() {
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
        Mockito.verify(siteRepository, Mockito.times(1)).findAllByOrganizationId(UUID.fromString("02416f2c-23cf-42f0-a212-3e26628bfa14"));
    }
}
