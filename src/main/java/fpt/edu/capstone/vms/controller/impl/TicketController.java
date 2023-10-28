package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Ticket> delete(UUID id) {
        return ticketService.delete(id);
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(ticketService.findAll());
    }

    @Override
    public ResponseEntity<?> create(CreateTicketInfo ticketInfo) {
        return ResponseEntity.ok((ticketService.create(ticketInfo)));
    }


}
