package fpt.edu.capstone.vms.persistence.service.impl;

import com.google.zxing.WriterException;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMapPk;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.QRcodeUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketServiceImpl extends GenericServiceImpl<Ticket, UUID> implements ITicketService {

    final ModelMapper mapper;
    final TicketRepository ticketRepository;
    final TemplateRepository templateRepository;
    final CustomerRepository customerRepository;
    final CustomerTicketMapRepository customerTicketMapRepository;

    private static String daysEarlier = "";
    private static int number = 0;

    public TicketServiceImpl(TicketRepository ticketRepository, CustomerRepository customerRepository,
                             TemplateRepository templateRepository, ModelMapper mapper, CustomerTicketMapRepository customerTicketMapRepository) {
        this.ticketRepository = ticketRepository;
        this.templateRepository = templateRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.init(ticketRepository);
    }


    /**
     * The function creates a ticket based on the provided ticket information, checks for room availability, and sets the
     * ticket status accordingly.
     *
     * @param ticketInfo An object of type ITicketController.CreateTicketInfo, which contains information about the ticket
     *                   being created.
     * @return The method is returning a Ticket object.
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, NullPointerException.class})
    public Ticket create(ITicketController.CreateTicketInfo ticketInfo) {

        //Tạo meeting
        var ticketDto = mapper.map(ticketInfo, Ticket.class);
        ticketDto.setCode(generateMeetingCode(ticketInfo.getPurpose()));

        if (StringUtils.isEmpty(ticketInfo.getRoomId().toString())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "roomId is empty");
        }

        if (isUserHaveTicketInTime(SecurityUtils.loginUsername(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User have meeting in this time");
        }

        //Check trùng phòng chưa
        if (isRoomBooked(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room have meeting in this time");
        }

        if (ticketInfo.isDraft() == true) {
            ticketDto.setStatus(Constants.StatusTicket.DRAFT);
            Ticket ticket = ticketRepository.save(ticketDto);
            setDataCustomer(ticketInfo, ticket);
            return ticket;
        } else {
            if (StringUtils.isEmpty(ticketDto.getPurpose().toString())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose is empty");
            }

            if (ticketDto.getPurpose().equals("OTHERS")) {
                if (StringUtils.isEmpty(ticketInfo.getPurposeNote())) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose other is empty");
                }
            }

            if (ticketInfo.getStartTime() == null || ticketInfo.getStartTime().toString().trim().isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Start time is empty");
            }

            if (ticketInfo.getEndTime() == null || ticketInfo.getEndTime().toString().trim().isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "End time is empty");
            }

            if (StringUtils.isEmpty(ticketInfo.getTemplateId().toString())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "TemplateId is empty");
            }

            ticketDto.setStatus(Constants.StatusTicket.PENDING);
            Ticket ticket = ticketRepository.save(ticketDto);

            Template template = templateRepository.findById(ticket.getTemplateId()).orElse(null);

            if (ObjectUtils.isEmpty(template)) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't not found ticket");
            }
            setDataCustomer(ticketInfo, ticket);
            var customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
            if (customerTicketMaps.isEmpty())
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
            sendQr(customerTicketMaps, ticket, template);
            return ticket;
        }

    }

    /**
     * The function `setDataCustomer` takes in ticket information and creates customer tickets based on the provided data.
     *
     * @param ticketInfo The ticketInfo parameter is an object of type ITicketController.CreateTicketInfo. It contains
     *                   information about the ticket being created, including the list of new customers and the list of old customers.
     * @param ticket     The "ticket" parameter is an instance of the Ticket class. It is used to create a customer ticket by
     *                   associating it with a customer.
     */
    private void setDataCustomer(ITicketController.CreateTicketInfo ticketInfo, Ticket ticket) {

        List<ICustomerController.NewCustomers> newCustomers = ticketInfo.getNewCustomers();
        List<String> oldCustomers = ticketInfo.getOldCustomers();

        if (oldCustomers == null && newCustomers.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
        }
        if (!newCustomers.isEmpty()) {
            for (ICustomerController.NewCustomers customerDto : newCustomers) {
                if (ObjectUtils.isEmpty(customerDto))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
                Customer customer = customerRepository.save(mapper.map(customerDto, Customer.class));
                createCustomerTicket(ticket, customer.getId());
            }
        }

        if (oldCustomers != null) {
            for (String oldCustomer : oldCustomers) {
                if (StringUtils.isEmpty(oldCustomer.trim()))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
                var customer = customerRepository.findById(UUID.fromString(oldCustomer)).orElse(null);
                if (ObjectUtils.isEmpty(customer))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
                createCustomerTicket(ticket, UUID.fromString(oldCustomer.trim()));
            }
        }

    }

    /**
     * The function updates a bookmark for a ticket if the ticket exists and belongs to the current user.
     *
     * @param ticketBookmark The ticketBookmark parameter is an object of type ITicketController.TicketBookmark. It
     *                       contains information related to a ticket bookmark, such as the ticket ID.
     * @return The method returns a Boolean value.
     */
    @Override
    public Boolean updateBookMark(ITicketController.TicketBookmark ticketBookmark) {
        if (ObjectUtils.isEmpty(ticketBookmark)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "payload is empty");
        }
        var ticket = ticketRepository.findById(UUID.fromString(ticketBookmark.getTicketId())).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Ticket is empty");
        }
        if (ticketRepository.existsByIdAndUsername(UUID.fromString(ticketBookmark.getTicketId()), SecurityUtils.loginUsername())) {
            ticketRepository.save(ticket.setBookmark(true));
            return true;
        }
        return false;
    }

    /**
     * The function deletes a ticket from the ticket repository if it exists and is associated with the current logged-in
     * user.
     *
     * @param ticketId The ticketId parameter is a String representing the unique identifier of the ticket that needs to be
     *                 deleted.
     * @return The method is returning a Boolean value. It returns true if the ticket is successfully deleted, and false
     * otherwise.
     */
    @Override
    @Transactional
    public Boolean deleteTicket(String ticketId) {
        var ticket = ticketRepository.findById(UUID.fromString(ticketId)).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Ticket is empty");
        }
        if (ticketRepository.existsByIdAndUsername(UUID.fromString(ticketId), SecurityUtils.loginUsername())) {
            ticketRepository.delete(ticket);
            return true;
        }
        return false;
    }


    /**
     * The function `sendQr` sends an email to each customer in the `customerTicketMap` list with a QR code generated from
     * the `meetingUrl` using the `EmailUtils` class.
     *
     * @param customerTicketMap customerTicketMap is a list of objects of type CustomerTicketMap. Each CustomerTicketMap
     *                          object represents a mapping between a customer and a ticket.
     * @param ticket            The "ticket" parameter is an object of type "Ticket". It represents a ticket that is associated with
     *                          the customer.
     * @param template          The `template` parameter is an object of type `Template` which contains the subject and body of an
     *                          email template.
     */
    private void sendQr(List<CustomerTicketMap> customerTicketMap, Ticket ticket, Template template) {
        customerTicketMap.forEach(o -> {
            String meetingUrl = "https://web-vms.azurewebsites.net/ticket/" + ticket.getId().toString() + "/customer/" + o.getCustomerTicketMapPk().getCustomerId().toString();

            var customer = customerRepository.findById(o.getCustomerTicketMapPk().getCustomerId()).orElse(null);
            if (ObjectUtils.isEmpty(customer))
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Customer is empty");

            // Tạo mã QR code
            try {
                byte[] qrCodeData = QRcodeUtils.getQRCodeImage(meetingUrl, 400, 400);
                assert template != null;
//                EmailUtils.sendMailWithQRCode(customer.getEmail(), template.getSubject(), template.getBody(), qrCodeData);
            } catch (WriterException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });

    }

    /**
     * The function creates a customer ticket by saving the ticket and customer ID in a customer ticket map.
     *
     * @param ticket     The "ticket" parameter is an object of the Ticket class. It represents a ticket that needs to be
     *                   associated with a customer.
     * @param customerId The `customerId` parameter is a unique identifier for a customer. It is of type `UUID`, which
     *                   stands for Universally Unique Identifier. This identifier is used to associate the customer with the ticket being
     *                   created.
     */
    private void createCustomerTicket(Ticket ticket, UUID customerId) {
        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        CustomerTicketMapPk pk = new CustomerTicketMapPk();
        pk.setTicketId(ticket.getId());
        pk.setCustomerId(customerId);
        customerTicketMap.setCustomerTicketMapPk(pk);
        customerTicketMapRepository.save(customerTicketMap);
    }

    /**
     * The function checks if a room is booked during a specified time period.
     *
     * @param roomId    The UUID of the room for which you want to check if it is booked or not.
     * @param startTime The start time of the booking. It is of type LocalDateTime, which represents a date and time
     *                  without a time zone.
     * @param endTime   The endTime parameter represents the end time of the booking. It is of type LocalDateTime, which is a
     *                  class in Java that represents a date and time without a time zone.
     * @return The method is returning a boolean value.
     */
    private boolean isRoomBooked(UUID roomId, LocalDateTime startTime, LocalDateTime endTime) {
        int count = ticketRepository.countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqual(roomId, endTime, startTime);
        return count > 0;
    }

    private boolean isUserHaveTicketInTime(String username, LocalDateTime startTime, LocalDateTime endTime) {
        int count = ticketRepository.countByUsernameAndEndTimeGreaterThanEqualAndStartTimeLessThanEqual(username, endTime, startTime);
        return count > 0;
    }

    /**
     * The function generates a meeting code based on the purpose and current date.
     *
     * @param purpose The purpose parameter is of type Constants.Purpose, which is an enum that represents the purpose of
     *                the meeting. The possible values for purpose are CONFERENCES, INTERVIEW, MEETING, OTHERS, and WORKING.
     * @return The method is returning a String value.
     */
    private static String generateMeetingCode(Constants.Purpose purpose) {
        String per = "";
        switch (purpose) {
            case CONFERENCES -> per = "C";
            case INTERVIEW -> per = "I";
            case MEETING -> per = "M";
            case OTHERS -> per = "O";
            case WORKING -> per = "W";
            default -> per = "T";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String dateCreated = dateFormat.format(new Date());
        if (!dateCreated.equals(daysEarlier)) {
            number = 0;
            daysEarlier = dateCreated;
        }
        number++;
        return per + dateCreated + String.format("%04d", number);
    }
}
