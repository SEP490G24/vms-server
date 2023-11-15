package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.sse.SseEmitterManager;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public class TicketController implements ITicketController {
    private final ITicketService ticketService;
    private final SseEmitterManager sseEmitterManager;
    private final CustomerTicketMapRepository customerTicketMapRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper mapper;

    public TicketController(ITicketService ticketService, SseEmitterManager sseEmitterManager, CustomerTicketMapRepository customerTicketMapRepository, CustomerRepository customerRepository, ModelMapper mapper) {
        this.ticketService = ticketService;
        this.sseEmitterManager = sseEmitterManager;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.customerRepository = customerRepository;
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
                ResponseEntity.ok(new PageImpl(ticketFilterPageDTOS, pageable, ticketFilterPageDTOS.size()))
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
                ResponseEntity.ok(new PageImpl(ticketFilterPageDTOS, pageable, ticketFilterPageDTOS.size()))
                : ResponseEntity.ok(ticketFilterDTOS);
        }
    }

    @Override
    public ResponseEntity<?> findByQRCode(String checkInCode) {
        return ResponseEntity.ok(ticketService.findByQRCode(checkInCode));
    }

    @Override
    public ResponseEntity<?> checkIn(CheckInPayload checkInPayload) {
        // Create a new emitter for the client
        SseEmitter emitter = new SseEmitter();

        // Add the emitter to the manager
        sseEmitterManager.addEmitter(checkInPayload, emitter);

        // Set up completion and timeout handlers
        emitter.onCompletion(() -> sseEmitterManager.removeEmitter(checkInPayload, emitter));
        emitter.onTimeout(() -> sseEmitterManager.removeEmitter(checkInPayload, emitter));

        // Start a new thread to handle the check-in process
        CompletableFuture.runAsync(() -> {
            try {
                // Perform the check-in process
                ticketService.checkInCustomer(checkInPayload);
            } catch (Exception e) {
                // Handle exceptions if needed
                e.printStackTrace();
            } finally {
                // Complete the emitter (close the connection)
                emitter.complete();
            }
        });

        // Return the emitter immediately to the client
        return ResponseEntity.ok(emitter);
    }

    @Override
    public ResponseEntity<?> filterTicketAndCustomer(TicketFilterUser filter, Pageable pageable) {
        return ResponseEntity.ok(ticketService.filterTicketAndCustomer(
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
            filter.getKeyword()));

    }

    @Override
    public ResponseEntity<?> filterTicketByRoom(TicketFilter filter) {
        return ResponseEntity.ok(ticketService.filterTicketByRoom(
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
            filter.getLastUpdatedBy(), filter.getKeyword()));
    }

    @Override
    public ResponseEntity<?> findByIdForUser(UUID ticketId) {
        try {
            TicketFilterDTO ticketFilterDTO = ticketService.findByTicketForUser(ticketId);
            setCustomer(ticketFilterDTO);
            return ResponseEntity.ok(ticketFilterDTO);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> findByIdForAdmin(UUID ticketId, String siteId) {
        try {
            TicketFilterDTO ticketFilterDTO = ticketService.findByTicketForAdmin(ticketId, siteId);
            setCustomer(ticketFilterDTO);
            return ResponseEntity.ok(ticketFilterDTO);
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
