package fpt.edu.capstone.vms.component;

import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TicketCleanupService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private CustomerTicketMapRepository customerTicketMapRepository;
    @Autowired
    private CustomerRepository customerRepository;


    @Scheduled(cron = "0 0/5 * * * *") // Chạy hàng ngày
    public void cleanupDraftTickets() {
//        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
//
//        List<Ticket> draftTickets = ticketRepository.findAllByCreatedOnBeforeAndStatus(thirtyDaysAgo, Constants.StatusTicket.DRAFT);
//
//        for (Ticket ticket : draftTickets) {
//            customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId()).forEach(o -> {
//
//                customerTicketMapRepository.deleteById(o.getCustomerTicketMapPk());
//            });
//            ticketRepository.delete(ticket);
//
//        }
    }

}
