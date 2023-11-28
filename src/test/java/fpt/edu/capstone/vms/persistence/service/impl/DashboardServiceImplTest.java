package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.persistence.dto.dashboard.MultiLineResponse;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.DashboardRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceImplTest {

    DashboardRepository dashboardRepository;
    SiteRepository siteRepository;
    DashboardServiceImpl dashboardService;

    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    void Setup() {
        MockitoAnnotations.openMocks(this);

        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);

        dashboardRepository = mock(DashboardRepository.class);
        siteRepository = mock(SiteRepository.class);
        dashboardService = new DashboardServiceImpl(dashboardRepository, siteRepository);

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
    void testCountTicketsByPurposeWithPie() {
        // Test data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setSites(new ArrayList<>()); // Replace with your desired site names

        LocalDateTime firstDay = LocalDateTime.of(2023, 11, 1, 0, 0);
        LocalDateTime lastDay = LocalDateTime.of(2023, 11, 30, 0, 0, 0);

        // Example mock data from your repository
        List<Object[]> mockData = Arrays.asList(
            new Object[]{Constants.Purpose.MEETING, 5L},
            new Object[]{Constants.Purpose.MEETING, 10L}
            // Add more data as needed
        );
        when(dashboardRepository.countTicketsByPurposeWithPie(firstDay, lastDay, new ArrayList<>())).thenReturn(mockData);

        // Execute the method
        List<IDashboardController.PurposePieResponse> result = dashboardService.countTicketsByPurposeWithPie(dashboardDTO);

        // Assertions
        assertNotNull(result);
        // Add more assertions based on your expected result

        // Verify method calls
        verify(dashboardRepository).countTicketsByPurposeWithPie(firstDay, lastDay, new ArrayList<>());
    }

    @Test
    void testCountTicketsByPurposeWithMonth() {
        // Test data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023); // Replace with your desired year
        dashboardDTO.setMonth(11); // Replace with your desired month
        dashboardDTO.setSites(new ArrayList<>()); // Replace with your desired site names

        // Mock behavior
        LocalDateTime firstDay = LocalDateTime.of(2023, 11, 1, 0, 0);
        LocalDateTime lastDay = LocalDateTime.of(2023, 11, 30, 0, 0, 0);

        // Example mock data from your repository
        List<Object[]> mockData = Arrays.asList(
            new Object[]{Constants.Purpose.MEETING, 5L},
            new Object[]{Constants.Purpose.MEETING, 10L}
            // Add more data as needed
        );
        when(dashboardRepository.countTicketsByPurposeWithPie(firstDay, lastDay, new ArrayList<>())).thenReturn(mockData);

        // Execute the method
        List<IDashboardController.PurposePieResponse> result = dashboardService.countTicketsByPurposeWithPie(dashboardDTO);

        // Assertions
        assertNotNull(result);
        // Add more assertions based on your expected result

        // Verify method calls
        verify(dashboardRepository).countTicketsByPurposeWithPie(firstDay, lastDay, new ArrayList<>());
    }

    @Test
    void testCountTicketsByPurposeWithYear() {
        // Test data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023); // Replace with your desired year
        dashboardDTO.setSites(new ArrayList<>()); // Replace with your desired site names

        // Mock behavior
        LocalDateTime firstDay = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime lastDay = LocalDateTime.of(2023, 12, 31, 0, 0, 0);

        // Example mock data from your repository
        List<Object[]> mockData = Arrays.asList(
            new Object[]{Constants.Purpose.MEETING, 5L},
            new Object[]{Constants.Purpose.MEETING, 10L}
            // Add more data as needed
        );
        when(dashboardRepository.countTicketsByPurposeWithPie(firstDay, lastDay, new ArrayList<>())).thenReturn(mockData);

        // Execute the method
        List<IDashboardController.PurposePieResponse> result = dashboardService.countTicketsByPurposeWithPie(dashboardDTO);

        // Assertions
        assertNotNull(result);
        // Add more assertions based on your expected result

        // Verify method calls
        verify(dashboardRepository).countTicketsByPurposeWithPie(firstDay, lastDay, new ArrayList<>());
    }

    @Test
    void testCountTicketsByPurpose() {
        // Test data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023); // Replace with your desired year
        dashboardDTO.setSites(new ArrayList<>()); // Replace with your desired site names

        // Mock behavior
        LocalDateTime firstDay = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime lastDay = LocalDateTime.of(2023, 12, 31, 0, 0, 0);

        // Example mock data from your repository
        List<Object[]> mockData = Arrays.asList(
            new Object[]{Constants.Purpose.MEETING, 5L},
            new Object[]{Constants.Purpose.MEETING, 10L}
            // Add more data as needed
        );
        when(dashboardRepository.countTicketsByPurposeWithPie(firstDay, lastDay, new ArrayList<>())).thenReturn(mockData);

        // Execute the method
        List<IDashboardController.PurposePieResponse> result = dashboardService.countTicketsByPurposeWithPie(dashboardDTO);

        // Assertions
        assertNotNull(result);
        // Add more assertions based on your expected result

        // Verify method calls
        verify(dashboardRepository).countTicketsByPurposeWithPie(firstDay, lastDay, new ArrayList<>());
    }

    @Test
    void testCountTicketsByPurposeByWithMultiLine_TimeMonth() {
        // Arrange
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setMonth(11);
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        // Mock dependencies
        when(dashboardRepository.countTicketsByPurposeWithMultiLine(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
            .thenReturn(repositoryResponse);

        // Act
        List<MultiLineResponse> result = dashboardService.countTicketsByPurposeByWithMultiLine(dashboardDTO);

        // Assert
        assertEquals(25, result.size());
    }

    @Test
    void testCountTicketsByPurposeByWithMultiLine_TimeYear() {
        // Arrange
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        // Mock dependencies
        when(dashboardRepository.countTicketsByPurposeWithMultiLine(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
            .thenReturn(repositoryResponse);

        // Act
        List<MultiLineResponse> result = dashboardService.countTicketsByPurposeByWithMultiLine(dashboardDTO);

        // Assert
        assertEquals(60, result.size());
    }

    @Test
    void testCountTicketsByPurposeByWithMultiLine_TimeFull() {
        // Arrange
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        // Mock dependencies
        when(dashboardRepository.countTicketsByPurposeWithMultiLine(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
            .thenReturn(repositoryResponse);

        // Act
        List<MultiLineResponse> result = dashboardService.countTicketsByPurposeByWithMultiLine(dashboardDTO);

        // Assert
        assertEquals(25, result.size());
    }

    @Test
    void testCountTicketsByStatus_TimeYear() {
        // Mock input data (dashboardDTO)
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setMonth(11);
        dashboardDTO.setSites(new ArrayList<>());

        // Mock repository response
        when(dashboardRepository.countTotalTickets(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList())).thenReturn(50);

        // Call the method
        IDashboardController.TotalTicketResponse result = dashboardService.countTicketsByStatus(dashboardDTO);

        // Verify the result
        assertEquals(0, result.getTotalTicket());
        // Add more assertions based on the expected behavior of your method
    }

    @Test
    void testCountTicketsByStatus_TimeMonth() {
        // Mock input data (dashboardDTO)
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setSites(new ArrayList<>());

        // Mock repository response
        when(dashboardRepository.countTotalTickets(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList())).thenReturn(50);

        // Call the method
        IDashboardController.TotalTicketResponse result = dashboardService.countTicketsByStatus(dashboardDTO);

        // Verify the result
        assertEquals(0, result.getTotalTicket());
        // Add more assertions based on the expected behavior of your method
    }

    @Test
    void testCountTicketsByStatus() {
        // Mock input data (dashboardDTO)
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setSites(new ArrayList<>());

        // Mock repository response
        when(dashboardRepository.countTotalTickets(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList())).thenReturn(50);

        // Call the method
        IDashboardController.TotalTicketResponse result = dashboardService.countTicketsByStatus(dashboardDTO);

        // Verify the result
        assertEquals(0, result.getTotalTicket());
        // Add more assertions based on the expected behavior of your method
    }

    @Test
    void testCountVisitsByStatus_TimeYear() {

        // Mock input data (dashboardDTO)
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setMonth(11);
        dashboardDTO.setSites(new ArrayList<>());

        // Mock repository response
        when(dashboardRepository.countTotalVisits(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList())).thenReturn(50);

        // Call the method
        IDashboardController.TotalVisitsResponse result = dashboardService.countVisitsByStatus(dashboardDTO);

        // Verify the result
        assertEquals(0, result.getTotalVisits());
        // Add more assertions based on the expected behavior of your method
    }

    @Test
    void testCountVisitsByStatus_TimeMonth() {

        // Mock input data (dashboardDTO)
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setSites(new ArrayList<>());

        // Mock repository response
        when(dashboardRepository.countTotalVisits(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList())).thenReturn(50);

        // Call the method
        IDashboardController.TotalVisitsResponse result = dashboardService.countVisitsByStatus(dashboardDTO);

        // Verify the result
        assertEquals(0, result.getTotalVisits());
        // Add more assertions based on the expected behavior of your method
    }

    @Test
    void testCountVisitsByStatus() {

        // Mock input data (dashboardDTO)
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setSites(new ArrayList<>());

        // Mock repository response
        when(dashboardRepository.countTotalVisits(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList())).thenReturn(50);

        // Call the method
        IDashboardController.TotalVisitsResponse result = dashboardService.countVisitsByStatus(dashboardDTO);

        // Verify the result
        assertEquals(0, result.getTotalVisits());
        // Add more assertions based on the expected behavior of your method
    }

    @Test
    void testCountTicketsByStatusWithStackedColumn() {

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        // Mock external service calls
        when(dashboardRepository.countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList()))
            .thenReturn(repositoryResponse);


        // Call the method
        List<MultiLineResponse> result = dashboardService.countTicketsByStatusWithStackedColumn(dashboardDTO);

        // Verify the interactions and assertions
        verify(dashboardRepository, times(1)).countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList());

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testCountTicketsByStatusWithStackedColumn_year() {

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        // Mock external service calls
        when(dashboardRepository.countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList()))
            .thenReturn(repositoryResponse);


        // Call the method
        List<MultiLineResponse> result = dashboardService.countTicketsByStatusWithStackedColumn(dashboardDTO);

        // Verify the interactions and assertions
        verify(dashboardRepository, times(1)).countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList());

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testCountTicketsByStatusWithStackedColumn_month() {

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setMonth(11);
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        // Mock external service calls
        when(dashboardRepository.countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList()))
            .thenReturn(repositoryResponse);


        // Call the method
        List<MultiLineResponse> result = dashboardService.countTicketsByStatusWithStackedColumn(dashboardDTO);

        // Verify the interactions and assertions
        verify(dashboardRepository, times(1)).countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList());

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testCountVisitsByStatusWithStackedColumn_month() {

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setMonth(11);
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        // Mock external service calls
        when(dashboardRepository.countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList()))
            .thenReturn(repositoryResponse);


        // Call the method
        List<MultiLineResponse> result = dashboardService.countVisitsByStatusWithStackedColumn(dashboardDTO);

        // Verify the interactions and assertions
        verify(dashboardRepository, times(1)).countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList());

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testCountVisitsByStatusWithStackedColumn_year() {

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        List<Constants.StatusTicket> purpose = new ArrayList<>();
        purpose.add(Constants.StatusTicket.REJECT);
        purpose.add(Constants.StatusTicket.CHECK_IN);
        purpose.add(Constants.StatusTicket.CHECK_OUT);
        List<String> sites = new ArrayList<>();

        // Mock external service calls
        when(dashboardRepository.countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList()))
            .thenReturn(repositoryResponse);


        // Call the method
        List<MultiLineResponse> result = dashboardService.countVisitsByStatusWithStackedColumn(dashboardDTO);

        assertNotNull(result);
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testCountVisitsByStatusWithStackedColumn() {

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setSites(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        // Mock external service calls
        when(dashboardRepository.countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList()))
            .thenReturn(repositoryResponse);


        // Call the method
        List<MultiLineResponse> result = dashboardService.countVisitsByStatusWithStackedColumn(dashboardDTO);

        // Verify the interactions and assertions
        verify(dashboardRepository, times(1)).countTicketsByStatusWithStackedColumn(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyList());

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testCountTicketsPeriod() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setSites(new ArrayList<>());

        // Mock external service calls
        when(dashboardRepository.getUpcomingMeetings(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
            .thenReturn(Arrays.asList(new Ticket(/* Your mocked data */)));
        when(dashboardRepository.getOngoingMeetings(any(LocalDateTime.class), anyList()))
            .thenReturn(Arrays.asList(new Ticket(/* Your mocked data */)));
        when(dashboardRepository.getRecentlyFinishedMeetings(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
            .thenReturn(Arrays.asList(new Ticket(/* Your mocked data */)));

        // Mock repository calls

        // Call the method
        IDashboardController.TicketsPeriodResponse result = dashboardService.countTicketsPeriod(dashboardDTO);

        // Verify the interactions and assertions
        verify(dashboardRepository, times(1)).getUpcomingMeetings(any(LocalDateTime.class), any(LocalDateTime.class), anyList());
        verify(dashboardRepository, times(1)).getOngoingMeetings(any(LocalDateTime.class), anyList());
        verify(dashboardRepository, times(1)).getRecentlyFinishedMeetings(any(LocalDateTime.class), any(LocalDateTime.class), anyList());

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        assertNotNull(result.getUpcomingMeetings());
        assertNotNull(result.getOngoingMeetings());
        assertNotNull(result.getRecentlyFinishedMeetings());
        // Add assertions based on the expected structure and content of the result
    }
}