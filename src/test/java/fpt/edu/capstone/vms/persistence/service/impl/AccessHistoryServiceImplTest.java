package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class AccessHistoryServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private CustomerTicketMapRepository customerTicketMapRepository;

    @InjectMocks
    private AccessHistoryServiceImpl accessHistoryService;

    @BeforeEach
    void setUp() {
//        // Create a mock Jwt object with the necessary claims
//        Jwt jwt = mock(Jwt.class);
//        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
//        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
//        when(authentication.getPrincipal()).thenReturn(jwt);
//
//        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
    }

//    @Test
//    public void accessHistoryTest() {
//        // Mock data
//        Pageable pageable = Pageable.unpaged();
//        LocalDateTime fromCheckInTime = LocalDateTime.now().minusDays(7);
//        LocalDateTime toCheckInTime = LocalDateTime.now();
//        LocalDateTime fromCheckOutTime = LocalDateTime.now().minusDays(14);
//        LocalDateTime toCheckOutTime = LocalDateTime.now().minusDays(7);
//        String site = "exampleSite";
//        Constants.StatusTicket status = Constants.StatusTicket.CHECK_IN;
//
//        // Mock the behavior of the repository
//        when(SecurityUtils.getUserDetails().isOrganizationAdmin()).thenReturn(true);
//        when(customerTicketMapRepository.accessHistory(eq(pageable), anyList(), eq(fromCheckInTime), eq(toCheckInTime),
//            eq(fromCheckOutTime), eq(toCheckOutTime), eq(status), anyString(), isNull()))
//            .thenReturn(new PageImpl<>(Collections.emptyList()));
//
//        // Call the method to be tested
//        Page<IAccessHistoryController.AccessHistoryResponseDTO> result = accessHistoryService.accessHistory(
//            pageable, null, status, fromCheckInTime, toCheckInTime, fromCheckOutTime, toCheckOutTime, site);
//
//        // Assertions
//        assertEquals(0, result.getTotalElements());
//    }
//
//    @Test
//    void accessHistory_AdminUser_Success() {
//        when(SecurityUtils.getUserDetails().isOrganizationAdmin()).thenReturn(true);
//        when(customerTicketMapRepository.accessHistory(eq(Pageable.unpaged()), anyList(), any(), any(), any(), any(), any(), anyString(), isNull()))
//            .thenReturn(new PageImpl<>(Collections.emptyList()));
//
//        Page<IAccessHistoryController.AccessHistoryResponseDTO> result = accessHistoryService.accessHistory(
//            Pageable.unpaged(), null, Constants.StatusTicket.CHECK_IN, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "exampleSite");
//
//        assertEquals(0, result.getTotalElements());
//    }
//
//    @Test
//    void accessHistory_NonAdminUser_Success() {
//        when(SecurityUtils.getUserDetails().isOrganizationAdmin()).thenReturn(false);
//        when(customerTicketMapRepository.accessHistory(eq(Pageable.unpaged()), isNull(), any(), any(), any(), any(), any(), anyString(), anyString()))
//            .thenReturn(new PageImpl<>(Collections.emptyList()));
//
//        Page<IAccessHistoryController.AccessHistoryResponseDTO> result = accessHistoryService.accessHistory(
//            Pageable.unpaged(), null, Constants.StatusTicket.CHECK_IN, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "exampleSite");
//
//        assertEquals(0, result.getTotalElements());
//    }
//
//    @Test
//    void viewAccessHistoryDetail_Success() {
//        UUID ticketId = UUID.randomUUID();
//        UUID customerId = UUID.randomUUID();
//        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
//        when(customerTicketMapRepository.findByCustomerTicketMapPk_TicketIdAndCustomerTicketMapPk_CustomerId(ticketId, customerId))
//            .thenReturn(customerTicketMap);
//        assertNotNull(accessHistoryService.viewAccessHistoryDetail(ticketId, customerId));
//    }
//
//    @Test
//    void export_Success() throws JRException {
//        IAccessHistoryController.AccessHistoryFilter filter = new IAccessHistoryController.AccessHistoryFilter();
//        when(accessHistoryService.accessHistory(any(), any(), any(), any(), any(), any(), any(), any()))
//            .thenReturn(new PageImpl<>(Collections.emptyList()));
//
//        assertNotNull(accessHistoryService.export(filter));
//    }
//
//    @Test
//    void getListSite_NonNullSite_Success() {
//        String site = "exampleSite";
//        List<String> result = accessHistoryService.getListSite(site);
//        assertEquals(1, result.size());
//        assertEquals(site, result.get(0));
//    }
//
//    @Test
//    void getListSite_NullSite_OrgIdNotNull_Success() {
//        String orgId = "exampleOrgId";
//        Site site = new Site();
//        site.setId(UUID.randomUUID());
//        site.setName("site1");
//        when(SecurityUtils.getOrgId()).thenReturn(orgId);
//        when(siteRepository.findAllByOrganizationId(UUID.fromString(orgId)))
//            .thenReturn(Collections.singletonList(site));
//
//        List<String> result = accessHistoryService.getListSite(null);
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void getListSite_NullSite_SiteIdNotNull_Success() {
//        String siteId = "exampleSiteId";
//        when(SecurityUtils.getSiteId()).thenReturn(siteId);
//
//        List<String> result = accessHistoryService.getListSite(null);
//        assertEquals(1, result.size());
//    }
}
