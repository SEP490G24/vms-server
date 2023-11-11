package fpt.edu.capstone.vms.component;

import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.util.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TicketSendEmailNotificationService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private CustomerTicketMapRepository customerTicketMapRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EmailUtils emailUtils;


    @Scheduled(cron = "0 0/5 * * * *") // Chạy hàng ngày
    public void SendEmailNotificationForCustomerWhenTicketAfterStart30Minutes() {
//        LocalDateTime currentDateTime = LocalDateTime.now();
//        LocalDateTime meetingStartTime = currentDateTime.plusMinutes(30);
//
//        List<Ticket> upcomingMeetings = ticketRepository.findAllByStartTimeBetweenAndStatus(currentDateTime, meetingStartTime, Constants.StatusTicket.PENDING);
//
//        for (Ticket ticket : upcomingMeetings) {
//
//            List<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
//            if (!customerTicketMaps.isEmpty()) {
//                customerTicketMaps.forEach(o -> {
//                    Customer customer = o.getCustomerEntity();
//                    Map<String, String> parameterMap = Map.of("ten_nguoi_nhan", customer.getVisitorName());
//                    String replacedTemplate = emailUtils.replaceEmailParameters(ticket.getTemplate().getBody(), parameterMap);
//
//                    emailUtils.sendMailWithQRCode(customer.getEmail(), ticket.getTemplate().getSubject(), replacedTemplate, null, ticket.getSiteId());
//                });
//
//            }
//        }
    }

}
