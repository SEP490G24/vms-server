package fpt.edu.capstone.vms.component;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.util.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class TicketSendEmailNotificationService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerTicketMapRepository customerTicketMapRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EmailUtils emailUtils;


    @Scheduled(cron = "0 0/1 * * * *") // Chạy hàng ngày
    public void SendEmailNotificationForCustomerWhenTicketAfterStart30Minutes() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime downStartTime = currentDateTime.plusMinutes(59);
        LocalDateTime upStartTime = currentDateTime.plusMinutes(60);

        List<Ticket> upcomingMeetings = ticketRepository.findAllByStartTimeLessThanEqualAndStartTimeGreaterThanAndStatus(upStartTime, downStartTime, Constants.StatusTicket.PENDING);

        for (Ticket ticket : upcomingMeetings) {

            List<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
            if (!customerTicketMaps.isEmpty()) {
                customerTicketMaps.forEach(o -> {
                    Customer customer = o.getCustomerEntity();
                    log.info("Send notification to ticket: " + o.getId().getTicketId());
                    log.info("Send notification to customer: " + customer.getEmail());
                    var template = templateRepository.findById(UUID.fromString("ada93bc0-a6f5-4376-b5f9-2d311c892585")).orElse(null);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    Site site = siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null);
                    Room room = roomRepository.findById(ticket.getRoomId()).orElse(null);
                    String date = ticket.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String startTime = ticket.getStartTime().format(formatter);
                    String endTime = ticket.getEndTime().format(formatter);
                    User user = userRepository.findFirstByUsername(ticket.getUsername());

                    Map<String, String> parameterMap = new HashMap<>();
                    parameterMap.put("customerName", customer.getVisitorName());
                    parameterMap.put("meetingName", ticket.getName());
                    parameterMap.put("dateTime", date);
                    parameterMap.put("startTime", startTime);
                    parameterMap.put("endTime", endTime);
                    parameterMap.put("address", site.getAddress());
                    String roomName = room != null ? room.getName() : "Updating....";
                    parameterMap.put("roomName", roomName);
                    parameterMap.put("staffName", user.getFirstName() + " " + user.getLastName());
                    parameterMap.put("staffPhone", user.getPhoneNumber());
                    parameterMap.put("staffEmail", user.getEmail());
                    parameterMap.put("checkInCode", o.getCheckInCode());
                    String replacedTemplate = emailUtils.replaceEmailParameters(template.getBody(), parameterMap);

                    emailUtils.sendMailWithQRCode(customer.getEmail(), template.getSubject(), replacedTemplate, null, ticket.getSiteId());
                });

            }
        }
    }

}
