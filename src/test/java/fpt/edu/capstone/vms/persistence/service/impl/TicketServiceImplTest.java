package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMapPk;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.repository.*;
import fpt.edu.capstone.vms.util.EmailUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fpt.edu.capstone.vms.constants.Constants.Purpose.OTHERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketServiceImplTest {

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private EmailUtils emailUtils;

    @Mock
    private RoomRepository roomRepository;


    @Mock
    private CustomerTicketMapRepository customerTicketMapRepository;

    SecurityContext securityContext;
    Authentication authentication;

    ModelMapper mapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        mapper = mock(ModelMapper.class);
    }

    @Test
    @DisplayName("Given Draft Ticket, When Creating, Then Set Status to DRAFT")
    public void givenDraftTicket_WhenCreating_ThenSetStatusToDraft() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(true);


        // Create a mock Jwt object with the necessary claims
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Ticket ticket = new Ticket();
        ticket.setStatus(Constants.StatusTicket.DRAFT);
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);

        assertNotNull(ticket);
        assertEquals(Constants.StatusTicket.DRAFT, ticket.getStatus());
    }

    @Test
    @DisplayName("Given Pending Ticket, When Creating, Then Set Status to PENDING")
    public void givenPendingTicket_WhenCreating_ThenSetStatusToPending() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now());
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        ticketInfo.setTemplateId(UUID.randomUUID());

        // Create a mock Jwt object with the necessary claims
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

        Ticket ticket = new Ticket();
        ticket.setStatus(Constants.StatusTicket.PENDING);
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(ticket);

        assertNotNull(ticket);
        assertEquals(Constants.StatusTicket.PENDING, ticket.getStatus());
    }

    @Test
    public void testCreatePendingTicketWithInvalidStartTime() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(null); // Invalid start time
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("valid_site_id");
        ticketInfo.setTemplateId(UUID.randomUUID());

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(NullPointerException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    public void testCreatePendingTicketWithInvalidSiteId() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now());
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("invalid_site_id"); // Invalid site id
        ticketInfo.setTemplateId(UUID.randomUUID());

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(false);

        assertThrows(NullPointerException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket Info with Invalid Template, When Creating, Then Throw Exception")
    public void givenTicketInfoWithInvalidTemplate_WhenCreating_ThenThrowException() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now());
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("valid_site_id");
        ticketInfo.setTemplateId(UUID.randomUUID());

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(siteRepository.existsByIdAndOrganizationId(Mockito.any(UUID.class), Mockito.any(UUID.class))).thenReturn(true);
        when(templateRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(new Ticket());

        assertThrows(NullPointerException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket Info with Invalid Purpose, When Creating, Then Throw Exception")
    public void givenTicketInfoWithInvalidPurpose_WhenCreating_ThenThrowException() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now());
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("valid_site_id");
        ticketInfo.setTemplateId(UUID.randomUUID());
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

        assertThrows(NullPointerException.class, () -> ticketService.create(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket Info with Other Purpose and No Purpose Note, When Creating, Then Throw Exception")
    public void givenTicketInfoWithOtherPurposeAndNoPurposeNote_WhenCreating_ThenThrowException() {
        ITicketController.CreateTicketInfo ticketInfo = new ITicketController.CreateTicketInfo();
        ticketInfo.setDraft(false);
        ticketInfo.setStartTime(LocalDateTime.now());
        ticketInfo.setEndTime(LocalDateTime.now().plusHours(1));
        ticketInfo.setSiteId("valid_site_id");
        ticketInfo.setTemplateId(UUID.randomUUID());
        ticketInfo.setPurpose(OTHERS); // Other purpose, but no purpose note

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

        assertThrows(NullPointerException.class, () -> ticketService.create(ticketInfo));
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
        verify(ticketRepository, Mockito.times(1)).delete(mockTicket);
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
        cancelTicket.setTemplateId(UUID.randomUUID());

        Ticket mockTicket = new Ticket();
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        mockTicket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3)); // Start time is after 2 hours
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), "mocked_username")).thenReturn(true);
        when(templateRepository.findById(cancelTicket.getTemplateId())).thenReturn(Optional.of(new Template()));
        when(customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(mockTicket.getId())).thenReturn(new ArrayList<>());

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
        verify(ticketRepository, Mockito.times(1)).save(mockTicket);
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
        cancelTicket.setTemplateId(UUID.randomUUID());

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
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3)); // Start time is after 2 hours
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), "another_user")).thenReturn(false);

        boolean result = ticketService.cancelTicket(cancelTicket);

        assertFalse(result);
        verify(ticketRepository, Mockito.never()).save(mockTicket);
    }

    @Test
    @DisplayName("Given Ticket with Invalid Template to Cancel, When Cancelling, Then Throw Exception")
    public void givenTicketWithInvalidTemplateToCancel_WhenCancelling_ThenThrowException() {

        ITicketController.CancelTicket cancelTicket = new ITicketController.CancelTicket();
        cancelTicket.setTicketId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
        cancelTicket.setTemplateId(UUID.randomUUID());

        Ticket mockTicket = new Ticket();
        mockTicket.setStartTime(LocalDateTime.now().plusHours(3)); // Start time is after 2 hours
        when(ticketRepository.findById(cancelTicket.getTicketId())).thenReturn(Optional.of(mockTicket));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), "mocked_username")).thenReturn(true);
        when(templateRepository.findById(cancelTicket.getTemplateId())).thenReturn(Optional.empty());

        Site site = new Site();
        mockTicket.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c06");
        mockTicket.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c06"));
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


        assertTrue(ticketService.cancelTicket(cancelTicket));
    }

    @Test
    @DisplayName("Given Valid TicketInfo, When Updating, Then Update Ticket")
    public void givenValidTicketInfo_WhenUpdating_ThenUpdateTicket() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);

        LocalDateTime newStartTime = LocalDateTime.now();
        LocalDateTime newEndTime = newStartTime.plusHours(2);
        ticketInfo.setStartTime(newStartTime);
        ticketInfo.setEndTime(newEndTime);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setStartTime(LocalDateTime.now().plusHours(1));
        mockTicket.setEndTime(LocalDateTime.now().plusHours(3));

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        assertEquals(newStartTime, ticketInfo.getStartTime());
        assertEquals(newEndTime, ticketInfo.getEndTime());
    }

    @Test
    @DisplayName("Given Ticket Not for Current User, When Updating, Then Throw Exception")
    public void givenTicketNotForCurrentUser_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("other_user");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(NullPointerException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Ticket with Invalid ID, When Updating, Then Throw Exception")
    public void givenTicketWithInvalidId_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        ticketInfo.setId(null);

        assertThrows(NullPointerException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Updated Ticket with Purpose Note When Purpose Is Not OTHERS, When Updating, Then Throw Exception")
    public void givenUpdatedTicketWithPurposeNoteWhenPurposeIsNotOthers_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setPurpose(Constants.Purpose.MEETING); // Not OTHERS
        ticketInfo.setPurpose(Constants.Purpose.MEETING);
        ticketInfo.setPurposeNote("Purpose note"); // Purpose is not OTHERS, but there's a note

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);

        assertThrows(NullPointerException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Room Booked, When Updating with the Same Room, When Updating, Then Throw Exception")
    public void givenRoomBooked_WhenUpdatingWithTheSameRoom_WhenUpdating_ThenThrowException() {
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

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(new Room()));

        assertThrows(NullPointerException.class, () -> ticketService.updateTicket(ticketInfo));
    }

    @Test
    @DisplayName("Given Updated Ticket with Invalid Template, When Updating, Then Throw Exception")
    public void givenUpdatedTicketWithInvalidTemplate_WhenUpdating_ThenThrowException() {
        ITicketController.UpdateTicketInfo ticketInfo = new ITicketController.UpdateTicketInfo();
        UUID ticketId = UUID.randomUUID();
        ticketInfo.setId(ticketId);
        UUID roomId = UUID.randomUUID();
        ticketInfo.setRoomId(roomId);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setUsername("mocked_username");
        mockTicket.setRoomId(roomId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        when(mapper.map(ticketInfo, Ticket.class)).thenReturn(mockTicket);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(new Room()));

        assertThrows(NullPointerException.class, () -> ticketService.updateTicket(ticketInfo));
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

        Page<Ticket> expectedPage = new PageImpl<>(List.of(new Ticket(), new Ticket()));

        when(ticketRepository.filter(pageable, names, null, SecurityUtils.loginUsername(), roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, bookmark, keyword))
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
        List<String> siteList = new ArrayList<>();
        siteList.add("Site1");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c08");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("06eb43a7-6ea8-4744-8231-760559fe2c07");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("mocked_username");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Page<Ticket> expectedPage = new PageImpl<>(List.of(new Ticket(), new Ticket()));

        when(ticketRepository.filter(pageable, names, siteList, SecurityUtils.loginUsername(), roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, null, keyword))
            .thenReturn(expectedPage);

        Page<Ticket> filteredTickets = ticketService.filterAllBySite(pageable, names, username, roomId, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, createdBy, lastUpdatedBy, keyword);

        assertNotNull(expectedPage);
        assertEquals(2, expectedPage.getTotalElements());
    }

    @Test
    @DisplayName("Given Invalid Site ID, When Filtering Tickets, Then Throw HttpClientErrorException")
    public void givenInvalidSiteId_WhenFilteringTickets_ThenThrowHttpClientErrorException() {
        // Mock input parameters
        UUID ticketId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        Ticket ticketEntity = new Ticket();
        ticketEntity.setSiteId("site2"); // A different site ID

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCustomerTicketMapPk_TicketIdAndCustomerTicketMapPk_CustomerId(ticketId, customerId))
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
        assertThrows(NullPointerException.class, () -> {
            ticketService.findByQRCode(ticketId, customerId);
        });
    }

    @Test
    @DisplayName("Given UpdateStatusTicketOfCustomer Request, When Updating Status, Then Update Customer Ticket Map")
    public void givenUpdateStatusTicketOfCustomerRequest_WhenUpdatingStatus_ThenUpdateCustomerTicketMap() {
        // Mock input parameters
        ITicketController.UpdateStatusTicketOfCustomer updateStatusTicketOfCustomer = new ITicketController.UpdateStatusTicketOfCustomer();
        updateStatusTicketOfCustomer.setTicketId(UUID.randomUUID());
        updateStatusTicketOfCustomer.setCustomerId(UUID.randomUUID());
        updateStatusTicketOfCustomer.setStatus(Constants.StatusTicket.CHECK_IN);
        updateStatusTicketOfCustomer.setReasonId(UUID.randomUUID());
        updateStatusTicketOfCustomer.setReasonNote("ReasonNote");

        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        customerTicketMap.setCustomerTicketMapPk(new CustomerTicketMapPk(UUID.randomUUID(), UUID.randomUUID()));

        // Mock repository behavior
        Mockito.when(customerTicketMapRepository.findByCustomerTicketMapPk_TicketIdAndCustomerTicketMapPk_CustomerId(updateStatusTicketOfCustomer.getTicketId(), updateStatusTicketOfCustomer.getCustomerId()))
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
        ticketService.updateStatusTicketOfCustomer(updateStatusTicketOfCustomer);

        // Verify that the customerTicketMap has been updated with the new status, reasonId, and reasonNote
        assertEquals(updateStatusTicketOfCustomer.getStatus(), customerTicketMap.getStatus());
        assertEquals(updateStatusTicketOfCustomer.getReasonId(), customerTicketMap.getReasonId());
        assertEquals(updateStatusTicketOfCustomer.getReasonNote(), customerTicketMap.getReasonNote());

        // Verify that the customerTicketMapRepository.save() has been called
        verify(customerTicketMapRepository).save(customerTicketMap);

    }
}
