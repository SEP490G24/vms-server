package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.service.impl.TicketServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class TicketController implements ITicketController {
    private final TicketServiceImpl ticketService;
    private final ModelMapper mapper;

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
    public ResponseEntity<?> createTicket(createTicketInfo ticketInfo) {
        var ticket = ticketService.save(mapper.map(ticketInfo, Ticket.class));
        return ResponseEntity.ok(ticket);
    }
}