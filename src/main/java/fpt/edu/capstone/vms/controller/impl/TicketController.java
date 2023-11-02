package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class TicketController implements ITicketController {
    private final ITicketService ticketService;
    private final CustomerTicketMapRepository customerTicketMapRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper mapper;

    public TicketController(ITicketService ticketService, CustomerTicketMapRepository customerTicketMapRepository, CustomerRepository customerRepository, ModelMapper mapper) {
        this.ticketService = ticketService;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<Ticket> findById(UUID id) {
        return ResponseEntity.ok(ticketService.findById(id));
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
    public ResponseEntity<?> filter(TicketFilterUser filter, boolean isPageable, Pageable pageable) {
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
        setCustomer(ticketFilterPageDTOS);

        List<TicketFilterDTO> ticketFilterDTOS = mapper.map(ticketEntity, new TypeToken<List<TicketFilterDTO>>() {
        }.getType());
        setCustomer(ticketFilterDTOS);

        return isPageable ?
            ResponseEntity.ok(new PageImpl(ticketFilterPageDTOS, pageable, ticketFilterPageDTOS.size()))
            : ResponseEntity.ok(ticketFilterDTOS);
    }

    @Override
    public ResponseEntity<?> filterAllBySites(TicketFilterSite filter, boolean isPageable, Pageable pageable) {
        var ticketEntity = ticketService.filterAllBySite(
            filter.getNames(),
            filter.getUsername(),
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

        var ticketEntityPageable = ticketService.filterAllBySite(
            pageable,
            filter.getNames(),
            filter.getUsername(),
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
        setCustomer(ticketFilterPageDTOS);

        List<TicketFilterDTO> ticketFilterDTOS = mapper.map(ticketEntity, new TypeToken<List<TicketFilterDTO>>() {
        }.getType());
        setCustomer(ticketFilterDTOS);

        return isPageable ?
            ResponseEntity.ok(new PageImpl(ticketFilterPageDTOS, pageable, ticketFilterPageDTOS.size()))
            : ResponseEntity.ok(ticketFilterDTOS);
    }

    private void setCustomer(List<TicketFilterDTO> ticketFilterDTOS) {
        ticketFilterDTOS.forEach(o -> {
            List<ICustomerController.CustomerInfo> customerInfos = new ArrayList<>();
            customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(o.getId()).forEach(a -> {
                customerInfos.add(mapper.map(customerRepository.findById(a.getCustomerTicketMapPk().getCustomerId()).orElse(null), ICustomerController.CustomerInfo.class));
            });
            o.setCustomers(customerInfos);
        });
    }
}
