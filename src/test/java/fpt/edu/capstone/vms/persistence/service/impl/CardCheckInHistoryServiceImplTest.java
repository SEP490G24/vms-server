package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.CardCheckInHistory;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMapPk;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CardCheckInHistoryRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardCheckInHistoryServiceImplTest {

    @Mock
    private CardCheckInHistoryRepository cardCheckInHistoryRepository;

    @Mock
    private CustomerTicketMapRepository customerTicketMapRepository;

    @InjectMocks
    private CardCheckInHistoryServiceImpl cardCheckInHistoryService;

    @Mock
    SecurityContext securityContext;
    @Mock
    Authentication authentication;
    @Mock
    SiteRepository siteRepository;

    @Mock
    TicketRepository ticketRepository;

    @Mock
    MessageSource messageSource;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testCheckCard() {
        // Arrange
        ICardController.CardCheckDTO cardCheckDTO = new ICardController.CardCheckDTO();
        cardCheckDTO.setCardId("sampleCardId");
        cardCheckDTO.setMacIp("sampleMacIp");

        CardCheckInHistory cardCheckInHistoryMock = new CardCheckInHistory();
        CustomerTicketMap customerTicketMapMock = new CustomerTicketMap();
        CustomerTicketMapPk pk = new CustomerTicketMapPk();
        pk.setTicketId(UUID.randomUUID());
        pk.setCustomerId(UUID.randomUUID());
        customerTicketMapMock.setCustomerTicketMapPk(pk);
        customerTicketMapMock.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        customerTicketMapMock.setCheckInTime(java.time.LocalDateTime.now().minusDays(1));
        customerTicketMapMock.setCheckInCode("sampleCheckInCode");
        cardCheckInHistoryMock.setCheckInCode(customerTicketMapMock.getCheckInCode());
        when(customerTicketMapRepository.findByCardId(cardCheckDTO.getCardId())).thenReturn(customerTicketMapMock);

        when(cardCheckInHistoryRepository.save(any(CardCheckInHistory.class))).thenReturn(cardCheckInHistoryMock);

        Ticket ticketMock = new Ticket();
        Room roomMock = new Room();
        ticketMock.setRoom(roomMock);
        when(ticketRepository.findById(pk.getTicketId())).thenReturn(java.util.Optional.of(ticketMock));
        // Act
        boolean result = cardCheckInHistoryService.checkCard(cardCheckDTO);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCheckCard_CustomerTicketNull() {
        // Arrange
        ICardController.CardCheckDTO cardCheckDTO = new ICardController.CardCheckDTO();
        cardCheckDTO.setCardId("sampleCardId");
        cardCheckDTO.setMacIp("sampleMacIp");

        CardCheckInHistory cardCheckInHistoryMock = new CardCheckInHistory();
        CustomerTicketMap customerTicketMapMock = new CustomerTicketMap();
        CustomerTicketMapPk pk = new CustomerTicketMapPk();
        pk.setTicketId(UUID.randomUUID());
        pk.setCustomerId(UUID.randomUUID());
        customerTicketMapMock.setCustomerTicketMapPk(pk);
        customerTicketMapMock.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        customerTicketMapMock.setCheckInTime(java.time.LocalDateTime.now().minusDays(1));
        customerTicketMapMock.setCheckInCode("sampleCheckInCode");
        cardCheckInHistoryMock.setCheckInCode(customerTicketMapMock.getCheckInCode());
        when(customerTicketMapRepository.findByCardId(cardCheckDTO.getCardId())).thenReturn(null);
        // Act
        boolean result = cardCheckInHistoryService.checkCard(cardCheckDTO);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCheckCard_WithNullCard() {
        // Arrange
        ICardController.CardCheckDTO cardCheckDTO = null;
        // Act
        boolean result = cardCheckInHistoryService.checkCardCheckInHistory(cardCheckDTO);
        // Assert
        assertFalse(result);
    }

    @Test
    void testCheckCard_CheckInTimeIsAfterNow() {
        // Arrange
        ICardController.CardCheckDTO cardCheckDTO = new ICardController.CardCheckDTO();
        cardCheckDTO.setCardId("sampleCardId");
        cardCheckDTO.setMacIp("sampleMacIp");

        CardCheckInHistory cardCheckInHistoryMock = new CardCheckInHistory();
        CustomerTicketMap customerTicketMapMock = new CustomerTicketMap();
        CustomerTicketMapPk pk = new CustomerTicketMapPk();
        pk.setTicketId(UUID.randomUUID());
        pk.setCustomerId(UUID.randomUUID());
        customerTicketMapMock.setCustomerTicketMapPk(pk);
        customerTicketMapMock.setStatus(Constants.StatusCustomerTicket.CHECK_IN);
        customerTicketMapMock.setCheckInTime(java.time.LocalDateTime.now().plusDays(1));
        customerTicketMapMock.setCheckInCode("sampleCheckInCode");
        cardCheckInHistoryMock.setCheckInCode(customerTicketMapMock.getCheckInCode());
        when(customerTicketMapRepository.findByCardId(cardCheckDTO.getCardId())).thenReturn(customerTicketMapMock);

        when(cardCheckInHistoryRepository.save(any(CardCheckInHistory.class))).thenReturn(cardCheckInHistoryMock);

        Ticket ticketMock = new Ticket();
        Room roomMock = new Room();
        ticketMock.setRoom(roomMock);
        when(ticketRepository.findById(pk.getTicketId())).thenReturn(java.util.Optional.of(ticketMock));
        // Act
        boolean result = cardCheckInHistoryService.checkCard(cardCheckDTO);

        // Assert
        assertFalse(result);

        // Verify that cardCheckInHistoryRepository.save was called with the correct parameters
        verify(cardCheckInHistoryRepository).save(argThat(cardCheckInHistory ->
            cardCheckInHistory.getCheckInCode().equals(customerTicketMapMock.getCheckInCode()) &&
                cardCheckInHistory.getMacIp().equals(cardCheckDTO.getMacIp()) &&
                cardCheckInHistory.getStatus().equals(Constants.StatusCheckInCard.DENIED)
        ));
    }

    @Test
    void testCheckCard_CheckOutTimeIsBeforeNow() {
        // Arrange
        ICardController.CardCheckDTO cardCheckDTO = new ICardController.CardCheckDTO();
        cardCheckDTO.setCardId("sampleCardId");
        cardCheckDTO.setMacIp("sampleMacIp");

        CardCheckInHistory cardCheckInHistoryMock = new CardCheckInHistory();
        CustomerTicketMap customerTicketMapMock = new CustomerTicketMap();
        CustomerTicketMapPk pk = new CustomerTicketMapPk();
        pk.setTicketId(UUID.randomUUID());
        pk.setCustomerId(UUID.randomUUID());
        customerTicketMapMock.setCustomerTicketMapPk(pk);
        customerTicketMapMock.setStatus(Constants.StatusCustomerTicket.CHECK_OUT);
        customerTicketMapMock.setCheckOutTime(java.time.LocalDateTime.now().minusDays(1));
        customerTicketMapMock.setCheckInCode("sampleCheckInCode");
        cardCheckInHistoryMock.setCheckInCode(customerTicketMapMock.getCheckInCode());
        when(customerTicketMapRepository.findByCardId(cardCheckDTO.getCardId())).thenReturn(customerTicketMapMock);

        when(cardCheckInHistoryRepository.save(any(CardCheckInHistory.class))).thenReturn(cardCheckInHistoryMock);

        Ticket ticketMock = new Ticket();
        Room roomMock = new Room();
        ticketMock.setRoom(roomMock);
        when(ticketRepository.findById(pk.getTicketId())).thenReturn(java.util.Optional.of(ticketMock));
        // Act
        boolean result = cardCheckInHistoryService.checkCard(cardCheckDTO);

        // Assert
        assertFalse(result);

        // Verify that cardCheckInHistoryRepository.save was called with the correct parameters
        verify(cardCheckInHistoryRepository).save(argThat(cardCheckInHistory ->
            cardCheckInHistory.getCheckInCode().equals(customerTicketMapMock.getCheckInCode()) &&
                cardCheckInHistory.getMacIp().equals(cardCheckDTO.getMacIp()) &&
                cardCheckInHistory.getStatus().equals(Constants.StatusCheckInCard.DENIED)
        ));
    }

    @Test
    void testCheckCard_NotEqualMacIp() {
        // Arrange
        ICardController.CardCheckDTO cardCheckDTO = new ICardController.CardCheckDTO();
        cardCheckDTO.setCardId("sampleCardId");
        cardCheckDTO.setMacIp("sampleMacIp");

        CardCheckInHistory cardCheckInHistoryMock = new CardCheckInHistory();
        CustomerTicketMap customerTicketMapMock = new CustomerTicketMap();
        CustomerTicketMapPk pk = new CustomerTicketMapPk();
        pk.setTicketId(UUID.randomUUID());
        pk.setCustomerId(UUID.randomUUID());
        customerTicketMapMock.setCustomerTicketMapPk(pk);
        customerTicketMapMock.setStatus(Constants.StatusCustomerTicket.CHECK_OUT);
        customerTicketMapMock.setCheckOutTime(java.time.LocalDateTime.now().minusDays(1));
        customerTicketMapMock.setCheckInCode("sampleCheckInCode");
        cardCheckInHistoryMock.setCheckInCode(customerTicketMapMock.getCheckInCode());
        when(customerTicketMapRepository.findByCardId(cardCheckDTO.getCardId())).thenReturn(customerTicketMapMock);

        when(cardCheckInHistoryRepository.save(any(CardCheckInHistory.class))).thenReturn(cardCheckInHistoryMock);

        Ticket ticketMock = new Ticket();
        Room roomMock = new Room();
        ticketMock.setRoom(roomMock);
        when(ticketRepository.findById(pk.getTicketId())).thenReturn(java.util.Optional.of(ticketMock));
        // Act
        boolean result = cardCheckInHistoryService.checkCard(cardCheckDTO);

        // Assert
        assertFalse(result);

        // Verify that cardCheckInHistoryRepository.save was called with the correct parameters
        verify(cardCheckInHistoryRepository).save(argThat(cardCheckInHistory ->
            cardCheckInHistory.getCheckInCode().equals(customerTicketMapMock.getCheckInCode()) &&
                cardCheckInHistory.getMacIp().equals(cardCheckDTO.getMacIp()) &&
                cardCheckInHistory.getStatus().equals(Constants.StatusCheckInCard.DENIED)
        ));
    }

    @Test
    void testGetAllCardHistoryOfCustomerWithNotPermission() {
        Pageable pageableSort = Pageable.unpaged();

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Arrange
        String checkInCode = "sampleCheckInCode";

        Ticket ticket = new Ticket();
        ticket.setSiteId("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        CustomerTicketMap customerTicketMapMock = new CustomerTicketMap();
        customerTicketMapMock.setTicketEntity(ticket);
        when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInCode)).thenReturn(customerTicketMapMock);

        // Mock the behavior of SecurityUtils.checkSiteAuthorization
        when(SecurityUtils.checkSiteAuthorization(siteRepository, customerTicketMapMock.getTicketEntity().getSiteId())).thenReturn(false);

        // Act and Assert
        assertThrows(CustomException.class, () -> {
            cardCheckInHistoryService.getAllCardHistoryOfCustomer(pageableSort, checkInCode);
        });

        // Verify that customerTicketMapRepository.findByCheckInCodeIgnoreCase was called with the correct parameters
        verify(customerTicketMapRepository).findByCheckInCodeIgnoreCase(checkInCode);

    }

    @Test
    void testGetAllCardHistoryOfCustomerWithPermission() {
        Pageable pageableSort = Pageable.unpaged();

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Arrange
        String checkInCode = "sampleCheckInCode";

        Ticket ticket = new Ticket();
        ticket.setSiteId("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        CustomerTicketMap customerTicketMapMock = new CustomerTicketMap();
        customerTicketMapMock.setTicketEntity(ticket);

        when(customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInCode)).thenReturn(customerTicketMapMock);

        // Mock the behavior of SecurityUtils.checkSiteAuthorization
        when(SecurityUtils.checkSiteAuthorization(siteRepository, customerTicketMapMock.getTicketEntity().getSiteId())).thenReturn(true);

        Page<ITicketController.CardCheckInHistoryDTO> expectedCardHistoryList = new PageImpl<>(List.of());
        when(cardCheckInHistoryRepository.getAllCardHistoryOfCustomer(pageableSort, checkInCode)).thenReturn(expectedCardHistoryList);

        // Act
        Page<ITicketController.CardCheckInHistoryDTO> result = cardCheckInHistoryService.getAllCardHistoryOfCustomer(pageableSort, checkInCode);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCardHistoryList, result);

        // Verify that customerTicketMapRepository.findByCheckInCodeIgnoreCase was called with the correct parameters
        verify(customerTicketMapRepository).findByCheckInCodeIgnoreCase(checkInCode);

        // Verify that cardCheckInHistoryRepository.getAllCardHistoryOfCustomer was called with the correct parameters
        verify(cardCheckInHistoryRepository).getAllCardHistoryOfCustomer(pageableSort, checkInCode);
    }
}
