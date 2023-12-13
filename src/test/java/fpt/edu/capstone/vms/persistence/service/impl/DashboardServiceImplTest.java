package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.dto.dashboard.MultiLineResponse;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.DashboardRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
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
import java.util.UUID;

import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_REALM_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_RESOURCE_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.REALM_ADMIN;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_ORGANIZATION;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class   DashboardServiceImplTest {

    DashboardRepository dashboardRepository;
    SiteRepository siteRepository;
    CustomerRepository customerRepository;
    CustomerTicketMapRepository customerTicketMapRepository;
    DashboardServiceImpl dashboardService;
    ModelMapper mapper;

    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    void Setup() {
        MockitoAnnotations.openMocks(this);

        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);

        dashboardRepository = mock(DashboardRepository.class);
        siteRepository = mock(SiteRepository.class);
        customerRepository = mock(CustomerRepository.class);
        customerTicketMapRepository = mock(CustomerTicketMapRepository.class);
        mapper = mock(ModelMapper.class);
        dashboardService = new DashboardServiceImpl(dashboardRepository, siteRepository,customerTicketMapRepository,customerRepository,mapper);

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
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
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
        dashboardDTO.setSiteId(new ArrayList<>()); // Replace with your desired site names

        LocalDateTime firstDay = LocalDateTime.of(2023, 12, 1, 0, 0);
        LocalDateTime lastDay = LocalDateTime.of(2023, 12, 31, 23, 59, 59);

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

    }

    @Test
    void testCountTicketsByPurposeWithMonth() {
        // Test data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023); // Replace with your desired year
        dashboardDTO.setMonth(11); // Replace with your desired month
        dashboardDTO.setSiteId(new ArrayList<>()); // Replace with your desired site names

        // Mock behavior
        LocalDateTime firstDay = LocalDateTime.of(2023, 11, 1, 0, 0);
        LocalDateTime lastDay = LocalDateTime.of(2023, 11, 30, 23, 59, 59);


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
    }

    private boolean isValidPercentageValue(Object value) {
        if (value instanceof Long) {
            return true; // Long is a valid percentage value
        } else if (value instanceof String) {
            try {
                Double.parseDouble((String) value);
                return true;
            } catch (NumberFormatException e) {
                return false; // Invalid percentage value
            }
        }
        return false; // Other types are considered invalid
    }

    @Test
    void testCountTicketsByPurposeWithYear() {
        // Test data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023); // Replace with your desired year
        dashboardDTO.setSiteId(new ArrayList<>()); // Replace with your desired site names

        // Mock behavior
        LocalDateTime firstDay = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime lastDay = LocalDateTime.of(2023, 12, 31, 23, 59, 59);

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
    }

    @Test
    void testCountTicketsByPurpose() {
        // Test data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023); // Replace with your desired year
        dashboardDTO.setSiteId(new ArrayList<>()); // Replace with your desired site names

        // Mock behavior
        LocalDateTime firstDay = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime lastDay = LocalDateTime.of(2023, 12, 31, 23, 59, 59);

        // Example mock data from your repository
        List<Object[]> mockData = Arrays.asList(
            new Object[]{Constants.Purpose.MEETING, 5L},
            new Object[]{Constants.Purpose.MEETING, 10L}
            // Add more data as needed
        );

        List<String> siteId = new ArrayList<>();
        siteId.add("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(dashboardRepository.countTicketsByPurposeWithPie(firstDay, lastDay, siteId)).thenReturn(mockData);

        // Execute the method
        List<IDashboardController.PurposePieResponse> result = dashboardService.countTicketsByPurposeWithPie(dashboardDTO);

        // Assertions
        assertNotNull(result);
        // Add more assertions based on your expected result
    }

    @Test
    void testCountTicketsByPurposeByWithMultiLine_TimeMonth() {
        // Arrange
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setMonth(11);
        dashboardDTO.setSiteId(new ArrayList<>());

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
        assertEquals(20, result.size());
    }

    @Test
    void testCountTicketsByPurposeByWithMultiLine_TimeYear() {
        // Arrange
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        assertEquals(20, result.size());
    }

    @Test
    void testCountTicketsByStatus_TimeYear() {
        // Mock input data (dashboardDTO)
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setMonth(11);
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        assertNotNull(result);
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testCountVisitsByStatusWithStackedColumn_month() {

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setMonth(11);
        dashboardDTO.setSiteId(new ArrayList<>());

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

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
        // Add assertions based on the expected structure and content of the result
    }

    @Test
    void testCountVisitsByStatusWithStackedColumn_year() {

        // Mock input data
        IDashboardController.DashboardDTO dashboardDTO = new IDashboardController.DashboardDTO();
        dashboardDTO.setYear(2023);
        dashboardDTO.setSiteId(new ArrayList<>());

        List<Object[]> repositoryResponse = Arrays.asList(
            new Object[]{"2023-11-01", "Purpose1", 5},
            new Object[]{"2023-11-02", "Purpose2", 10}
            // Add more rows as needed
        );

        List<Constants.StatusCustomerTicket> purpose = new ArrayList<>();
        purpose.add(Constants.StatusCustomerTicket.REJECT);
        purpose.add(Constants.StatusCustomerTicket.CHECK_IN);
        purpose.add(Constants.StatusCustomerTicket.CHECK_OUT);
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
        dashboardDTO.setSiteId(new ArrayList<>());

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
        dashboardDTO.setSiteId(new ArrayList<>());

        // Mock external service calls
        when(dashboardRepository.getUpcomingMeetings(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
            .thenReturn(Arrays.asList(new Ticket()));
        when(dashboardRepository.getOngoingMeetings(
            any(LocalDateTime.class),
            anyList(),
            eq(Constants.StatusTicket.PENDING)))
            .thenReturn(Arrays.asList(new Ticket()));
        when(dashboardRepository.getRecentlyFinishedMeetings(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(Constants.StatusTicket.COMPLETE)))
            .thenReturn(Arrays.asList(new Ticket()));

        when(siteRepository.findAllByOrganizationId(UUID.randomUUID())).thenReturn(Arrays.asList());
        // Mock repository calls
        when(mapper.map(any(List.class), any())).thenReturn(Arrays.asList(new ITicketController.TicketFilterDTO()));
        // Call the method
        IDashboardController.TicketsPeriodResponse result = dashboardService.countTicketsPeriod(dashboardDTO);

        // Verify the interactions and assertions
        verify(dashboardRepository, times(1)).getUpcomingMeetings(any(LocalDateTime.class), any(LocalDateTime.class), anyList());
        verify(dashboardRepository, times(1)).getOngoingMeetings(any(LocalDateTime.class), anyList(), eq(Constants.StatusTicket.PENDING));
        verify(dashboardRepository, times(1)).getRecentlyFinishedMeetings(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(Constants.StatusTicket.COMPLETE));

        // Add more assertions based on the expected behavior of your method
        assertNotNull(result);
    }
}
