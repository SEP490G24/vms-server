package fpt.edu.capstone.vms.persistence.service.impl;

import com.google.zxing.WriterException;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.*;
import fpt.edu.capstone.vms.persistence.repository.*;
import fpt.edu.capstone.vms.persistence.service.sse.SseEmitterManager;
import fpt.edu.capstone.vms.util.EmailUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import fpt.edu.capstone.vms.util.SettingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static fpt.edu.capstone.vms.constants.Constants.Purpose.MEETING;
import static fpt.edu.capstone.vms.constants.Constants.Purpose.OTHERS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketServiceImpl ticketService;

    private TicketRepository ticketRepository;
    private AuditLogRepository auditLogRepository;

    private TemplateRepository templateRepository;

    private SiteRepository siteRepository;

    private EmailUtils emailUtils;

    private SettingUtils settingUtils;
    private AuditLogServiceImpl auditLogService;
    private UserRepository userRepository;

    private RoomRepository roomRepository;
    private CustomerRepository customerRepository;
    private OrganizationRepository organizationRepository;
    private ReasonRepository reasonRepository;

    private CustomerTicketMapRepository customerTicketMapRepository;

    SecurityContext securityContext;
    Authentication authentication;
    SseEmitterManager sseEmitterManager;

    ModelMapper mapper;

    @BeforeEach
    public void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        mapper = mock(ModelMapper.class);
        ticketRepository = mock(TicketRepository.class);
        siteRepository = mock(SiteRepository.class);
        settingUtils = mock(SettingUtils.class);
        templateRepository = mock(TemplateRepository.class);
        roomRepository = mock(RoomRepository.class);
        customerTicketMapRepository = mock(CustomerTicketMapRepository.class);
        emailUtils = mock(EmailUtils.class);
        auditLogRepository = mock(AuditLogRepository.class);
        sseEmitterManager = mock(SseEmitterManager.class);
        reasonRepository = mock(ReasonRepository.class);
        userRepository = mock(UserRepository.class);
        customerRepository = mock(CustomerRepository.class);

        ticketService = new TicketServiceImpl(ticketRepository
            , customerRepository, templateRepository
            , mapper, roomRepository, siteRepository
            , organizationRepository, customerTicketMapRepository
            , emailUtils, auditLogRepository, settingUtils, userRepository, reasonRepository, sseEmitterManager);
    }

    @Test
    @DisplayName("Given Draft Ticket, When Creating, Then Set Status to DRAFT")
    public void givenDraftTicket_WhenCreating_ThenSetStatusToDraft() {
//        List<ICustomerController.NewCustomers> newCustomers = Collections.singletonList(
//            new ICustomerController.NewCustomers("John Doe", "123456789112", "john@example.com", null, null, null, null, null, null));

        List<String> oldCustomer = new ArrayList<>();
        oldCustomer.add("06eb43a7-6ea8-4744-8231-760559fe2c09");

        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(true);
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().minusMinutes(30));
        ticketInfo.setEndTime(LocalDateTime.now());
        ticketInfo.setNewCustomers(null);
        ticketInfo.setOldCustomers(oldCustomer);
        ticketInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId()))).thenReturn(java.util.Optional.of(site));

        Customer customer = new Customer();
        customer.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        when(customerRepository.existsByIdAndAndOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"), site.getOrganizationId().toString())).thenReturn(true);


        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        ticket.setStatus(Constants.StatusTicket.DRAFT);
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        Ticket result = ticketService.create(ticketInfo);

        assertNotNull(result);
        assertEquals(Constants.StatusTicket.DRAFT, ticket.getStatus());
    }

    @Test
    @DisplayName("Given Pending Ticket, When Creating, Then Set Status to PENDING")
    public void givenPendingTicket_WhenCreating_ThenSetStatusToPending() {
        List<String> oldCustomer = new ArrayList<>();
        oldCustomer.add("06eb43a7-6ea8-4744-8231-760559fe2c09");

        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().minusMinutes(30));
        ticketInfo.setEndTime(LocalDateTime.now());
        ticketInfo.setNewCustomers(null);
        ticketInfo.setOldCustomers(oldCustomer);
        ticketInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.randomUUID());
        site.setAddress("address");
        when(siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId()))).thenReturn(java.util.Optional.of(site));

        Customer customer = new Customer();
        customer.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        customer.setIdentificationNumber("123456789012");
        when(customerRepository.existsByIdAndAndOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"), site.getOrganizationId().toString())).thenReturn(true);


        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        ticket.setStatus(Constants.StatusTicket.PENDING);
        ticket.setPurpose(Constants.Purpose.MEETING);
        ticket.setStartTime(LocalDateTime.now());
        ticket.setEndTime(LocalDateTime.now().plusHours(1));
        ticket.setUsername("mocked_username");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(customer.getId(), ticket.getId()));
        List<CustomerTicketMap> customerTicketMaps = new ArrayList<>();
        customerTicketMaps.add(customerTicketMap);
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId())).thenReturn(customerTicketMaps);
        when(customerRepository.findById(customerTicketMap.getCustomerTicketMapPk().getCustomerId())).thenReturn(Optional.of(customer));

        Template template = new Template();
        template.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        when(settingUtils.getOrDefault(eq(Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL))).thenReturn(template.getId().toString());
        when(templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL)))).thenReturn(Optional.of(template));
        Room room = new Room();
        room.setName("abc");
        when(roomRepository.findById(ticket.getRoomId())).thenReturn(Optional.of(room));
        User user = new User();
        user.setFirstName("Nguyen");
        user.setLastName("Test");
        user.setPhoneNumber("098554xxx");
        user.setEmail("email");
        when(userRepository.findFirstByUsername(ticket.getUsername())).thenReturn(user);

        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        Ticket result = ticketService.create(ticketInfo);

        assertNotNull(result);
        assertEquals(Constants.StatusTicket.PENDING, ticket.getStatus());
    }

    @Test
    @DisplayName("Given Pending Ticket, When Creating, Then Set Status to PENDING")
    public void givenTicketWithRoomNotInSite_WhenCreating_ThenThrowException() {
        List<String> oldCustomer = new ArrayList<>();
        oldCustomer.add("06eb43a7-6ea8-4744-8231-760559fe2c09");

        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().minusMinutes(30));
        ticketInfo.setEndTime(LocalDateTime.now());
        ticketInfo.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        ticketInfo.setNewCustomers(null);
        ticketInfo.setOldCustomers(oldCustomer);
        ticketInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId()))).thenReturn(java.util.Optional.of(site));

        Room room = new Room();
        room.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c05"));
        room.setName("abc");
        when(roomRepository.findById(ticketInfo.getRoomId())).thenReturn(Optional.of(room));

        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        ticket.setStatus(Constants.StatusTicket.PENDING);
        ticket.setPurpose(Constants.Purpose.MEETING);
        ticket.setStartTime(LocalDateTime.now());
        ticket.setEndTime(LocalDateTime.now().plusHours(1));
        ticket.setUsername("mocked_username");

        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);

        HttpClientErrorException errorException = assertThrows(HttpClientErrorException.class, () -> ticketService.create(ticketInfo));
        assertEquals("400 User can not create meeting in this room", errorException.getMessage());
    }

    @Test
    @DisplayName("Given Pending Ticket, When Creating, Then Set Status to PENDING")
    public void givenTicketWithRoomNull_WhenCreating_ThenThrowException() {
        List<String> oldCustomer = new ArrayList<>();
        oldCustomer.add("06eb43a7-6ea8-4744-8231-760559fe2c09");

        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().minusMinutes(30));
        ticketInfo.setEndTime(LocalDateTime.now());
        ticketInfo.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        ticketInfo.setNewCustomers(null);
        ticketInfo.setOldCustomers(oldCustomer);
        ticketInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId()))).thenReturn(java.util.Optional.of(site));

        when(roomRepository.findById(ticketInfo.getRoomId())).thenReturn(Optional.empty());

        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        ticket.setStatus(Constants.StatusTicket.PENDING);
        ticket.setPurpose(Constants.Purpose.MEETING);
        ticket.setStartTime(LocalDateTime.now());
        ticket.setEndTime(LocalDateTime.now().plusHours(1));
        ticket.setUsername("mocked_username");

        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);

        HttpClientErrorException errorException = assertThrows(HttpClientErrorException.class, () -> ticketService.create(ticketInfo));
        assertEquals("400 Room is null", errorException.getMessage());
    }

    @Test
    @DisplayName("Given Pending Ticket, When Creating, Then Set Status to PENDING")
    public void givenTicketWithRoomHaveTime_WhenCreating_ThenThrowException() {
        List<String> oldCustomer = new ArrayList<>();
        oldCustomer.add("06eb43a7-6ea8-4744-8231-760559fe2c09");

        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().minusMinutes(30));
        ticketInfo.setEndTime(LocalDateTime.now());
        ticketInfo.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        ticketInfo.setNewCustomers(null);
        ticketInfo.setOldCustomers(oldCustomer);
        ticketInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");

        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId()))).thenReturn(java.util.Optional.of(site));

        Room room = new Room();
        room.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        room.setName("abc");
        when(roomRepository.findById(ticketInfo.getRoomId())).thenReturn(Optional.of(room));
        when(ticketRepository.countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqualAndStatusNotLike(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime(), Constants.StatusTicket.CANCEL)).thenReturn(1);


        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        ticket.setStatus(Constants.StatusTicket.PENDING);
        ticket.setPurpose(Constants.Purpose.MEETING);
        ticket.setStartTime(LocalDateTime.now());
        ticket.setEndTime(LocalDateTime.now().plusHours(1));
        ticket.setUsername("mocked_username");

        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);

        HttpClientErrorException errorException = assertThrows(HttpClientErrorException.class, () -> ticketService.create(ticketInfo));
        assertEquals("400 Room have meeting in this time", errorException.getMessage());
    }


    @Test
    public void testCreatePendingTicketWithInvalidSiteNull() {
        List<String> oldCustomer = new ArrayList<>();
        oldCustomer.add("06eb43a7-6ea8-4744-8231-760559fe2c09");
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now()); // Invalid start time
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setNewCustomers(null);
        ticketInfo.setOldCustomers(oldCustomer);

        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        ticket.setStatus(Constants.StatusTicket.PENDING);
        ticket.setPurpose(Constants.Purpose.MEETING);
        ticket.setStartTime(null);
        ticket.setEndTime(LocalDateTime.now().plusHours(1));
        ticket.setUsername("mocked_username");
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpClientErrorException errorException = assertThrows(HttpClientErrorException.class, () -> ticketService.create(ticketInfo));
        assertEquals("400 site is null", errorException.getMessage());
    }

    @Test
    public void testCreatePendingTicketWithStartTimeMustLessThanEndTime() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now()); // Invalid start time
        ticketInfo.setEndTime(LocalDateTime.now());
        ticketInfo.setSiteId("valid_site_id");
        ticketInfo.setPurpose(Constants.Purpose.MEETING);

        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        ticket.setStatus(Constants.StatusTicket.PENDING);
        ticket.setPurpose(Constants.Purpose.MEETING);
        ticket.setStartTime(null);
        ticket.setEndTime(LocalDateTime.now().plusHours(1));
        ticket.setUsername("mocked_username");
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpClientErrorException errorException = assertThrows(HttpClientErrorException.class, () -> ticketService.create(ticketInfo));
        assertEquals("400 Time meeting must greater than 15 minutes", errorException.getMessage());
    }

    @Test
    public void testCreatePendingTicketWithInvalidSiteId() {
        List<String> oldCustomer = new ArrayList<>();
        oldCustomer.add("06eb43a7-6ea8-4744-8231-760559fe2c09");
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now());
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId(""); // Invalid site id
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setNewCustomers(null);
        ticketInfo.setOldCustomers(oldCustomer);

        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        ticket.setStatus(Constants.StatusTicket.PENDING);
        ticket.setPurpose(Constants.Purpose.MEETING);
        ticket.setStartTime(null);
        ticket.setEndTime(LocalDateTime.now().plusHours(1));
        ticket.setUsername("mocked_username");
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(false);

        assertThrows(HttpClientErrorException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket Info with Invalid Purpose, When Creating, Then Throw Exception")
    public void givenTicketInfoWithInvalidPurpose_WhenCreating_ThenThrowException() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now());
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("valid_site_id");
        ticketInfo.setPurpose(null); // Invalid purpose

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);
        when(templateRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(new Template()));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(new Ticket());

        assertThrows(HttpClientErrorException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket Info with Other Purpose and No Purpose Note, When Creating, Then Throw Exception")
    public void givenTicketInfoWithOtherPurposeAndNoPurposeNote_WhenCreating_ThenThrowException() {
        List<String> oldCustomer = new ArrayList<>();
        oldCustomer.add("06eb43a7-6ea8-4744-8231-760559fe2c09");
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now());
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c06");
        ticketInfo.setPurpose(OTHERS); // Other purpose, but no purpose note
        ticketInfo.setNewCustomers(null);
        ticketInfo.setOldCustomers(oldCustomer);


        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);
        when(templateRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(new Template()));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(new Ticket());

        assertThrows(HttpClientErrorException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    public void testUpdateBookMark() {
        ITicketController.TicketBookmark ticketBookmark = new ITicketController.TicketBookmark();
        ticketBookmark.setTicketId("06eb43a7-6ea8-4744-8231-760559fe2c06");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Ticket mockTicket = new Ticket();
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c06");
        mockTicket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        when(ticketRepository.findById(UUID.fromString(ticketBookmark.getTicketId()))).thenReturn(Optional.of(mockTicket));
        when(ticketRepository.existsByIdAndUsername(UUID.fromString(ticketBookmark.getTicketId()), "mocked_username")).thenReturn(true);
        when(ticketRepository.save(Mockito.any(Ticket.class))).thenReturn(mockTicket);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        when(ticketRepository.findById(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"))).thenReturn(Optional.of(mockTicket));
        when(siteRepository.findById(UUID.fromString(mockTicket.getSiteId()))).thenReturn(Optional.of(site));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c06", auditLog.getSiteId());
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getOrganizationId());
            assertEquals(mockTicket.getId().toString(), auditLog.getPrimaryKey());
            assertEquals("Ticket", auditLog.getTableName());
            assertEquals(Constants.AuditType.UPDATE, auditLog.getAuditType());
            assertEquals(mockTicket.toString(), auditLog.getOldValue());
            assertEquals(mockTicket.toString(), auditLog.getNewValue());
            return auditLog;
        });


        boolean result = ticketService.updateBookMark(ticketBookmark);

        assertTrue(result);
        assertTrue(mockTicket.isBookmark());
    }

    @Test
    public void testUpdateBookMarkWithEmptyPayload() {
        ITicketController.TicketBookmark ticketBookmark = null; // Empty payload

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateBookMark(ticketBookmark));
    }

    @Test
    public void testUpdateBookMarkWithInvalidTicketId() {
        ITicketController.TicketBookmark ticketBookmark = new ITicketController.TicketBookmark();
        ticketBookmark.setTicketId("06eb43a7-6ea8-4744-8231-760559fe2c06");

        when(ticketRepository.findById(UUID.fromString(ticketBookmark.getTicketId()))).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateBookMark(ticketBookmark));
    }

    @Test
    public void testUpdateBookMarkWithUnauthorizedUser() {
        ITicketController.TicketBookmark ticketBookmark = new ITicketController.TicketBookmark();
        ticketBookmark.setTicketId("06eb43a7-6ea8-4744-8231-760559fe2c06");

        Ticket mockTicket = new Ticket();
        when(ticketRepository.findById(UUID.fromString(ticketBookmark.getTicketId()))).thenReturn(Optional.of(mockTicket));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.existsByIdAndUsername(UUID.fromString(ticketBookmark.getTicketId()), "another_user")).thenReturn(false);

        boolean result = ticketService.updateBookMark(ticketBookmark);

        assertFalse(result);
        assertFalse(mockTicket.isBookmark());
    }

    @Test
    public void testUpdateBookMarkWithNullTicket() {
        ITicketController.TicketBookmark ticketBookmark = new ITicketController.TicketBookmark();
        ticketBookmark.setTicketId("06eb43a7-6ea8-4744-8231-760559fe2c06");

        when(ticketRepository.findById(UUID.fromString(ticketBookmark.getTicketId()))).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateBookMark(ticketBookmark));
    }

    @Test
    @DisplayName("Given Existing Ticket, When Deleting, Then Delete Ticket")
    public void givenExistingTicket_WhenDeleting_ThenDeleteTicket() {
        String ticketId = "06eb43a7-6ea8-4744-8231-760559fe2c08";
        Ticket mockTicket = new Ticket();
        mockTicket.setSiteId(ticketId);
        mockTicket.setId(UUID.fromString(ticketId));

        when(ticketRepository.findById(UUID.fromString(ticketId))).thenReturn(Optional.of(mockTicket));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.existsByIdAndUsername(UUID.fromString(ticketId), "mocked_username")).thenReturn(true);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        when(ticketRepository.findById(UUID.fromString(ticketId))).thenReturn(Optional.of(mockTicket));
        when(siteRepository.findById(UUID.fromString(mockTicket.getSiteId()))).thenReturn(Optional.of(site));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getSiteId());
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getOrganizationId());
            assertEquals(mockTicket.getId().toString(), auditLog.getPrimaryKey());
            assertEquals("Ticket", auditLog.getTableName());
            assertEquals(Constants.AuditType.DELETE, auditLog.getAuditType());
            assertEquals(mockTicket.toString(), auditLog.getOldValue());
            assertEquals(null, auditLog.getNewValue());
            return auditLog;
        });

        boolean result = ticketService.deleteTicket(ticketId);

        assertTrue(result);
        verify(ticketRepository, times(1)).delete(mockTicket);
    }

    @Test
    @DisplayName("Given Ticket with Invalid ID, When Deleting, Then Throw Exception")
    public void givenTicketWithInvalidId_WhenDeleting_ThenThrowException() {
        String ticketId = "06eb43a7-6ea8-4744-8231-760559fe2c06";

        when(ticketRepository.findById(UUID.fromString(ticketId))).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> ticketService.deleteTicket(ticketId));
    }

    @Test
    @DisplayName("Given Unauthorized User, When Deleting, Then Throw Exception")
    public void givenUnauthorizedUser_WhenDeleting_ThenThrowException() {
        String ticketId = "06eb43a7-6ea8-4744-8231-760559fe2c06";
        Ticket mockTicket = new Ticket();

        when(ticketRepository.findById(UUID.fromString(ticketId))).thenReturn(Optional.of(mockTicket));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("another_user");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.existsByIdAndUsername(UUID.fromString(ticketId), "another_user")).thenReturn(false);

        boolean result = ticketService.deleteTicket(ticketId);

        assertFalse(result);
        verify(ticketRepository, Mockito.never()).delete(mockTicket);
    }

    @Test
    @DisplayName("Given Null Ticket, When Deleting, Then Throw Exception")
    public void givenNullTicket_WhenDeleting_ThenThrowException() {
        String ticketId = "06eb43a7-6ea8-4744-8231-760559fe2c06";

        when(ticketRepository.findById(UUID.fromString(ticketId))).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> ticketService.deleteTicket(ticketId));
    }

    @Test
    @DisplayName("Given Ticket to Cancel, When Cancelling, Then Cancel Ticket")
    public void givenTicketToCancel_WhenCancelling_ThenCancelTicket() {
        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        Ticket mockTicket = new Ticket();
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3)); // Start time is after 2 hours
        mockTicket.setEndTime(LocalDateTime.now().plusHours(6)); // Start time is after 2 hours
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        Reason reason = new Reason();
        reason.setName("Reason");
        when(reasonRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(reason));

        Customer customer = new Customer();
        customer.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);
        when(ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), "mocked_username")).thenReturn(true);

        Template template = new Template();
        template.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        when(settingUtils.getOrDefault(eq(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL))).thenReturn(template.getId().toString());
        when(templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL)))).thenReturn(Optional.of(template));

        List<CustomerTicketMap> customerTicketMaps = new ArrayList<>();
        CustomerTicketMap customerTicketMap1 = new CustomerTicketMap();
        customerTicketMap1.setCustomerTicketMapPk(new CustomerTicketMapPk(mockTicket.getId(), customer.getId()));
        customerTicketMaps.add(customerTicketMap1);
        customerTicketMap1.setCustomerEntity(customer);
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(mockTicket.getId())).thenReturn(customerTicketMaps);


        doNothing().when(emailUtils).sendMailWithQRCode(anyString(), anyString(), anyString(), any(), anyString());

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        when(ticketRepository.findById(UUID.fromString(String.valueOf(mockTicket.getId())))).thenReturn(Optional.of(mockTicket));
        when(siteRepository.findById(UUID.fromString(mockTicket.getSiteId()))).thenReturn(Optional.of(site));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getSiteId());
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getOrganizationId());
            assertEquals(mockTicket.getId().toString(), auditLog.getPrimaryKey());
            assertEquals("Ticket", auditLog.getTableName());
            assertEquals(Constants.AuditType.UPDATE, auditLog.getAuditType());
            assertEquals(mockTicket.toString(), auditLog.getOldValue());
            assertEquals(mockTicket.toString(), auditLog.getNewValue());
            return auditLog;
        });

        boolean result = ticketService.cancelTicket(cancelTicket);

        assertTrue(result);
        assertEquals(Constants.StatusTicket.CANCEL, mockTicket.getStatus());
        verify(ticketRepository, times(1)).save(mockTicket);
    }

    @Test
    @DisplayName("Given Ticket to Cancel, When Cancelling, Then Cancel Ticket")
    public void givenTicketToCancelWithNoneTemplate_WhenCancelling_ThenThrowException() {
        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        Ticket mockTicket = new Ticket();
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3)); // Start time is after 2 hours
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        Customer customer = new Customer();
        customer.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        Template template = new Template();
        template.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);
        when(ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), "mocked_username")).thenReturn(true);
        when(settingUtils.getOrDefault(eq(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL))).thenReturn(template.getId().toString());

        List<CustomerTicketMap> customerTicketMaps = new ArrayList<>();
        CustomerTicketMap customerTicketMap1 = new CustomerTicketMap();
        customerTicketMap1.setCustomerTicketMapPk(new CustomerTicketMapPk(mockTicket.getId(), customer.getId()));
        customerTicketMap1.setCustomerEntity(customer);
        customerTicketMaps.add(customerTicketMap1);

        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(mockTicket.getId())).thenReturn(customerTicketMaps);


        doNothing().when(emailUtils).sendMailWithQRCode(anyString(), anyString(), anyString(), any(), anyString());

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        when(ticketRepository.findById(UUID.fromString(String.valueOf(mockTicket.getId())))).thenReturn(Optional.of(mockTicket));
        when(siteRepository.findById(UUID.fromString(mockTicket.getSiteId()))).thenReturn(Optional.of(site));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getSiteId());
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getOrganizationId());
            assertEquals(mockTicket.getId().toString(), auditLog.getPrimaryKey());
            assertEquals("Ticket", auditLog.getTableName());
            assertEquals(Constants.AuditType.UPDATE, auditLog.getAuditType());
            assertEquals(mockTicket.toString(), auditLog.getOldValue());
            assertEquals(mockTicket.toString(), auditLog.getNewValue());
            return auditLog;
        });

        assertThrows(HttpClientErrorException.class, () -> ticketService.cancelTicket(cancelTicket));
    }

    @Test
    @DisplayName("Given Ticket with Invalid ID to Cancel, When Cancelling, Then Throw Exception")
    public void givenTicketWithInvalidIdToCancel_WhenCancelling_ThenThrowException() {
        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> ticketService.cancelTicket(cancelTicket));
    }

    @Test
    @DisplayName("Given Ticket with StartTime Before 2 Hours to Cancel, When Cancelling, Then Throw Exception")
    public void givenTicketWithStartTimeBefore2HoursToCancel_WhenCancelling_ThenThrowException() {

        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        Ticket mockTicket = new Ticket();
        mockTicket.setStartTime(LocalDateTime.now().plusHours(1)); // Start time is before 2 hours
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        assertThrows(HttpClientErrorException.class, () -> ticketService.cancelTicket(cancelTicket));
    }

    @Test
    @DisplayName("Given Unauthorized User to Cancel, When Cancelling, Then Throw Exception")
    public void givenUnauthorizedUserToCancel_WhenCancelling_ThenThrowException() {

        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        Ticket mockTicket = new Ticket();
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3)); // Start time is after 2 hours
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Template template = new Template();
        template.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        when(settingUtils.getOrDefault(eq(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL))).thenReturn(template.getId().toString());

        doNothing().when(emailUtils).sendMailWithQRCode(anyString(), anyString(), anyString(), any(), anyString());
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(mockTicket.getId())).thenReturn(new ArrayList<>());
        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);
        when(ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), "another_user")).thenReturn(false);

        boolean result = ticketService.cancelTicket(cancelTicket);

        assertFalse(result);
        verify(ticketRepository, Mockito.never()).save(mockTicket);
    }

    @Test
    @DisplayName("Given Valid TicketInfo, When Updating, Then Update Ticket")
    public void givenValidTicketInfo_WhenUpdating_ThenUpdateTicket() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        ticketInfo.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().minusHours(1));

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3));
        mockTicket.setEndTime(LocalDateTime.now().plusHours(4));
        mockTicket.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Room room = new Room();
        room.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        room.setName("abc");
        when(roomRepository.findById(mockTicket.getRoomId())).thenReturn(Optional.of(room));

        Commune commune = new Commune();
        commune.setName("Kim liên");
        District district = new District();
        district.setName("Nam Đàn");
        Province province = new Province();
        province.setName("Nghệ An");
        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.randomUUID());
        site.setProvince(province);
        site.setDistrict(district);
        site.setCommune(commune);
        when(siteRepository.findById(UUID.fromString(mockTicket.getSiteId()))).thenReturn(java.util.Optional.of(site));

        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        Ticket result = ticketService.updateTicket(ticketInfo);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Given Ticket Not for Current User, When Updating, Then Throw Exception")
    public void givenTicketNotForCurrentUser_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("other_username");
        mockTicket.setStartTime(LocalDateTime.now().plusHours(1));
        mockTicket.setEndTime(LocalDateTime.now().plusHours(3));

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));

        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket with Invalid ID, When Updating, Then Throw Exception")
    public void givenTicketWithInvalidId_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        ticketInfo.setId(null);

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Updated Ticket with Purpose Note When Purpose Is Not OTHERS, When Updating, Then Throw Exception")
    public void givenUpdatedTicketWithPurposeNoteWhenPurposeIsNotOthers_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        ticketInfo.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));


        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        mockTicket.setUsername("mocked_username");
        mockTicket.setPurpose(MEETING); // Not OTHERS
        mockTicket.setPurposeNote("TEST");
        mockTicket.setStartTime(LocalDateTime.now());


        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));

        Room room = new Room();
        room.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        room.setName("abc");
        when(roomRepository.findById(mockTicket.getRoomId())).thenReturn(Optional.of(room));

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(mockTicket.getSiteId()))).thenReturn(java.util.Optional.of(site));

        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Updated Ticket with Purpose Note When Purpose Is Not OTHERS, When Updating, Then Throw Exception")
    public void givenUpdatedTicketWithPurposeNoteNUllWhenPurposeIsOthers_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        ticketInfo.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));


        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        mockTicket.setUsername("mocked_username");
        mockTicket.setPurpose(OTHERS);
        mockTicket.setPurposeNote(null);
        mockTicket.setStartTime(LocalDateTime.now());


        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));

        Room room = new Room();
        room.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        room.setName("abc");
        when(roomRepository.findById(mockTicket.getRoomId())).thenReturn(Optional.of(room));

        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(mockTicket.getSiteId()))).thenReturn(java.util.Optional.of(site));

        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Room Booked, When Updating with the Same Room, When Updating, Then Throw Exception")
    public void givenRoomBooked_WhenUpdatingWithTheRoomNull_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId);

        LocalDateTime newStartTime = LocalDateTime.now();
        LocalDateTime newEndTime = newStartTime.plusHours(2);
        ticketInfo.setStartTime(newStartTime);
        ticketInfo.setEndTime(newEndTime);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setRoomId(roomId);
        mockTicket.setStartTime(LocalDateTime.now().plusHours(1));
        mockTicket.setEndTime(LocalDateTime.now().plusHours(3));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        when(roomRepository.findById(mockTicket.getRoomId())).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Room Booked, When Updating with the Same Room, When Updating, Then Throw Exception")
    public void givenRoomBooked_WhenUpdatingWithTheRoomIsNotInSite_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId);

        LocalDateTime newStartTime = LocalDateTime.now();
        LocalDateTime newEndTime = newStartTime.plusHours(2);
        ticketInfo.setStartTime(newStartTime);
        ticketInfo.setEndTime(newEndTime);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setRoomId(roomId);
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setStartTime(LocalDateTime.now().plusHours(1));
        mockTicket.setEndTime(LocalDateTime.now().plusHours(3));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        Room room = new Room();
        room.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c05"));
        room.setName("abc");
        when(roomRepository.findById(mockTicket.getRoomId())).thenReturn(Optional.of(room));

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Room Booked, When Updating with the Same Room, When Updating, Then Throw Exception")
    public void givenRoomBooked_WhenUpdatingWithTheRoomHaveTicket_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId);

        LocalDateTime newStartTime = LocalDateTime.now();
        LocalDateTime newEndTime = newStartTime.plusHours(2);
        ticketInfo.setStartTime(newStartTime);
        ticketInfo.setEndTime(newEndTime);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setRoomId(roomId);
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setStartTime(LocalDateTime.now().plusHours(1));
        mockTicket.setEndTime(LocalDateTime.now().plusHours(3));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        Room room = new Room();
        room.setSiteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        room.setName("abc");
        when(roomRepository.findById(mockTicket.getRoomId())).thenReturn(Optional.of(room));
        when(ticketRepository.countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqualAndStatusNotLike(roomId, ticketInfo.getStartTime(), ticketInfo.getEndTime(), Constants.StatusTicket.CANCEL)).thenReturn(1);

        assertThrows(HttpClientErrorException.class, () -> ticketService.updateTicket(ticketInfo));
    }
    @Test
    @DisplayName("Given Filter Parameters, When Filtering Tickets, Then Return Page of Tickets")
    public void givenFilterParameters_WhenFilteringTickets_ThenReturnPageOfTickets() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdOn")));
        List<String> names = new ArrayList<>();
        UUID roomId = UUID.randomUUID();
        Constants.StatusTicket status = Constants.StatusTicket.PENDING;
        Constants.Purpose purpose = Constants.Purpose.MEETING;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(30);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        LocalDateTime startTimeStart = LocalDateTime.now().minusDays(15);
        LocalDateTime startTimeEnd = LocalDateTime.now().plusDays(15);
        LocalDateTime endTimeStart = LocalDateTime.now().minusDays(10);
        LocalDateTime endTimeEnd = LocalDateTime.now().plusDays(20);
        String createdBy = "user1";
        String lastUpdatedBy = "user2";
        Boolean bookmark = true;
        String keyword = "meeting";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<String> usernames = new ArrayList<>();
        usernames.add(SecurityUtils.loginUsername());

        Page<Ticket> expectedPage = new PageImpl<>(List.of(new Ticket(), new Ticket()));

        when(ticketRepository.filter(pageable, names, null, usernames, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, bookmark, keyword))
            .thenReturn(expectedPage);

        Page<Ticket> filteredTickets = ticketService.filter(pageable, names, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, bookmark, keyword);

        assertNotNull(filteredTickets);
        assertEquals(2, filteredTickets.getTotalElements());
    }

    @Test
    @DisplayName("Given Filter Parameters for All Sites, When Filtering Tickets, Then Return Page of Tickets")
    public void givenFilterParametersForAllSites_WhenFilteringTickets_ThenReturnPageOfTickets() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdOn")));
        List<String> names = new ArrayList<>();
        UUID roomId = UUID.randomUUID();
        Constants.StatusTicket status = Constants.StatusTicket.PENDING;
        Constants.Purpose purpose = Constants.Purpose.MEETING;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(30);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        LocalDateTime startTimeStart = LocalDateTime.now().minusDays(15);
        LocalDateTime startTimeEnd = LocalDateTime.now().plusDays(15);
        LocalDateTime endTimeStart = LocalDateTime.now().minusDays(10);
        LocalDateTime endTimeEnd = LocalDateTime.now().plusDays(20);
        String createdBy = "user1";
        String username = "user1";
        String lastUpdatedBy = "user2";
        String keyword = "meeting";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<String> siteList = new ArrayList<>();
        List<String> usernames = new ArrayList<>();
        siteList.add("06eb43a7-6ea8-4744-8231-760559fe2c08");
        usernames.add(SecurityUtils.loginUsername());

        when(SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);

        Page<Ticket> expectedPage = new PageImpl<>(List.of(new Ticket(), new Ticket()));

        when(ticketRepository.filter(pageable, names, siteList, usernames, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, null, keyword))
            .thenReturn(expectedPage);

        Page<Ticket> filteredTickets = ticketService.filterAllBySite(pageable, names, siteList, usernames, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, keyword);

        assertNotNull(expectedPage);
        assertEquals(2, expectedPage.getTotalElements());
    }

    @Test
    @DisplayName("Given Invalid Site ID, When Filtering Tickets, Then Throw HttpClientErrorException")
    public void givenInvalidSiteId_WhenFilteringTickets_ThenThrowHttpClientErrorException() {
        // Mock input parameters
        UUID ticketId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String checkInCode = "ABC3AD";
        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        Ticket ticketEntity = new Ticket();
        ticketEntity.setSiteId("site2"); // A different site ID

        customerTicketMap.setTicketEntity(ticketEntity);
        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInCode))
            .thenReturn(customerTicketMap);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Verify that a HttpClientErrorException is thrown
        assertThrows(HttpClientErrorException.class, () -> {
            ticketService.findByQRCode(checkInCode);
        });
    }

    @Test
    @DisplayName("Given CheckInPayload Request, When Updating Status, Then Update Customer Ticket Map")
    public void givenUpdateStatusTicketOfCustomerRequest_WhenUpdatingStatus_ThenUpdateCustomerTicketMap() {
        // Mock input parameters
        ITicketController.CheckInPayload checkInPayload = new ITicketController.CheckInPayload();
        checkInPayload.setTicketId(UUID.randomUUID());
        checkInPayload.setCustomerId(UUID.randomUUID());
        checkInPayload.setStatus(Constants.StatusTicket.CHECK_IN);
        checkInPayload.setReasonId(UUID.randomUUID());
        checkInPayload.setReasonNote("ReasonNote");
        checkInPayload.setCheckInCode("checkInCode");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode()))
            .thenReturn(customerTicketMap);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Ticket ticket = new Ticket();
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(Optional.of(ticket));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));

        when(ticketRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(ticket));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            // Kiểm tra giá trị của auditLog nếu cần
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getSiteId());
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getOrganizationId());
            assertEquals(customerTicketMap.getId().toString(), auditLog.getPrimaryKey());
            assertEquals("CustomerTicketMap", auditLog.getTableName());
            assertEquals(Constants.AuditType.CREATE, auditLog.getAuditType());
            assertEquals(null, auditLog.getOldValue());
            assertEquals(customerTicketMap.toString(), auditLog.getNewValue());
            return auditLog;
        });


        // Call the method under test
        ticketService.checkInCustomer(checkInPayload);

        // Verify that the customerTicketMap has been updated with the new status, reasonId, and reasonNote
        assertEquals(checkInPayload.getStatus(), customerTicketMap.getStatus());
        assertEquals(checkInPayload.getReasonId(), customerTicketMap.getReasonId());
        assertEquals(checkInPayload.getReasonNote(), customerTicketMap.getReasonNote());

        // Verify that the customerTicketMapRepository.save() has been called
        verify(customerTicketMapRepository).save(customerTicketMap);

    }

    private static final String CUSTOMER_TICKET_TABLE_NAME = "CustomerTicketMap";

    @Test
    void testCheckOutCustomer() {
        // Mock data
        UUID customerTicketMapId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        String siteId = UUID.randomUUID().toString();
        LocalDateTime currentTime = LocalDateTime.now();
        UUID reasonId = UUID.randomUUID();

        ITicketController.CheckInPayload checkOutPayload = new ITicketController.CheckInPayload();
        checkOutPayload.setCheckInCode("ABCDE");
        checkOutPayload.setStatus(Constants.StatusTicket.CHECK_OUT);
        checkOutPayload.setReasonId(reasonId);
        checkOutPayload.setReasonNote("Customer requested checkout");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));
        customerTicketMap.setStatus(Constants.StatusTicket.CHECK_IN);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(customerTicketMapId, ticketId));
        customerTicketMap.setCheckInTime(currentTime.minusHours(1));  // Set a past check-in time

        Ticket ticket = new Ticket();
        ticket.setId(customerTicketMap.getCustomerTicketMapPk().getTicketId());
        ticket.setSiteId(siteId);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(java.util.Optional.of(ticket));

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        // Mock repository behavior
        when(customerTicketMapRepository.findByCheckInCodeIgnoreCase("ABCDE")).thenReturn(customerTicketMap);
        when(siteRepository.findById(UUID.fromString(siteId))).thenReturn(java.util.Optional.of(new Site()));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));

        // Call the method under test
        ticketService.checkInCustomer(checkOutPayload);

        // Verify that the status is updated to CHECK_OUT
        Mockito.verify(customerTicketMapRepository).save(customerTicketMap);

        // Verify that the audit log is created
        Mockito.verify(auditLogRepository).save(any(AuditLog.class));

        // You can add more assertions if needed
    }


    @Test
    void testGenerateCheckInCode() {
        // Generate check-in codes multiple times and ensure they meet the expected criteria
        for (int i = 0; i < 100; i++) {
            String checkInCode = ticketService.generateCheckInCode();

            // Check the length
            assertEquals(6, checkInCode.length(), "Generated check-in code should have a length of 6");

            // Check if all characters are alphanumeric
            assertTrue(checkInCode.matches("[A-Z0-9]+"), "Generated check-in code should be alphanumeric");

            // You can add more specific criteria based on your needs
        }
    }

    @Test
    void testGenerateMeetingCode() {
        // Test with different purposes and usernames
        for (Constants.Purpose purpose : Constants.Purpose.values()) {
            String username = "username";
            String meetingCode = ticketService.generateMeetingCode(purpose, username);

            // Check the length
            assertEquals(25, meetingCode.length(), "Generated meeting code should have a length of 16");

            // Check if the code starts with the correct purpose letter
            assertEquals(meetingCode.substring(0, 1), getPurposeCode(purpose), "Generated meeting code should start with the correct purpose code");

            // Check if the date part is valid (format: ddMMyy)
            assertTrue(meetingCode.substring(1, 7).matches("\\d{6}"), "Generated meeting code should have a valid date part");

            // Check if the remaining part is a 4-digit number
            assertFalse(meetingCode.substring(7).matches("\\d{4}"), "Generated meeting code should end with a 4-digit number");

        }
    }

    private String getPurposeCode(Constants.Purpose purpose) {
        switch (purpose) {
            case CONFERENCES -> {
                return "C";
            }
            case INTERVIEW -> {
                return "I";
            }
            case MEETING -> {
                return "M";
            }
            case OTHERS -> {
                return "O";
            }
            case WORKING -> {
                return "W";
            }
            default -> {
                return "T";
            }
        }
    }

    @Test
    void testCreateCustomerTicket() {
        // Mock data
        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        UUID customerId = UUID.randomUUID();
        String checkInCode = "ABC123";

        // Mock behavior of the repository save method
        when(customerTicketMapRepository.save(any(CustomerTicketMap.class))).thenReturn(new CustomerTicketMap());

        // Call the method under test
        ticketService.createCustomerTicket(ticket, customerId, checkInCode);

        // Verify that the repository save method was called with the correct arguments
        verify(customerTicketMapRepository).save(any(CustomerTicketMap.class));

        // You can add more assertions if needed
    }

    @Test
    void testCreateCustomerTicketWithDifferentCheckInCode() {
        // Mock data
        Ticket ticket = new Ticket();
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        UUID customerId = UUID.randomUUID();
        String checkInCode = "XYZ789";

        // Mock behavior of the repository save method
        when(customerTicketMapRepository.save(any(CustomerTicketMap.class))).thenReturn(new CustomerTicketMap());

        // Call the method under test
        ticketService.createCustomerTicket(ticket, customerId, checkInCode);

        // Verify that the repository save method was called with the correct arguments
        verify(customerTicketMapRepository).save(any(CustomerTicketMap.class));

        // You can add more assertions if needed
    }


    @Test
    void testFilterTicketAndCustomer() {
        // Mock data
        Pageable pageable = Pageable.unpaged();
        UUID roomId = UUID.randomUUID();
        Constants.StatusTicket status = Constants.StatusTicket.PENDING;
        Constants.Purpose purpose = Constants.Purpose.CONFERENCES;
        String keyword = "search";

        CustomerTicketMap ticketMap1 = new CustomerTicketMap();
        CustomerTicketMap ticketMap2 = new CustomerTicketMap();
        List<CustomerTicketMap> ticketMapList = Arrays.asList(ticketMap1, ticketMap2);

        when(customerTicketMapRepository.filter(any(Pageable.class), null, any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class), any(UUID.class), any(Constants.StatusTicket.class), any(Constants.Purpose.class), any(String.class)))
            .thenReturn(new PageImpl<>(ticketMapList));

        ITicketController.TicketByQRCodeResponseDTO responseDTO1 = new ITicketController.TicketByQRCodeResponseDTO();
        ITicketController.TicketByQRCodeResponseDTO responseDTO2 = new ITicketController.TicketByQRCodeResponseDTO();
        List<ITicketController.TicketByQRCodeResponseDTO> responseDTOList = Arrays.asList(responseDTO1, responseDTO2);

        when(mapper.map(ticketMapList, new TypeToken<List<ITicketController.TicketByQRCodeResponseDTO>>() {
        }.getType()))
            .thenReturn(responseDTOList);

        // Call the method under test
        Page<ITicketController.TicketByQRCodeResponseDTO> result = ticketService.filterTicketAndCustomer(
            pageable, null, null, roomId, status, purpose, null, null, null, null, null, null, null, null, null, keyword
        );

        // Verify that the repository filter method was called with the correct arguments
        Mockito.verify(customerTicketMapRepository).filter(any(Pageable.class), null, any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class), any(LocalDateTime.class), any(UUID.class), any(Constants.StatusTicket.class), any(Constants.Purpose.class), any(String.class));

        // Verify that the result has the expected content
        assertEquals(responseDTOList, result.getContent());

        // You can add more assertions if needed
    }

    @Test
    void testFindByTicketForAdminWithValidTicketAndOrgIdAndSiteId() {
        // Mock data
        UUID ticketId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSiteId(siteId.toString());

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));
        // Call the method under test
        assertThrows(HttpClientErrorException.class, () -> ticketService.findByTicketForAdmin(ticketId, siteId.toString()));


        // You can add more assertions if needed
    }

    @Test
    void testFindByTicketForAdminWithValidTicketAndOrgIdAndNoSiteId() {
        // Mock data
        UUID ticketId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSiteId(siteId.toString());

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(HttpClientErrorException.class, () -> ticketService.findByTicketForAdmin(ticketId, null));

        // Verify that the repository findById method was called with the correct argument
        Mockito.verify(ticketRepository).findById(ticketId);

        // You can add more assertions if needed
    }

    @Test
    void testFindByTicketForAdminWithInvalidTicket() {
        // Mock data
        UUID ticketId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(HttpClientErrorException.class, () -> ticketService.findByTicketForAdmin(ticketId, null));

        // Verify that the repository findById method was called with the correct argument
        Mockito.verify(ticketRepository).findById(ticketId);

        // You can add more assertions if needed
    }

    @Test
    void testFindByTicketForAdminWithInvalidSiteId() {
        // Mock data
        UUID ticketId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID invalidSiteId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSiteId(UUID.randomUUID().toString());  // Set a different site ID

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(siteRepository.findById(invalidSiteId)).thenReturn(Optional.empty());

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(HttpClientErrorException.class, () -> ticketService.findByTicketForAdmin(ticketId, invalidSiteId.toString()));

        // Verify that the repository findById method was called with the correct argument
        Mockito.verify(ticketRepository).findById(ticketId);

        // Verify that the repository findById method was called with the correct argument
        Mockito.verify(siteRepository).findById(invalidSiteId);

        // You can add more assertions if needed
    }

    @Test
    void testFindByTicketForAdminWithInvalidSiteIdForSiteAdmin() {
        // Mock data
        UUID ticketId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSiteId(UUID.randomUUID().toString());  // Set a different site ID

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(HttpClientErrorException.class, () -> ticketService.findByTicketForAdmin(ticketId, null));
    }

    @Test
    void testSendEmail() throws IOException, WriterException {
        // Mock data
        Customer customer = new Customer();
        customer.setVisitorName("John Doe");
        customer.setEmail("john.doe@example.com");

        Ticket ticket = new Ticket();
        ticket.setName("Meeting ABC");
        ticket.setStartTime(LocalDateTime.now());
        ticket.setEndTime(LocalDateTime.now().plusHours(1));
        ticket.setUsername("john_doe");
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c07");

        Room room = new Room();
        room.setName("Room 101");

        String checkInCode = "ABCDE";

        UUID siteId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Site site = new Site();
        site.setId(siteId);
        site.setAddress("123 Main Street");

        Template template = new Template();
        template.setId(templateId);
        template.setSubject("Confirmation Email");
        template.setBody("Dear {{customerName}}, your meeting {{meetingName}} is scheduled on {{dateTime}} from {{startTime}} to {{endTime}} at {{address}}, Room {{roomName}}. Please check in using code {{checkInCode}}.");

        User user = new User();
        user.setId("userId");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("123456789");
        user.setEmail("john.doe@example.com");

        // Mock dependencies
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(userRepository.findFirstByUsername("john_doe")).thenReturn(user);

        // Mock settingUtils behavior
        when(settingUtils.getOrDefault(any(String.class))).thenReturn(templateId.toString());
        when(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL)).thenReturn(templateId.toString());

        // Mock emailUtils behavior
        when(emailUtils.replaceEmailParameters(any(String.class), any(Map.class))).thenAnswer(invocation -> {
            Map<String, String> parameterMap = invocation.getArgument(1);
            return "Dear " + parameterMap.get("customerName") + ", your meeting " +
                parameterMap.get("meetingName") + " is scheduled on " +
                parameterMap.get("dateTime") + " from " +
                parameterMap.get("startTime") + " to " +
                parameterMap.get("endTime") + " at " +
                parameterMap.get("address") + ", Room " +
                parameterMap.get("roomName") + ". Please check in using code " +
                parameterMap.get("checkInCode") + ".";
        });

        // Call the method under test
        ticketService.sendEmail(customer, ticket, room, checkInCode, false);

        // You can add more assertions if needed
    }

    @Test
    void testSendEmailWithMissingCustomer() {
        // Mock data
        Ticket ticket = new Ticket();
        Room room = new Room();
        String checkInCode = "ABCDE";

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(HttpClientErrorException.class, () -> ticketService.sendEmail(null, ticket, room, checkInCode, false));

        // You can add more assertions if needed
    }

    @Test
    void testSendEmailWithMissingTemplate() {
        // Mock data
        Customer customer = new Customer();
        customer.setVisitorName("John Doe");

        UUID siteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setUsername("john_doe");
        ticket.setSiteId(siteId.toString());
        Room room = new Room();
        String checkInCode = "ABCDE";

        Site site = new Site();
        site.setId(siteId);
        site.setAddress("abc");

        User user = new User();
        user.setId(userId.toString());
        user.setFirstName("John");
        user.setLastName("Doe");

        // Mock dependencies
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(java.util.Optional.of(site));
        when(userRepository.findFirstByUsername("john_doe")).thenReturn(user);
        // Mock settingUtils behavior to return null, simulating a missing template
        Template template = new Template();
        template.setId(templateId);
        template.setSubject("Confirmation Email");
        template.setBody("Dear {{customerName}}, your meeting {{meetingName}} is scheduled on {{dateTime}} from {{startTime}} to {{endTime}} at {{address}}, Room {{roomName}}. Please check in using code {{checkInCode}}.");
        when(settingUtils.getOrDefault(eq(Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL))).thenReturn(template.getId().toString());
        when(templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL)))).thenReturn(Optional.empty());

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(HttpClientErrorException.class, () -> ticketService.sendEmail(customer, ticket, room, checkInCode, false));

        // You can add more assertions if needed
    }

    @Test
    void testFindByTicketForUser() {

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock data
        UUID ticketId = UUID.randomUUID();
        String username = "mocked_username";

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setUsername(username);

        // Mock repository behavior
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        // Call the method under test
        ITicketController.TicketFilterDTO result = ticketService.findByTicketForUser(ticketId);

        // Verify that the repository findById method was called with the correct argument
        Mockito.verify(ticketRepository).findById(ticketId);

        assertEquals(null, result);
    }

    @Test
    void testFindByTicketForUserWhenTicketNotFound() {

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Mock data
        UUID ticketId = UUID.randomUUID();

        // Mock repository behavior when the ticket is not found
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Call the method under test and expect a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> ticketService.findByTicketForUser(ticketId));

        // Verify that the correct exception is thrown with the expected status code and message
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 Can't not found ticket", exception.getMessage());

        // You can add more assertions if needed
    }

    @Test
    void testFindByTicketForUserWhenInvalidUsername() {

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock data
        UUID ticketId = UUID.randomUUID();
        String ticketUsername = "john_doe";

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setUsername(ticketUsername);

        // Mock repository behavior
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        // Call the method under test and expect a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> ticketService.findByTicketForUser(ticketId));

        // Verify that the correct exception is thrown with the expected status code and message
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("403 Can't not view this ticket", exception.getMessage());
    }

    @Test
    void testFilterAllBySite() {

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock data
        List<String> names = List.of("Meeting A", "Meeting B");
//        List<String> sites = List.of("06eb43a7-6ea8-4744-8231-760559fe2c07");
        List<String> usernames = List.of("john_doe", "jane_doe");
        UUID roomId = UUID.randomUUID();
        Constants.StatusTicket status = Constants.StatusTicket.CHECK_IN;
        Constants.Purpose purpose = Constants.Purpose.CONFERENCES;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        LocalDateTime startTimeStart = LocalDateTime.now().minusHours(1);
        LocalDateTime startTimeEnd = LocalDateTime.now().plusHours(1);
        LocalDateTime endTimeStart = LocalDateTime.now().plusHours(2);
        LocalDateTime endTimeEnd = LocalDateTime.now().plusHours(3);
        String createdBy = "admin";
        String lastUpdatedBy = "manager";
        String keyword = "important";

        List<Ticket> mockResult = new ArrayList<>();  // Replace with your expected result

        // Mock repository behavior
        when(ticketRepository.filter(
            any(List.class),  // Use Matchers to capture any list argument
            any(List.class),
            any(List.class),
            any(UUID.class),
            any(Constants.StatusTicket.class),
            any(Constants.Purpose.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(String.class),
            any(String.class),
            any(Boolean.class),
            any(String.class)))
            .thenReturn(mockResult);

        when(siteRepository.findAllById(any(List.class))).thenReturn(new ArrayList<>());  // Mock siteRepository behavior if needed

        // Call the method under test
        List<Ticket> result = ticketService.filterAllBySite(names, null, usernames, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, keyword);

        // Verify that the repository filter method was called with the correct arguments
        Mockito.verify(ticketRepository).filter(
            Mockito.eq(names),
            Mockito.any(List.class),
            Mockito.eq(usernames),
            Mockito.eq(roomId),
            Mockito.eq(status),
            Mockito.eq(purpose),
            Mockito.eq(createdOnStart),
            Mockito.eq(createdOnEnd),
            Mockito.eq(startTimeStart),
            Mockito.eq(startTimeEnd),
            Mockito.eq(endTimeStart),
            Mockito.eq(endTimeEnd),
            Mockito.eq(createdBy),
            Mockito.eq(lastUpdatedBy),
            Mockito.isNull(),
            Mockito.eq(keyword));

        // Verify the result
        assertEquals(mockResult, result);

        // You can add more assertions if needed
    }

    @Test
    void testFilter() {

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock data
        List<String> names = List.of("Meeting A", "Meeting B");
        UUID roomId = UUID.randomUUID();
        Constants.StatusTicket status = Constants.StatusTicket.CHECK_IN;
        Constants.Purpose purpose = Constants.Purpose.CONFERENCES;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        LocalDateTime startTimeStart = LocalDateTime.now().minusHours(1);
        LocalDateTime startTimeEnd = LocalDateTime.now().plusHours(1);
        LocalDateTime endTimeStart = LocalDateTime.now().plusHours(2);
        LocalDateTime endTimeEnd = LocalDateTime.now().plusHours(3);
        String createdBy = "admin";
        String lastUpdatedBy = "manager";
        Boolean bookmark = true;
        String keyword = "important";

        List<Ticket> mockResult = new ArrayList<>();  // Replace with your expected result

        // Mock repository behavior
        when(ticketRepository.filter(
            any(List.class),  // Use Matchers to capture any list argument
            any(List.class),
            any(List.class),
            any(UUID.class),
            any(Constants.StatusTicket.class),
            any(Constants.Purpose.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(String.class),
            any(String.class),
            any(Boolean.class),
            any(String.class)))
            .thenReturn(mockResult);

        // Call the method under test
        List<Ticket> result = ticketService.filter(
            names,
            roomId,
            status,
            purpose,
            createdOnStart,
            createdOnEnd,
            startTimeStart,
            startTimeEnd,
            endTimeStart,
            endTimeEnd,
            createdBy,
            lastUpdatedBy,
            bookmark,
            keyword
        );

        // Verify that the repository filter method was called with the correct arguments
        Mockito.verify(ticketRepository).filter(
            Mockito.eq(names),
            Mockito.isNull(),
            Mockito.any(List.class),
            Mockito.eq(roomId),
            Mockito.eq(status),
            Mockito.eq(purpose),
            Mockito.eq(createdOnStart),
            Mockito.eq(createdOnEnd),
            Mockito.eq(startTimeStart),
            Mockito.eq(startTimeEnd),
            Mockito.eq(endTimeStart),
            Mockito.eq(endTimeEnd),
            Mockito.eq(createdBy),
            Mockito.eq(lastUpdatedBy),
            Mockito.eq(bookmark),
            Mockito.eq(keyword)
        );

        // Verify the result
        assertEquals(mockResult, result);
    }

    @Test
    void testCheckNewCustomersWithInvalidIdentificationNumber() {

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock data
        List<ICustomerController.NewCustomers> newCustomers = Collections.singletonList(
            new ICustomerController.NewCustomers("John Doe", "123456789", "john@example.com", null, null, null, null, null, null));
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        Room room = new Room();
        room.setId(UUID.randomUUID());

        Site site = new Site();
        site.setId(UUID.randomUUID());
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId()))).thenReturn(java.util.Optional.of(site));

        // Call the method under test and expect a HttpClientErrorException
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
            ticketService.checkNewCustomers(newCustomers, ticket, room));

        // Verify that the correct exception is thrown with the expected status code and message
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 IdentificationNumber is incorrect", exception.getMessage());
    }

    @Test
    void testCheckNewCustomersWithExceptionDuringMapping() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Mock data
        List<ICustomerController.NewCustomers> newCustomers = Collections.singletonList(
            new ICustomerController.NewCustomers("John Doe", "123456789112", "john@example.com", null, null, null, null, null, null));
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        Room room = new Room();
        room.setId(UUID.randomUUID());

        Site site = new Site();
        site.setId(UUID.randomUUID());
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId()))).thenReturn(java.util.Optional.of(site));

        when(customerRepository.findByIdentificationNumberAndOrganizationId(any(String.class), any(String.class))).thenReturn(null);

        // Mock ModelMapper to throw an exception during mapping
        when(mapper.map(any(ICustomerController.NewCustomers.class), Mockito.eq(Customer.class))).thenThrow(new RuntimeException("Mapping error"));

        // Call the method under test and expect a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            ticketService.checkNewCustomers(newCustomers, ticket, room));

        // Verify that the correct exception is thrown with the expected message
        assertEquals("Mapping error", exception.getMessage());
    }

}
