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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

    @Mock
    SiteRepository siteRepository;

    @InjectMocks
    SiteServiceImpl siteService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllByOrganizationId() {
        // Mock data
        Site site = new Site();
        site.setName("VMS");
        site.setEnable(true);
        List<Site> mockSites = Arrays.asList(site);


        // Mock behavior of the roleRepository
        when(siteRepository.findAllByOrganizationId(UUID.fromString(""))).thenReturn(mockSites);

        // Call the service method

        List<Site> sites = siteService.findAllByOrganizationId("");

        // Assert the result
        assertEquals(2, sites.size()); // Check that the list has 2 roles
        assertEquals("ROLE_ADMIN", sites.get(0).getName()); // Check the first role's name
        assertEquals("ROLE_USER", sites.get(1).getName()); // Check the second role's name

        // Verify that the roleRepository's findAll method was called
        Mockito.verify(siteRepository, Mockito.times(1)).findAll();
    }
}
