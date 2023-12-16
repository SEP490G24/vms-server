package fpt.edu.capstone.vms.persistence.service.impl;

import com.google.zxing.WriterException;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Commune;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMapPk;
import fpt.edu.capstone.vms.persistence.entity.District;
import fpt.edu.capstone.vms.persistence.entity.Province;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.repository.ReasonRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.util.EmailUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import fpt.edu.capstone.vms.util.SettingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static fpt.edu.capstone.vms.constants.Constants.Purpose.MEETING;
import static fpt.edu.capstone.vms.constants.Constants.Purpose.OTHERS;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_REALM_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_RESOURCE_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.REALM_ADMIN;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_ORGANIZATION;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_SITE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        reasonRepository = mock(ReasonRepository.class);
        userRepository = mock(UserRepository.class);
        customerRepository = mock(CustomerRepository.class);

        ticketService = new TicketServiceImpl(ticketRepository
            , customerRepository, templateRepository
            , mapper, roomRepository, siteRepository
            , customerTicketMapRepository
            , emailUtils, auditLogRepository, settingUtils, userRepository, reasonRepository);

        Jwt jwt = mock(Jwt.class);

        SecurityUtils.UserDetails userDetails = new SecurityUtils.UserDetails();
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority(PREFIX_REALM_ROLE + REALM_ADMIN),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_ORGANIZATION),
            new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + SCOPE_SITE)
        );


        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        when(authentication.getAuthorities()).thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> {
            userDetails.setRealmAdmin(false);
            userDetails.setOrganizationAdmin(true);
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
    @DisplayName("Given Draft Ticket, When Creating, Then Set Status to DRAFT")
    public void givenDraftTicket_WhenCreating_ThenSetStatusToDraft() {
//        List<ICustomerController.NewCustomers> newCustomers = Collections.singletonList(
//            new ICustomerController.NewCustomers("John Doe", "123456789112", "john@example.com", null, null, null, null, null, null));

        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));

        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(true);
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().plusMinutes(1));
        ticketInfo.setEndTime(LocalDateTime.now().plusMinutes(31));
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
        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));

        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().plusMinutes(1));
        ticketInfo.setEndTime(LocalDateTime.now().plusMinutes(31));
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
        customerTicketMap.setSendMail(false);
        customerTicketMap.setCheckInCode("abc");
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(customer.getId(), ticket.getId()));
        List<CustomerTicketMap> customerTicketMaps = new ArrayList<>();
        customerTicketMaps.add(customerTicketMap);
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId())).thenReturn(customerTicketMaps);
        when(customerRepository.findById(customerTicketMap.getCustomerTicketMapPk().getCustomerId())).thenReturn(Optional.of(customer));
        when(customerTicketMapRepository.findByCheckInCode(customerTicketMap.getCheckInCode())).thenReturn(customerTicketMap);

        Commune commune = new Commune().setName("abc");
        District district = new District().setName("abc");
        Province province = new Province().setName("abc");

        site.setCommune(commune);
        site.setDistrict(district);
        site.setProvince(province);

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
        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));

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

        assertThrows(CustomException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    @DisplayName("Given Pending Ticket, When Creating, Then Set Status to PENDING")
    public void givenTicketWithRoomNull_WhenCreating_ThenThrowException() {
        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));

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

        assertThrows(CustomException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    @DisplayName("Given Pending Ticket, When Creating, Then Set Status to PENDING")
    public void givenTicketWithRoomHaveTime_WhenCreating_ThenThrowException() {
        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));

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

        assertThrows(CustomException.class, () -> ticketService.create(ticketInfo));
    }


    @Test
    public void testCreatePendingTicketWithInvalidSiteNull() {
        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));
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

        assertThrows(CustomException.class, () -> ticketService.create(ticketInfo));
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

        assertThrows(CustomException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    public void testCreatePendingTicketWithInvalidSiteId() {
        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));
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

        assertThrows(CustomException.class, () -> ticketService.create(ticketInfo));
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

        assertThrows(CustomException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket Info with Other Purpose and No Purpose Note, When Creating, Then Throw Exception")
    public void givenTicketInfoWithOtherPurposeAndNoPurposeNote_WhenCreating_ThenThrowException() {
        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));
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

        assertThrows(CustomException.class, () -> ticketService.create(ticketInfo));
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
    }

    @Test
    public void testUpdateBookMarkWithEmptyPayload() {
        ITicketController.TicketBookmark ticketBookmark = null; // Empty payload

        assertThrows(CustomException.class, () -> ticketService.updateBookMark(ticketBookmark));
    }

    @Test
    public void testUpdateBookMarkWithInvalidTicketId() {
        ITicketController.TicketBookmark ticketBookmark = new ITicketController.TicketBookmark();
        ticketBookmark.setTicketId("06eb43a7-6ea8-4744-8231-760559fe2c06");

        when(ticketRepository.findById(UUID.fromString(ticketBookmark.getTicketId()))).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> ticketService.updateBookMark(ticketBookmark));
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

        assertThrows(CustomException.class, () -> ticketService.updateBookMark(ticketBookmark));
    }

    @Test
    public void testUpdateBookMarkWithNullTicket() {
        ITicketController.TicketBookmark ticketBookmark = new ITicketController.TicketBookmark();
        ticketBookmark.setTicketId("06eb43a7-6ea8-4744-8231-760559fe2c06");

        when(ticketRepository.findById(UUID.fromString(ticketBookmark.getTicketId()))).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> ticketService.updateBookMark(ticketBookmark));
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

        assertThrows(CustomException.class, () -> ticketService.deleteTicket(ticketId));
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

        assertThrows(CustomException.class, () -> ticketService.deleteTicket(ticketId));
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
        mockTicket.setUsername("mocked_username");
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        Reason reason = new Reason();
        reason.setName("Reason");
        when(reasonRepository.findById(cancelTicket.getReasonId())).thenReturn(Optional.of(reason));

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

        User user = new User();
        when(userRepository.findFirstByUsername(mockTicket.getUsername())).thenReturn(user);

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
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3)); // Start time is after 2 hours
        mockTicket.setEndTime(LocalDateTime.now().plusHours(6)); // Start time is after 2 hours
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

        assertThrows(CustomException.class, () -> ticketService.cancelTicket(cancelTicket));
    }

    @Test
    @DisplayName("Given Ticket with Invalid ID to Cancel, When Cancelling, Then Throw Exception")
    public void givenTicketWithInvalidIdToCancel_WhenCancelling_ThenThrowException() {
        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> ticketService.cancelTicket(cancelTicket));
    }

    @Test
    @DisplayName("Given Ticket with StartTime Before 2 Hours to Cancel, When Cancelling, Then Throw Exception")
    public void givenTicketWithStartTimeBefore2HoursToCancel_WhenCancelling_ThenThrowException() {

        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        Ticket mockTicket = new Ticket();
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
        mockTicket.setEndTime(LocalDateTime.now().plusHours(1));
        mockTicket.setStartTime(LocalDateTime.now().plusHours(1)); // Start time is before 2 hours
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        assertThrows(CustomException.class, () -> ticketService.cancelTicket(cancelTicket));
    }

    @Test
    @DisplayName("Given Unauthorized User to Cancel, When Cancelling, Then Throw Exception")
    public void givenUnauthorizedUserToCancel_WhenCancelling_ThenThrowException() {

        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));

        Ticket mockTicket = new Ticket();
        mockTicket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3)); // Start time is after 2 hours
        mockTicket.setEndTime(LocalDateTime.now().plusHours(6)); // Start time is after 2 hours
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
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        when(siteRepository.findById(UUID.fromString(mockTicket.getSiteId()))).thenReturn(Optional.of(site));
        boolean result = ticketService.cancelTicket(cancelTicket);

        assertTrue(result);
    }

    @Test
    @DisplayName("Given Valid TicketInfo, When Updating, Then Update Ticket")
    public void givenValidTicketInfo_WhenUpdating_ThenUpdateTicket() {
        List<ICustomerController.CustomerInfo> oldCustomer = new ArrayList<>();
        oldCustomer.add(new ICustomerController.CustomerInfo().setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));

        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        ticketInfo.setRoomId("06eb43a7-6ea8-4744-8231-760559fe2c07");
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setStartTime(LocalDateTime.now().plusMinutes(1));
        ticketInfo.setOldCustomers(oldCustomer);
        ticketInfo.setDraft(false);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3));
        mockTicket.setEndTime(LocalDateTime.now().plusHours(4));
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
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

        Customer customer = new Customer();
        customer.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09"));
        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setSendMail(false);
        customerTicketMap.setCheckInCode("abc");
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(mockTicket.getId(), UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c09")));
        customerTicketMap.setCustomerEntity(customer);

        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(mockTicket.getId())).thenReturn(List.of(customerTicketMap));
        when(customerTicketMapRepository.findById(customerTicketMap.getCustomerTicketMapPk())).thenReturn(Optional.of(customerTicketMap));
        Template template = new Template();
        template.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        when(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL)).thenReturn(template.getId().toString());
        when(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL)).thenReturn(template.getId().toString());
        when(customerTicketMapRepository.findByCheckInCode(customerTicketMap.getCheckInCode())).thenReturn(customerTicketMap);
        when(templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL)))).thenReturn(Optional.of(template));
        when(templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL)))).thenReturn(Optional.of(template));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        User user = new User();
        user.setFirstName("first_name");
        when(userRepository.findFirstByUsername(mockTicket.getUsername())).thenReturn(user);

        Ticket result = ticketService.updateTicket(ticketInfo);
        assertNotNull(result);

    }

    @Test
    @DisplayName("Given Ticket Not for Current User, When Updating, Then Throw Exception")
    public void givenTicketNotForCurrentUser_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        ticketInfo.setStartTime(LocalDateTime.now().plusMinutes(1));

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("other_username");
        mockTicket.setStartTime(LocalDateTime.now().plusHours(1));
        mockTicket.setEndTime(LocalDateTime.now().plusHours(3));
        mockTicket.setStatus(Constants.StatusTicket.PENDING);

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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket with Invalid ID, When Updating, Then Throw Exception")
    public void givenTicketWithInvalidId_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        ticketInfo.setId(null);

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Updated Ticket with Purpose Note When Purpose Is Not OTHERS, When Updating, Then Throw Exception")
    public void givenUpdatedTicketWithPurposeNoteWhenPurposeIsNotOthers_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        ticketInfo.setRoomId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticketInfo.setStartTime(LocalDateTime.now().plusMinutes(1));

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        mockTicket.setUsername("mocked_username");
        mockTicket.setPurpose(MEETING); // Not OTHERS
        mockTicket.setPurposeNote("TEST");
        mockTicket.setStartTime(LocalDateTime.now());
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
        mockTicket.setEndTime(LocalDateTime.now().plusMinutes(1));


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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Updated Ticket with Purpose Note When Purpose Is Not OTHERS, When Updating, Then Throw Exception")
    public void givenUpdatedTicketWithPurposeNoteNUllWhenPurposeIsOthers_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        ticketInfo.setRoomId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticketInfo.setStartTime(LocalDateTime.now().plusMinutes(1));


        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setRoomId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        mockTicket.setUsername("mocked_username");
        mockTicket.setPurpose(OTHERS);
        mockTicket.setPurposeNote(null);
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
        mockTicket.setEndTime(LocalDateTime.now().plusMinutes(1));
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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Room Booked, When Updating with the Same Room, When Updating, Then Throw Exception")
    public void givenRoomBooked_WhenUpdatingWithTheRoomNull_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId.toString());

        LocalDateTime newStartTime = LocalDateTime.now();
        LocalDateTime newEndTime = newStartTime.plusHours(2);
        ticketInfo.setStartTime(newStartTime);
        ticketInfo.setEndTime(newEndTime);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setRoomId(roomId);
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
        mockTicket.setEndTime(LocalDateTime.now().plusMinutes(1));
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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Room Booked, When Updating with the Same Room, When Updating, Then Throw Exception")
    public void givenRoomBooked_WhenUpdatingWithTheRoomIsNotInSite_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId.toString());

        LocalDateTime newStartTime = LocalDateTime.now();
        LocalDateTime newEndTime = newStartTime.plusHours(2);
        ticketInfo.setStartTime(newStartTime);
        ticketInfo.setEndTime(newEndTime);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setRoomId(roomId);
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setStatus(Constants.StatusTicket.PENDING);
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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Room Booked, When Updating with the Same Room, When Updating, Then Throw Exception")
    public void givenRoomBooked_WhenUpdatingWithTheRoomHaveTicket_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId.toString());

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
        mockTicket.setStatus(Constants.StatusTicket.PENDING);

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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    public void givenRoomBooked_WhenUpdatingWithTicketCancel_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId.toString());

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
        mockTicket.setStatus(Constants.StatusTicket.CANCEL);

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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    public void givenRoomBooked_WhenUpdatingWithTicketComplete_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId.toString());

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
        mockTicket.setStatus(Constants.StatusTicket.COMPLETE);

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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    public void givenRoomBooked_WhenUpdatingWithTicketExpired_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId.toString());

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
        mockTicket.setEndTime(LocalDateTime.now());
        mockTicket.setStatus(Constants.StatusTicket.PENDING);

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

        assertThrows(CustomException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Filter Parameters, When Filtering Tickets, Then Return Page of Tickets")
    public void givenFilterParameters_WhenFilteringTickets_ThenReturnPageOfTickets() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
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

        Page<Ticket> filteredTickets = ticketService.getAllTicketPageableByUsername(pageableSort, names, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, bookmark, keyword);

        assertNull(filteredTickets);
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

        Page<Ticket> filteredTickets = ticketService.filterAllBySite(pageable, names, siteList, usernames, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, null, keyword);

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
        ticketEntity.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c07"); // A different site ID
        ticketEntity.setEndTime(LocalDateTime.now().plusHours(1));

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
        assertThrows(CustomException.class, () -> {
            ticketService.findByQRCode(checkInCode);
        });
    }

    @Test
    @DisplayName("Given CheckInPayload Request, When Updating Status, Then Update Customer Ticket Map")
    public void givenUpdateStatusTicketOfCustomerRequest_WhenUpdatingStatus_ThenUpdateCustomerTicketMap() {
        // Mock input parameters
        ITicketController.CheckInPayload checkInPayload = new ITicketController.CheckInPayload();
        checkInPayload.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        checkInPayload.setCheckInCode("checkInCode");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.PENDING);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode()))
            .thenReturn(customerTicketMap);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Ticket ticket = new Ticket();
        ticket.setStartTime(LocalDateTime.now().minusHours(2));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(1));
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c07");
        ticket.setStatus(Constants.StatusTicket.PENDING);
        customerTicketMap.setTicketEntity(ticket);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(Optional.of(ticket));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));

        when(ticketRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(ticket));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            // Kiểm tra giá trị của auditLog nếu cần
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c07", auditLog.getSiteId());
            assertEquals("06eb43a7-6ea8-4744-8231-760559fe2c08", auditLog.getOrganizationId());
            assertEquals(customerTicketMap.getId().toString(), auditLog.getPrimaryKey());
            assertEquals("CustomerTicketMap", auditLog.getTableName());
            assertEquals(Constants.AuditType.CREATE, auditLog.getAuditType());
            assertEquals(null, auditLog.getOldValue());
            assertEquals(customerTicketMap.toString(), auditLog.getNewValue());
            return auditLog;
        });

        ITicketController.TicketByQRCodeResponseDTO ticketByQRCodeResponseDTO = new ITicketController.TicketByQRCodeResponseDTO();
        ticketByQRCodeResponseDTO.setSiteId(ticket.getSiteId());
        when(mapper.map(customerTicketMap, ITicketController.TicketByQRCodeResponseDTO.class)).thenReturn(ticketByQRCodeResponseDTO);

        // Call the method under test
        ticketService.updateStatusCustomerOfTicket(checkInPayload);

        // Verify that the customerTicketMap has been updated with the new status, reasonId, and reasonNote
        assertEquals(checkInPayload.getStatus(), customerTicketMap.getStatus());

        // Verify that the customerTicketMapRepository.save() has been called
        verify(customerTicketMapRepository).save(customerTicketMap);

    }

    @Test
    public void testCheckInCustomer_WithCustomerIsCheckIn() {
        // Mock input parameters
        ITicketController.CheckInPayload checkInPayload = new ITicketController.CheckInPayload();
        checkInPayload.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        checkInPayload.setCheckInCode("checkInCode");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode()))
            .thenReturn(customerTicketMap);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Ticket ticket = new Ticket();
        ticket.setStartTime(LocalDateTime.now().minusHours(2));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(1));
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticket.setStatus(Constants.StatusTicket.PENDING);
        customerTicketMap.setTicketEntity(ticket);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(Optional.of(ticket));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));
        // Verify that the customerTicketMap has been updated with the new status, reasonId, and reasonNote
        assertThrows(CustomException.class, () -> ticketService.updateStatusCustomerOfTicket(checkInPayload));

    }

    @Test
    public void testCheckInCustomer_WithCustomerIsCheckOUT() {
        // Mock input parameters
        ITicketController.CheckInPayload checkInPayload = new ITicketController.CheckInPayload();
        checkInPayload.setStatus(Constants.StatusCustomerTicket.CHECK_OUT);
        checkInPayload.setCheckInCode("checkInCode");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.CHECK_OUT);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode()))
            .thenReturn(customerTicketMap);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Ticket ticket = new Ticket();
        ticket.setStartTime(LocalDateTime.now().minusHours(2));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(1));
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticket.setStatus(Constants.StatusTicket.PENDING);
        customerTicketMap.setTicketEntity(ticket);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(Optional.of(ticket));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));
        // Verify that the customerTicketMap has been updated with the new status, reasonId, and reasonNote
        assertThrows(CustomException.class, () -> ticketService.updateStatusCustomerOfTicket(checkInPayload));

    }

    @Test
    public void testCheckInCustomer_WithTicketIsComplete() {
        // Mock input parameters
        ITicketController.CheckInPayload checkInPayload = new ITicketController.CheckInPayload();
        checkInPayload.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        checkInPayload.setCheckInCode("checkInCode");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.PENDING);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode()))
            .thenReturn(customerTicketMap);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Ticket ticket = new Ticket();
        ticket.setStartTime(LocalDateTime.now().minusHours(2));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(1));
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticket.setStatus(Constants.StatusTicket.COMPLETE);
        customerTicketMap.setTicketEntity(ticket);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(Optional.of(ticket));

        // Verify that the customerTicketMap has been updated with the new status, reasonId, and reasonNote
        assertThrows(CustomException.class, () -> ticketService.updateStatusCustomerOfTicket(checkInPayload));

    }

    @Test
    public void testCheckInCustomer_WithTicketIsDraft() {
        // Mock input parameters
        ITicketController.CheckInPayload checkInPayload = new ITicketController.CheckInPayload();
        checkInPayload.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        checkInPayload.setCheckInCode("checkInCode");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.PENDING);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode()))
            .thenReturn(customerTicketMap);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Ticket ticket = new Ticket();
        ticket.setStartTime(LocalDateTime.now().minusHours(2));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(1));
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticket.setStatus(Constants.StatusTicket.DRAFT);
        customerTicketMap.setTicketEntity(ticket);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(Optional.of(ticket));

        // Verify that the customerTicketMap has been updated with the new status, reasonId, and reasonNote
        assertThrows(CustomException.class, () -> ticketService.updateStatusCustomerOfTicket(checkInPayload));

    }

    @Test
    public void testCheckInCustomer_WithTicketIsCancel() {
        // Mock input parameters
        ITicketController.CheckInPayload checkInPayload = new ITicketController.CheckInPayload();
        checkInPayload.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        checkInPayload.setCheckInCode("checkInCode");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.PENDING);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode()))
            .thenReturn(customerTicketMap);

        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
        Ticket ticket = new Ticket();
        ticket.setStartTime(LocalDateTime.now().minusHours(2));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(1));
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticket.setStatus(Constants.StatusTicket.CANCEL);
        customerTicketMap.setTicketEntity(ticket);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(Optional.of(ticket));

        // Verify that the customerTicketMap has been updated with the new status, reasonId, and reasonNote
        assertThrows(CustomException.class, () -> ticketService.updateStatusCustomerOfTicket(checkInPayload));

    }

    @Test
    void testCheckOutCustomer() {
        // Mock data
        UUID customerTicketMapId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        String siteId = UUID.randomUUID().toString();
        LocalDateTime currentTime = LocalDateTime.now();
        Integer reasonId = 1;

        ITicketController.CheckInPayload checkOutPayload = new ITicketController.CheckInPayload();
        checkOutPayload.setCheckInCode("ABCDE");
        checkOutPayload.setStatus(Constants.StatusCustomerTicket.CHECK_OUT);
        checkOutPayload.setReasonId(reasonId);
        checkOutPayload.setReasonNote("Customer requested checkout");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(customerTicketMapId, ticketId));
        customerTicketMap.setCheckInTime(currentTime);  // Set a past check-in time

        Ticket ticket = new Ticket();
        ticket.setStartTime(LocalDateTime.now().minusMinutes(1));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(1));
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c07");
        ticket.setStatus(Constants.StatusTicket.PENDING);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(java.util.Optional.of(ticket));

        customerTicketMap.setTicketEntity(ticket);
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        // Mock repository behavior
        when(customerTicketMapRepository.findByCheckInCodeIgnoreCase("ABCDE")).thenReturn(customerTicketMap);
        when(siteRepository.findById(UUID.fromString(siteId))).thenReturn(java.util.Optional.of(new Site()));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));

        ITicketController.TicketByQRCodeResponseDTO ticketByQRCodeResponseDTO = new ITicketController.TicketByQRCodeResponseDTO();
        ticketByQRCodeResponseDTO.setSiteId(ticket.getSiteId());
        when(mapper.map(customerTicketMap, ITicketController.TicketByQRCodeResponseDTO.class)).thenReturn(ticketByQRCodeResponseDTO);

        // Call the method under test
        ticketService.updateStatusCustomerOfTicket(checkOutPayload);

        // Verify that the status is updated to CHECK_OUT
        Mockito.verify(customerTicketMapRepository).save(customerTicketMap);

        // Verify that the audit log is created
        Mockito.verify(auditLogRepository).save(any(AuditLog.class));

        // You can add more assertions if needed
    }

    @Test
    void testRejectCustomer() {
        // Mock data
        UUID customerTicketMapId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        String siteId = UUID.randomUUID().toString();
        LocalDateTime currentTime = LocalDateTime.now();
        Integer reasonId = 1;

        ITicketController.CheckInPayload checkOutPayload = new ITicketController.CheckInPayload();
        checkOutPayload.setCheckInCode("ABCDE");
        checkOutPayload.setStatus(Constants.StatusCustomerTicket.REJECT);
        checkOutPayload.setReasonId(reasonId);
        checkOutPayload.setReasonNote("Customer requested checkout");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.REJECT);
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(customerTicketMapId, ticketId));
        customerTicketMap.setCheckInTime(currentTime);  // Set a past check-in time

        Ticket ticket = new Ticket();
        ticket.setStartTime(LocalDateTime.now().minusMinutes(1));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(1));
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        ticket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c07");
        ticket.setStatus(Constants.StatusTicket.PENDING);
        when(ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId())).thenReturn(java.util.Optional.of(ticket));

        customerTicketMap.setTicketEntity(ticket);
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        // Mock repository behavior
        when(customerTicketMapRepository.findByCheckInCodeIgnoreCase("ABCDE")).thenReturn(customerTicketMap);
        when(siteRepository.findById(UUID.fromString(siteId))).thenReturn(java.util.Optional.of(new Site()));
        when(siteRepository.findById(UUID.fromString(ticket.getSiteId()))).thenReturn(Optional.of(site));

        ITicketController.TicketByQRCodeResponseDTO ticketByQRCodeResponseDTO = new ITicketController.TicketByQRCodeResponseDTO();
        ticketByQRCodeResponseDTO.setSiteId(ticket.getSiteId());
        when(mapper.map(customerTicketMap, ITicketController.TicketByQRCodeResponseDTO.class)).thenReturn(ticketByQRCodeResponseDTO);

        // Call the method under test
        ticketService.updateStatusCustomerOfTicket(checkOutPayload);

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
        Pageable pageable = PageRequest.of(0, 10);
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        UUID roomId = UUID.randomUUID();
        Constants.StatusCustomerTicket status = Constants.StatusCustomerTicket.CHECK_IN;
        Constants.Purpose purpose = Constants.Purpose.CONFERENCES;
        String keyword = "SEARCH";
        List<String> sites = new ArrayList<>();
        sites.add("06eb43a7-6ea8-4744-8231-760559fe2c07");

        List<CustomerTicketMap> ticketMapList = new ArrayList<>();

        Page<CustomerTicketMap> customerTicketMapPage = new PageImpl<>(List.of());

        when(customerTicketMapRepository.filter(pageable, sites, null, null, null, null, roomId, status, purpose, keyword))
            .thenReturn(customerTicketMapPage);

        // Call the method under test
        Page<CustomerTicketMap> result = ticketService.filterTicketAndCustomer(
            pageableSort, null, null, roomId, purpose, null, null, null, null, null, null, null, null, null, keyword
        );

        // Verify that the repository filter method was called with the correct arguments
        Mockito.verify(customerTicketMapRepository).filter(pageable, sites, null, null, null, null, roomId, status, purpose, keyword);

        // Verify that the result has the expected content
        assertEquals(ticketMapList, result.getContent());
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
        assertThrows(CustomException.class, () -> ticketService.findByTicket(ticketId));


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
        assertThrows(CustomException.class, () -> ticketService.findByTicket(ticketId));

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
        assertThrows(CustomException.class, () -> ticketService.findByTicket(ticketId));

        // Verify that the repository findById method was called with the correct argument
        Mockito.verify(ticketRepository).findById(ticketId);

        // You can add more assertions if needed
    }

    @Test
    void testFindByTicketForAdminWithInvalidSiteId() {
        // Mock data
        UUID ticketId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID invalidSiteId = UUID.fromString("7da5477f-ed0c-446c-b154-4cd9720414c3");

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSiteId(UUID.randomUUID().toString());  // Set a different site ID

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("7da5477f-ed0c-446c-b154-4cd9720414c3");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(siteRepository.findById(invalidSiteId)).thenReturn(Optional.empty());

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(CustomException.class, () -> ticketService.findByTicket(ticketId));

        // Verify that the repository findById method was called with the correct argument
        Mockito.verify(ticketRepository).findById(ticketId);


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
        assertThrows(CustomException.class, () -> ticketService.findByTicket(ticketId));
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

        Commune commune = new Commune().setName("abc");
        District district = new District().setName("abc");
        Province province = new Province().setName("abc");

        site.setCommune(commune);
        site.setDistrict(district);
        site.setProvince(province);

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));
        customerTicketMap.setCheckInCode(checkInCode);
        customerTicketMap.setTicketEntity(ticket);
        customerTicketMap.setCustomerEntity(customer);
        customerTicketMap.setSendMail(true);
        when(customerTicketMapRepository.findByCheckInCode(checkInCode)).thenReturn(customerTicketMap);
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
        ticketService.sendEmail(customer, ticket, room, checkInCode, false, false);

        // You can add more assertions if needed
    }

    @Test
    void testSendEmailWithMissingCustomer() {
        // Mock data
        Ticket ticket = new Ticket();
        Room room = new Room();
        String checkInCode = "ABCDE";

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(CustomException.class, () -> ticketService.sendEmail(null, ticket, room, checkInCode, false, false));

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
        assertThrows(CustomException.class, () -> ticketService.sendEmail(customer, ticket, room, checkInCode, false, false));

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
        ITicketController.TicketFilterDTO result = ticketService.findByTicket(ticketId);

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
        assertThrows(CustomException.class, () -> ticketService.findByTicket(ticketId));

        // You can add more assertions if needed
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
        Constants.StatusTicket status = Constants.StatusTicket.PENDING;
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
        List<Ticket> result = ticketService.filterAllBySite(names, null, usernames, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, null, keyword);

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
        Constants.StatusTicket status = Constants.StatusTicket.PENDING;
        Constants.Purpose purpose = Constants.Purpose.CONFERENCES;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        LocalDateTime startTimeStart = LocalDateTime.now().minusHours(1);
        LocalDateTime startTimeEnd = LocalDateTime.now().plusHours(1);
        LocalDateTime endTimeStart = LocalDateTime.now().plusHours(2);
        LocalDateTime endTimeEnd = LocalDateTime.now().plusHours(3);
        String createdBy = "mocked_username";
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
        List<Ticket> result = ticketService.getAllTicketByUsername(
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
            new ICustomerController.NewCustomers("John Doe", "123456789", "john@example.com", null, null, null));
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        Room room = new Room();
        room.setId(UUID.randomUUID());

        Site site = new Site();
        site.setId(UUID.randomUUID());
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId()))).thenReturn(java.util.Optional.of(site));

        // Call the method under test and expect a HttpClientErrorException
        assertThrows(CustomException.class, () ->
            ticketService.checkNewCustomers(newCustomers, ticket, room, true));
    }

    @Test
    void testFilterTicketByRoom() {
        // Test data
        List<String> names = List.of("Room1", "Room2");
        List<String> sites = new ArrayList<>();
        List<String> usernames = List.of("user1", "user2");
        UUID roomId = UUID.randomUUID();
        Constants.StatusTicket status = Constants.StatusTicket.PENDING;
        Constants.Purpose purpose = Constants.Purpose.MEETING;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        LocalDateTime startTimeStart = LocalDateTime.now().minusHours(2);
        LocalDateTime startTimeEnd = LocalDateTime.now();
        LocalDateTime endTimeStart = LocalDateTime.now().plusHours(1);
        LocalDateTime endTimeEnd = LocalDateTime.now().plusHours(3);
        String createdBy = "admin";
        String lastUpdatedBy = "admin";
        String keyword = "search";

        // Mock behavior
        // Assuming you have proper implementations for filterAllBySite and roomRepository.filter
        List<Ticket> tickets = List.of(new Ticket(), new Ticket());
        when(ticketService.filterAllBySite(null, sites, null, null, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, null, keyword))
            .thenReturn(tickets);

        List<Room> rooms = new ArrayList<>();
        when(roomRepository.filter(null, SecurityUtils.getListSiteToUUID(siteRepository, sites), null, null, null, null, null)).thenReturn(rooms);

        // Execute the method
        ITicketController.TicketByRoomResponseDTO result = ticketService.filterTicketByRoom(
            names, sites, usernames, roomId, status, purpose, createdOnStart, createdOnEnd,
            startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, keyword
        );

        // Assertions
        assertNotNull(result);
        assertEquals(rooms, result.getRooms());
        assertEquals(0, result.getTickets().size());

        verify(roomRepository).filter(null, SecurityUtils.getListSiteToUUID(siteRepository, sites), null, null, null, null, null);
    }

    @Test
    void testAddCardCustomerTicket() {
        // Test data
        String checkInCode = "CHECK_IN_CODE";
        String orgId = "ORG_ID";
        String siteId = "SITE_ID";
        String cardId = "CARD_ID";

        // Mock behavior
        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInCode)).thenReturn(customerTicketMap);

        Ticket ticket = new Ticket();
        ticket.setSiteId(siteId);
        when(ticketRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(ticket));

        when(siteRepository.existsByIdAndOrganizationId(any(UUID.class), any(UUID.class))).thenReturn(true);

        when(settingUtils.getBoolean(Constants.SettingCode.CONFIGURATION_CARD)).thenReturn(true);

        when(customerTicketMapRepository.existsByCardIdAndStatus(cardId, Constants.StatusCustomerTicket.CHECK_IN)).thenReturn(false);

        // Execute the method
        boolean result = ticketService.addCardCustomerTicket(new ITicketController.CustomerTicketCardDTO(checkInCode, cardId));

        // Assertions
        assertTrue(result);

        // Verify method calls
        verify(customerTicketMapRepository).save(any());
    }

    @Test
    void testCheckOldCustomers() {

        // Mock input data
        List<String> oldCustomers = Arrays.asList("c353835a-5e1e-4df5-973f-aec252bf260f");
        Ticket ticket = new Ticket(); // Provide necessary ticket data
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        Room room = new Room(); // Provide necessary room data

        // Mock site repository behavior
        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        site.setOrganizationId(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"));
        when(siteRepository.findById(site.getId())).thenReturn(java.util.Optional.of(site));

        // Mock customer ticket map repository behavior
        CustomerTicketMap customerTicketMap1 = new CustomerTicketMap(); // Provide necessary data for customerTicketMap1
        customerTicketMap1.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260a"), ticket.getId()));
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId()))
            .thenReturn(Arrays.asList(customerTicketMap1));

        // Mock template repository behavior
        Template template = new Template(); // Provide necessary template data
        when(templateRepository.findById(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"))).thenReturn(java.util.Optional.of(template));
        when(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL)).thenReturn("c353835a-5e1e-4df5-973f-aec252bf260f");

        when(customerRepository.existsByIdAndAndOrganizationId(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"), "c353835a-5e1e-4df5-973f-aec252bf260f")).thenReturn(true);

        // Call the method
        assertDoesNotThrow(() -> ticketService.checkOldCustomers(oldCustomers, ticket, room, true, Arrays.asList(customerTicketMap1)));
    }

    @Test
    void testCheckOldCustomers_notPermission() {

        // Mock input data
        List<String> oldCustomers = Arrays.asList("27c476cd-cfd3-4285-b6b2-23b8787f8c26", "c353835a-5e1e-4df5-973f-aec252bf260f");
        Ticket ticket = new Ticket(); // Provide necessary ticket data
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        Room room = new Room(); // Provide necessary room data

        // Mock site repository behavior
        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        site.setOrganizationId(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"));
        when(siteRepository.findById(site.getId())).thenReturn(java.util.Optional.of(site));

        // Mock customer ticket map repository behavior
        CustomerTicketMap customerTicketMap1 = new CustomerTicketMap(); // Provide necessary data for customerTicketMap1
        customerTicketMap1.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"), ticket.getId()));
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId()))
            .thenReturn(Arrays.asList(customerTicketMap1));

        // Mock customer repository behavior
        when(customerRepository.existsByIdAndAndOrganizationId(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"), "c353835a-5e1e-4df5-973f-aec252bf260f")).thenReturn(true);

        // Mock template repository behavior
        Template template = new Template(); // Provide necessary template data
        when(templateRepository.findById(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"))).thenReturn(java.util.Optional.of(template));
        when(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL)).thenReturn("c353835a-5e1e-4df5-973f-aec252bf260f");

        // Call the method
        assertThrows(CustomException.class, () -> ticketService.checkOldCustomers(oldCustomers, ticket, room, true, Arrays.asList(customerTicketMap1)));

    }

    @Test
    void testCheckOldCustomers_AllEmpty() {

        List<String> oldCustomers = new ArrayList<>();
        // Mock input data
        Ticket ticket = new Ticket(); // Provide necessary ticket data
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        Room room = new Room(); // Provide necessary room data

        // Mock site repository behavior
        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        site.setOrganizationId(UUID.randomUUID());
        when(siteRepository.findById(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"))).thenReturn(java.util.Optional.of(site));

        // Mock customer ticket map repository behavior
        CustomerTicketMap customerTicketMap1 = new CustomerTicketMap(); // Provide necessary data for customerTicketMap1
        customerTicketMap1.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"), ticket.getId()));
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId()))
            .thenReturn(Arrays.asList(customerTicketMap1));

        // Mock template repository behavior
        Template template = new Template(); // Provide necessary template data
        when(templateRepository.findById(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"))).thenReturn(java.util.Optional.of(template));
        when(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL)).thenReturn("c353835a-5e1e-4df5-973f-aec252bf260f");

        // Call the method
        assertDoesNotThrow(() -> ticketService.checkOldCustomers(oldCustomers, ticket, room, true, Arrays.asList(customerTicketMap1)));

    }

    @Test
    void testCheckOldCustomers_removeCustomer() {

        // Mock input data
        List<String> oldCustomers = Arrays.asList("c353835a-5e1e-4df5-973f-aec252bf260f");
        Ticket ticket = new Ticket(); // Provide necessary ticket data
        ticket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        Room room = new Room(); // Provide necessary room data

        // Mock site repository behavior
        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        site.setOrganizationId(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"));
        when(siteRepository.findById(site.getId())).thenReturn(java.util.Optional.of(site));

        // Mock customer ticket map repository behavior
        CustomerTicketMap customerTicketMap1 = new CustomerTicketMap(); // Provide necessary data for customerTicketMap1
        customerTicketMap1.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"), ticket.getId()));
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId()))
            .thenReturn(Arrays.asList(customerTicketMap1));

        // Mock template repository behavior
        Template template = new Template(); // Provide necessary template data
        when(templateRepository.findById(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"))).thenReturn(java.util.Optional.of(template));
        when(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL)).thenReturn("c353835a-5e1e-4df5-973f-aec252bf260f");

        when(customerRepository.existsByIdAndAndOrganizationId(UUID.fromString("c353835a-5e1e-4df5-973f-aec252bf260f"), "c353835a-5e1e-4df5-973f-aec252bf260f")).thenReturn(true);

        // Call the method
        assertDoesNotThrow(() -> ticketService.checkOldCustomers(oldCustomers, ticket, room, true, Arrays.asList(customerTicketMap1)));
    }
}
