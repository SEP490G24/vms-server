package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.UUID;

import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_REALM_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_RESOURCE_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.REALM_ADMIN;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_ORGANIZATION;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccessHistoryServiceImplTest {

    private TicketRepository ticketRepository;
    private SiteRepository siteRepository;
    private CustomerTicketMapRepository customerTicketMapRepository;

    private AccessHistoryServiceImpl accessHistoryService;

    SecurityContext securityContext;
    Authentication authentication;
    ModelMapper mapper;

    Pageable pageable;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        mapper = mock(ModelMapper.class);
        pageable = mock(Pageable.class);
        ticketRepository = mock(TicketRepository.class);
        siteRepository = mock(SiteRepository.class);
        customerTicketMapRepository = mock(CustomerTicketMapRepository.class);

        accessHistoryService = new AccessHistoryServiceImpl(ticketRepository, mapper, siteRepository, customerTicketMapRepository);
        Jwt jwt = mock(Jwt.class);

        SecurityUtils.UserDetails userDetails = new SecurityUtils.UserDetails();
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority(PREFIX_REALM_ROLE + REALM_ADMIN),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_ORGANIZATION),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_SITE)
        );


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
            userDetails.setOrganizationAdmin(false);
            userDetails.setSiteAdmin(false);

            // Iterate over the authorities and set flags in userDetails
            for (GrantedAuthority grantedAuthority : authorities) {
                switch (grantedAuthority.getAuthority()) {
                    case PREFIX_REALM_ROLE + REALM_ADMIN:
                        userDetails.setRealmAdmin(false);
                        break;
                    case PREFIX_RESOURCE_ROLE + SCOPE_ORGANIZATION:
                        userDetails.setOrganizationAdmin(false);
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
    void testAccessHistory_OrganizationAdmin_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<Constants.StatusCustomerTicket> status = List.of(Constants.StatusCustomerTicket.CHECK_IN, Constants.StatusCustomerTicket.CHECK_OUT);
        LocalDateTime formCheckInTime = LocalDateTime.now().minusDays(7);
        LocalDateTime toCheckInTime = LocalDateTime.now();
        LocalDateTime formCheckOutTime = LocalDateTime.now().minusDays(14);
        LocalDateTime toCheckOutTime = LocalDateTime.now().minusDays(7);
        List<String> sites = new ArrayList<>();
        // Mock repository behavior
        Page<CustomerTicketMap> customerTicketMapPage = new PageImpl<>(List.of());
        when(customerTicketMapRepository.accessHistory(
            pageable,
            sites,
            formCheckInTime,
            toCheckInTime,
            formCheckOutTime,
            toCheckOutTime,
            status,
            null,
            null
        )).thenReturn(customerTicketMapPage);

        // Mock mapper behavior// Act
        Page<CustomerTicketMap> result = accessHistoryService.accessHistory(
            pageableSort, null, status, formCheckInTime, toCheckInTime, formCheckOutTime, toCheckOutTime, sites
        );
        List<String> sites1 = new ArrayList<>();
        sites1.add("06eb43a7-6ea8-4744-8231-760559fe2c07");
        // Assert
        assertEquals(null, result);
        verify(customerTicketMapRepository).accessHistory(
            pageable,
            sites1,
            formCheckInTime,
            toCheckInTime,
            formCheckOutTime,
            toCheckOutTime,
            status,
            null,
            null
        );
    }

    @Test
    void testViewAccessHistoryDetail() {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();


        // Mocking behavior
        CustomerTicketMap customerTicketMap = new CustomerTicketMap(); // Create a sample CustomerTicketMap
        when(customerTicketMapRepository.findByCheckInCode(anyString()))
            .thenReturn(customerTicketMap);

        IAccessHistoryController.AccessHistoryResponseDTO expectedResponseDTO = new IAccessHistoryController.AccessHistoryResponseDTO();
        when(mapper.map(customerTicketMap, IAccessHistoryController.AccessHistoryResponseDTO.class))
            .thenReturn(expectedResponseDTO);

        // Act
        IAccessHistoryController.AccessHistoryResponseDTO result = accessHistoryService.viewAccessHistoryDetail(anyString());

        // Assert
        assertEquals(expectedResponseDTO, result);

        // Verify that customerTicketMapRepository.findByCustomerTicketMapPk_TicketIdAndCustomerTicketMapPk_CustomerId was called with the correct parameters
        verify(customerTicketMapRepository).findByCheckInCode(anyString());

        // Verify that mapper.map was called with the correct parameters
        verify(mapper).map(customerTicketMap, IAccessHistoryController.AccessHistoryResponseDTO.class);
    }
}
