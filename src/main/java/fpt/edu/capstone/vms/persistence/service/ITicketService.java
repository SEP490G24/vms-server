package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.UUID;


public interface ITicketService extends IGenericService<Ticket, UUID> {
    Ticket create(ITicketController.CreateTicketInfo ticketInfo);

    Boolean updateBookMark(ITicketController.TicketBookmark ticketBookmark);

    Boolean deleteTicket(String ticketId);
}
