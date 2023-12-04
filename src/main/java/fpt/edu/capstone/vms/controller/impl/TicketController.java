package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ICardCheckInHistoryService;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.sse.checkIn.SseCheckInEmitterManager;
import fpt.edu.capstone.vms.persistence.service.sse.checkIn.SseCheckInSession;
import fpt.edu.capstone.vms.util.JacksonUtils;
import fpt.edu.capstone.vms.util.ResponseUtils;
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
        try {
            if (SecurityUtils.checkSiteAuthorization(siteRepository, ticketService.findById(UUID.fromString(id)).getSiteId())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
            return ResponseUtils.getResponseEntityStatus((ticketService.deleteTicket(id)));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> create(CreateTicketInfo ticketInfo) {
        try {
            return ResponseUtils.getResponseEntityStatus((ticketService.create(ticketInfo)));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateBookmark(TicketBookmark ticketBookmark) {
        try {
            return ResponseUtils.getResponseEntityStatus(ticketService.updateBookMark(ticketBookmark));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> filterAllTicketBookmarkForUser(Pageable pageable) {
        var ticketEntity = ticketService.filter(
            pageable,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true,
            null);
        List<TicketFilterDTO> ticketFilterDTOS = mapper.map(ticketEntity.getContent(), new TypeToken<List<TicketFilterDTO>>() {
        }.getType());
        ticketFilterDTOS.forEach(o -> {
            setCustomer(o);

        });
        return ResponseEntity.ok(new PageImpl(ticketFilterDTOS, pageable, ticketEntity.getTotalElements()));
    }

    @Override
    public ResponseEntity<?> cancelMeeting(CancelTicket cancelTicket) {
        try {
            return ResponseUtils.getResponseEntityStatus(ticketService.cancelTicket(cancelTicket));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateMeeting(UpdateTicketInfo updateTicketInfo) {
        try {
            return ResponseUtils.getResponseEntityStatus((ticketService.updateTicket(updateTicketInfo)));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> filterAllTicket(TicketFilter filter, boolean isPageable, Pageable pageable) {
        if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
            var ticketEntity = ticketService.filterAllBySite(
                filter.getNames(),
                filter.getSiteId(),
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
                filter.getSiteId(),
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
        try {
            return ResponseUtils.getResponseEntityStatus(ticketService.findByQRCode(checkInCode));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SseEmitter subscribeCheckIn() {
        var key = SseCheckInSession.builder()
            .siteId(SecurityUtils.getSiteId())
            .organizationId(SecurityUtils.getOrgId())
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
    public ResponseEntity<?> updateStatusCustomerOfTicket(CheckInPayload checkInPayload) {
        try {
            // Perform the check-in process
            var ticketByQRCodeResponseDTO = ticketService.updateStatusCustomerOfTicket(checkInPayload);
            // Return the emitter immediately to the client
            sseCheckInEmitterManager.broadcast(ticketByQRCodeResponseDTO.getSiteId(), ticketByQRCodeResponseDTO);
            return ResponseUtils.getResponseEntityStatus(ticketByQRCodeResponseDTO);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public ResponseEntity<?> filterTicketAndCustomer(TicketFilter filter, Pageable pageable) {
        Page<CustomerTicketMap> customerTicketMaps = ticketService.filterTicketAndCustomer(
            pageable,
            filter.getSiteId(),
            filter.getNames(),
            filter.getRoomId(),
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
            filter.getSiteId(),
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
            return ResponseUtils.getResponseEntityStatus(ticketService.addCardCustomerTicket(customerTicketCardDTO));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllCardHistoryOfCustomer(String checkInCode, Pageable pageable) {
        try {
            return ResponseUtils.getResponseEntityStatus(cardCheckInHistoryService.getAllCardHistoryOfCustomer(pageable, checkInCode));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> viewDetailTicket(UUID ticketId) {
        try {
            TicketFilterDTO ticketFilterDTO = ticketService.findByTicket(ticketId);
            setCustomer(ticketFilterDTO);
            return ResponseUtils.getResponseEntityStatus(ticketFilterDTO);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
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
