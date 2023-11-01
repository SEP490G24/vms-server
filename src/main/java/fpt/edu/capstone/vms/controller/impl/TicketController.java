package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
public class TicketController implements ITicketController {
    private final ITicketService ticketService;

    public TicketController(ITicketService ticketService) {
        this.ticketService = ticketService;
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
    public ResponseEntity<?> cancelMeeting(String ticketId) {
        return ResponseEntity.ok(ticketService.cancelTicket(ticketId));
    }

}
