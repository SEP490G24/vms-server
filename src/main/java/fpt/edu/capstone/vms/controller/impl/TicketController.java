package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ICardCheckInHistoryService;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.sse.checkIn.SseCheckInEmitterManager;
import fpt.edu.capstone.vms.persistence.service.sse.checkIn.SseCheckInSession;
import fpt.edu.capstone.vms.util.JacksonUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import fpt.edu.capstone.vms.util.SseUtils;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Log4j2
public class TicketController implements ITicketController {
    private final ITicketService ticketService;
    private final SseCheckInEmitterManager sseCheckInEmitterManager;
    private final CustomerTicketMapRepository customerTicketMapRepository;
    private final CustomerRepository customerRepository;
    private final ICardCheckInHistoryService cardCheckInHistoryService;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;

    public TicketController(ITicketService ticketService, SseCheckInEmitterManager sseCheckInEmitterManager, CustomerTicketMapRepository customerTicketMapRepository, CustomerRepository customerRepository, ICardCheckInHistoryService cardCheckInHistoryService, SiteRepository siteRepository, ModelMapper mapper) {
        this.ticketService = ticketService;
        this.sseCheckInEmitterManager = sseCheckInEmitterManager;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.customerRepository = customerRepository;
        this.cardCheckInHistoryService = cardCheckInHistoryService;
        this.siteRepository = siteRepository;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<?> delete(String id) {
        return ResponseEntity.ok(ticketService.deleteTicket(id));
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(ticketService.findAll());
    }

    @Override
    public ResponseEntity<?> create(CreateTicketInfo ticketInfo) {
        try {
            return ResponseEntity.ok((ticketService.create(ticketInfo)));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> updateBookmark(TicketBookmark ticketBookmark) {
        return ResponseEntity.ok(ticketService.updateBookMark(ticketBookmark));
    }

    @Override
    public ResponseEntity<?> cancelMeeting(CancelTicket cancelTicket) {
        return ResponseEntity.ok(ticketService.cancelTicket(cancelTicket));
    }

    @Override
    public ResponseEntity<?> updateMeeting(UpdateTicketInfo updateTicketInfo) {
        try {
            return ResponseEntity.ok((ticketService.updateTicket(updateTicketInfo)));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> filterAllBySites(TicketFilter filter, boolean isPageable, Pageable pageable) {
        if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
            var ticketEntity = ticketService.filterAllBySite(
                filter.getNames(),
                filter.getSites(),
                filter.getUsernames(),
                filter.getRoomId(),
                filter.getStatus(),
                filter.getPurpose(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getStartTimeStart(),
                filter.getStartTimeEnd(),
                filter.getEndTimeStart(),
                filter.getEndTimeEnd(),
                filter.getCreatedBy(),
                filter.getLastUpdatedBy(), filter.getKeyword());

            var ticketEntityPageable = ticketService.filterAllBySite(
                pageable,
                filter.getNames(),
                filter.getSites(),
                filter.getUsernames(),
                filter.getRoomId(),
                filter.getStatus(),
                filter.getPurpose(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getStartTimeStart(),
                filter.getStartTimeEnd(),
                filter.getEndTimeStart(),
                filter.getEndTimeEnd(),
                filter.getCreatedBy(),
                filter.getLastUpdatedBy(),
                filter.getKeyword());


            List<TicketFilterDTO> ticketFilterPageDTOS = mapper.map(ticketEntityPageable.getContent(), new TypeToken<List<TicketFilterDTO>>() {
            }.getType());
            ticketFilterPageDTOS.forEach(o -> {
                setCustomer(o);

            });
            List<TicketFilterDTO> ticketFilterDTOS = mapper.map(ticketEntity, new TypeToken<List<TicketFilterDTO>>() {
            }.getType());
            ticketFilterDTOS.forEach(o -> {
                setCustomer(o);

            });
            return isPageable ?
                ResponseEntity.ok(new PageImpl(ticketFilterPageDTOS, pageable, ticketEntityPageable.getTotalElements()))
                : ResponseEntity.ok(ticketFilterDTOS);
        } else {
            var ticketEntity = ticketService.filter(
                filter.getNames(),
                filter.getRoomId(),
                filter.getStatus(),
                filter.getPurpose(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getStartTimeStart(),
                filter.getStartTimeEnd(),
                filter.getEndTimeStart(),
                filter.getEndTimeEnd(),
                filter.getCreatedBy(),
                filter.getLastUpdatedBy(),
                filter.getBookmark(),
                filter.getKeyword());

            var ticketEntityPageable = ticketService.filter(
                pageable,
                filter.getNames(),
                filter.getRoomId(),
                filter.getStatus(),
                filter.getPurpose(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getStartTimeStart(),
                filter.getStartTimeEnd(),
                filter.getEndTimeStart(),
                filter.getEndTimeEnd(),
                filter.getCreatedBy(),
                filter.getLastUpdatedBy(),
                filter.getBookmark(),
                filter.getKeyword());


            List<TicketFilterDTO> ticketFilterPageDTOS = mapper.map(ticketEntityPageable.getContent(), new TypeToken<List<TicketFilterDTO>>() {
            }.getType());
            ticketFilterPageDTOS.forEach(o -> {
                setCustomer(o);

            });

            List<TicketFilterDTO> ticketFilterDTOS = mapper.map(ticketEntity, new TypeToken<List<TicketFilterDTO>>() {
            }.getType());
            ticketFilterDTOS.forEach(o -> {
                setCustomer(o);

            });

            return isPageable ?
                ResponseEntity.ok(new PageImpl(ticketFilterPageDTOS, pageable, ticketEntityPageable.getTotalElements()))
                : ResponseEntity.ok(ticketFilterDTOS);
        }
    }

    @Override
    public ResponseEntity<?> findByQRCode(String checkInCode) {
        return ResponseEntity.ok(ticketService.findByQRCode(checkInCode));
    }

    @Override
    public SseEmitter subscribeCheckIn(String siteId) {
        if (SecurityUtils.getOrgId() != null) {
            if (SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You don't have permission to access this site");
            }
        } else {
            siteId = SecurityUtils.getSiteId();
        }
        var key = SseCheckInSession.builder()
            .siteId(siteId)
            .username(SecurityUtils.loginUsername())
            .sessionId(UUID.randomUUID())
            .build();
        log.info("Call API /subscribeCheckIn with sse session: {}", JacksonUtils.toJson(key));
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        SseUtils.sendInitEvent(sseEmitter);
        sseCheckInEmitterManager.addSubscribeEmitter(key, sseEmitter);
        sseEmitter.onCompletion(() -> sseCheckInEmitterManager.removeEmitter(key));
        sseEmitter.onTimeout(() -> sseCheckInEmitterManager.removeEmitter(key));
        sseEmitter.onError((e) -> sseCheckInEmitterManager.removeEmitter(key));
        return sseEmitter;
    }

    @Override
    public ResponseEntity<?> checkIn(CheckInPayload checkInPayload) {
        // Perform the check-in process
        var ticketByQRCodeResponseDTO = ticketService.checkInCustomer(checkInPayload);
        // Return the emitter immediately to the client
        sseCheckInEmitterManager.broadcast(ticketByQRCodeResponseDTO.getSiteId(), ticketByQRCodeResponseDTO);
        return ResponseEntity.ok(ticketByQRCodeResponseDTO);
    }

    @Override
    public ResponseEntity<?> filterTicketAndCustomer(TicketFilter filter, Pageable pageable) {
        Page<CustomerTicketMap> customerTicketMaps = ticketService.filterTicketAndCustomer(
            pageable,
            filter.getSites(),
            filter.getNames(),
            filter.getRoomId(),
            filter.getStatus(),
            filter.getPurpose(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getStartTimeStart(),
            filter.getStartTimeEnd(),
            filter.getEndTimeStart(),
            filter.getEndTimeEnd(),
            filter.getCreatedBy(),
            filter.getLastUpdatedBy(),
            filter.getBookmark(),
            filter.getKeyword());
        List<ITicketController.TicketByQRCodeResponseDTO> ticketByQRCodeResponseDTOS = mapper.map(customerTicketMaps.getContent(), new TypeToken<List<ITicketController.TicketByQRCodeResponseDTO>>() {
        }.getType());
        return ResponseEntity.ok(new PageImpl(ticketByQRCodeResponseDTOS, pageable, customerTicketMaps.getTotalElements()));
    }

    @Override
    public ResponseEntity<?> filterTicketByRoom(TicketFilter filter) {
        TicketByRoomResponseDTO ticketByRoomResponseDTO = ticketService.filterTicketByRoom(
            filter.getNames(),
            filter.getSites(),
            filter.getUsernames(),
            filter.getRoomId(),
            filter.getStatus(),
            filter.getPurpose(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getStartTimeStart(),
            filter.getStartTimeEnd(),
            filter.getEndTimeStart(),
            filter.getEndTimeEnd(),
            filter.getCreatedBy(),
            filter.getLastUpdatedBy(), filter.getKeyword());
        List<ITicketController.TicketFilterDTO> ticketFilterDTOS = mapper.map(ticketByRoomResponseDTO.getTickets(), new TypeToken<List<ITicketController.TicketFilterDTO>>() {
        }.getType());
        ticketFilterDTOS.forEach(o -> {
            setCustomer(o);
        });

        TicketByRoomResponse ticketByRoomResponse = new TicketByRoomResponse();
        ticketByRoomResponse.setRooms(ticketByRoomResponseDTO.getRooms());
        ticketByRoomResponse.setTickets(ticketFilterDTOS);
        return ResponseEntity.ok(ticketByRoomResponse);
    }

    @Override
    public ResponseEntity<?> addCardToCustomerTicket(CustomerTicketCardDTO customerTicketCardDTO) {
        try {
            return ResponseEntity.ok(ticketService.addCardCustomerTicket(customerTicketCardDTO));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getAllCardHistoryOfCustomer(String checkInCode) {
        try {
            return ResponseEntity.ok(cardCheckInHistoryService.getAllCardHistoryOfCustomer(checkInCode));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> viewDetailTicket(UUID ticketId, String siteId) {
        try {
            if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
                TicketFilterDTO ticketFilterDTO = ticketService.findByTicketForAdmin(ticketId, siteId);
                setCustomer(ticketFilterDTO);
                return ResponseEntity.ok(ticketFilterDTO);
            } else {
                TicketFilterDTO ticketFilterDTO = ticketService.findByTicketForUser(ticketId);
                setCustomer(ticketFilterDTO);
                return ResponseEntity.ok(ticketFilterDTO);
            }
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    private void setCustomer(TicketFilterDTO ticketFilterDTO) {
        List<ICustomerController.CustomerInfo> customerInfos = new ArrayList<>();
        customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticketFilterDTO.getId()).forEach(a -> {
            customerInfos.add(mapper.map(customerRepository.findById(a.getCustomerTicketMapPk().getCustomerId()).orElse(null), ICustomerController.CustomerInfo.class));
        });
        ticketFilterDTO.setCustomers(customerInfos);
    }
}
