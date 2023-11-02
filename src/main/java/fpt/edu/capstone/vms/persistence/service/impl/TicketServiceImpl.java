package fpt.edu.capstone.vms.persistence.service.impl;

import com.google.zxing.WriterException;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMapPk;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.EmailUtils;
import fpt.edu.capstone.vms.util.QRcodeUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import fpt.edu.capstone.vms.util.Utils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketServiceImpl extends GenericServiceImpl<Ticket, UUID> implements ITicketService {

    final ModelMapper mapper;
    final TicketRepository ticketRepository;
    final RoomRepository roomRepository;
    final TemplateRepository templateRepository;
    final CustomerRepository customerRepository;
    final SiteRepository siteRepository;
    final OrganizationRepository organizationRepository;
    final CustomerTicketMapRepository customerTicketMapRepository;
    final EmailUtils emailUtils;


    public TicketServiceImpl(TicketRepository ticketRepository, CustomerRepository customerRepository,
                             TemplateRepository templateRepository, ModelMapper mapper, RoomRepository roomRepository, SiteRepository siteRepository, OrganizationRepository organizationRepository, CustomerTicketMapRepository customerTicketMapRepository, EmailUtils emailUtils) {
        this.ticketRepository = ticketRepository;
        this.templateRepository = templateRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.roomRepository = roomRepository;
        this.siteRepository = siteRepository;
        this.organizationRepository = organizationRepository;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.emailUtils = emailUtils;
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

        String username = SecurityUtils.loginUsername();
        //Tạo meeting
        var ticketDto = mapper.map(ticketInfo, Ticket.class);
        ticketDto.setCode(generateMeetingCode(ticketInfo.getPurpose(), username));

        LocalDateTime startTime = ticketInfo.getStartTime();
        LocalDateTime endTime = ticketInfo.getEndTime();

        if (startTime.isAfter(endTime)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Time is not true");
        }

        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes(); // Chuyển thời gian thành phút

        if (minutes < 15) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Time meeting must greater than 15 minutes");
        }

        if (SecurityUtils.getOrgId() != null) {
            if (StringUtils.isEmpty(ticketInfo.getSiteId().trim()))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SiteId is null");
            if (siteRepository.existsByIdAndOrganizationId(UUID.fromString(ticketInfo.getSiteId()), UUID.fromString(SecurityUtils.getOrgId())))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SiteId is not in organization");
            ticketDto.setSiteId(ticketInfo.getSiteId());
        } else {
            ticketDto.setSiteId(SecurityUtils.getSiteId());

        }

        Room room = roomRepository.findById(ticketInfo.getRoomId()).orElse(null);

        if (!room.getSiteId().equals(ticketDto.getSiteId()))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User can not create meeting in this room");

        if (ObjectUtils.isEmpty(room)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room is null");
        }

        if (isRoomBooked(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room have meeting in this time");
        }


        ticketDto.setUsername(username);

        if (ticketInfo.isDraft() == true) {
            ticketDto.setStatus(Constants.StatusTicket.DRAFT);
            Ticket ticket = ticketRepository.save(ticketDto);
            setDataCustomer(ticketInfo, ticket);
            return ticket;
        } else {

            if (StringUtils.isEmpty(ticketInfo.getTemplateId().toString())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "TemplateId is empty");
            }

            Template template = templateRepository.findById(ticketDto.getTemplateId()).orElse(null);

            if (ObjectUtils.isEmpty(template)) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't not found ticket");
            }

            if (!template.getSiteId().equals(ticketDto.getSiteId()))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User can not create meeting in this template");

            if (isUserHaveTicketInTime(SecurityUtils.loginUsername(), startTime, endTime)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User have meeting in this time");
            }

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

            ticketDto.setStatus(Constants.StatusTicket.PENDING);
            Ticket ticket = ticketRepository.save(ticketDto);

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

        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "site is flase");
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }
        if (newCustomers != null) {
            for (ICustomerController.NewCustomers customerDto : newCustomers) {
                if (ObjectUtils.isEmpty(customerDto))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
                if (!Utils.isCCCDValid(customerDto.getIdentificationNumber())) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "IdentificationNumber is incorrect");
                }
                Customer customerExist = customerRepository.findByIdentificationNumberAndOrganizationId(customerDto.getIdentificationNumber(), orgId);
                if (ObjectUtils.isEmpty(customerExist)) {
                    Customer customer = customerRepository.save(mapper.map(customerDto, Customer.class).setOrganizationId(orgId));
                    createCustomerTicket(ticket, customer.getId());
                } else {
                    createCustomerTicket(ticket, customerExist.getId());
                }
            }
        }

        if (oldCustomers != null) {
            for (String oldCustomer : oldCustomers) {
                if (StringUtils.isEmpty(oldCustomer.trim()))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
                if (!customerRepository.existsByIdAndAndOrganizationId(UUID.fromString(oldCustomer), orgId))
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
     * The function cancels a ticket by updating its status to "CANCEL" if the ticket exists and belongs to the currently
     * logged in user.
     *
     * @param ticketId The ticketId parameter is a String that represents the unique identifier of the ticket that needs to
     *                 be canceled.
     * @return The method is returning a Boolean value.
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, NullPointerException.class})
    public Boolean cancelTicket(String ticketId) {
        var ticket = ticketRepository.findById(UUID.fromString(ticketId)).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Ticket is empty");
        }

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = ticket.getStartTime();

        if (startTime.isAfter(currentTime.plusHours(2))) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Meetings cannot be canceled at least 2 hours before they start.");
        }


        if (ticketRepository.existsByIdAndUsername(UUID.fromString(ticketId), SecurityUtils.loginUsername())) {
            ticket.setStatus(Constants.StatusTicket.CANCEL);
            ticketRepository.save(ticket);
            List<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
            if (!customerTicketMaps.isEmpty()) {
                customerTicketMaps.forEach(o -> {
                    Customer customer = o.getCustomerEntity();
                    emailUtils.sendMailWithQRCode(customer.getEmail(), "Cancel Meeting", "Sorry to cancel meeting", null);
                });

            }
            return true;
        }
        return false;
    }

    @Override
    public Page<Ticket> filter(Pageable pageable
        , List<String> names
        , UUID roomId
        , Constants.StatusTicket status
        , Constants.Purpose purpose
        , LocalDateTime createdOnStart
        , LocalDateTime createdOnEnd
        , LocalDateTime startTimeStart
        , LocalDateTime startTimeEnd
        , LocalDateTime endTimeStart
        , LocalDateTime endTimeEnd
        , String createdBy
        , String lastUpdatedBy
        , Boolean bookmark
        , String keyword) {

        return ticketRepository.filter(pageable
            , names
            , null
            , SecurityUtils.loginUsername()
            , roomId
            , status
            , purpose
            , createdOnStart
            , createdOnEnd
            , startTimeStart
            , startTimeEnd
            , endTimeStart
            , endTimeEnd
            , createdBy
            , lastUpdatedBy
            , bookmark
            , keyword);
    }

    @Override
    public Page<Ticket> filterAllBySite(Pageable pageable
        , List<String> names, String username
        , UUID roomId, Constants.StatusTicket status
        , Constants.Purpose purpose, LocalDateTime createdOnStart
        , LocalDateTime createdOnEnd, LocalDateTime startTimeStart
        , LocalDateTime startTimeEnd, LocalDateTime endTimeStart
        , LocalDateTime endTimeEnd, String createdBy
        , String lastUpdatedBy, String keyword) {
        return ticketRepository.filter(pageable
            , names
            , getListSite()
            , username
            , roomId
            , status
            , purpose
            , createdOnStart
            , createdOnEnd
            , startTimeStart
            , startTimeEnd
            , endTimeStart
            , endTimeEnd
            , createdBy
            , lastUpdatedBy
            , null
            , keyword);
    }

    @Override
    public List<Ticket> filter(List<String> names
        , UUID roomId
        , Constants.StatusTicket status
        , Constants.Purpose purpose
        , LocalDateTime createdOnStart
        , LocalDateTime createdOnEnd
        , LocalDateTime startTimeStart
        , LocalDateTime startTimeEnd
        , LocalDateTime endTimeStart
        , LocalDateTime endTimeEnd
        , String createdBy
        , String lastUpdatedBy
        , Boolean bookmark
        , String keyword) {
        return ticketRepository.filter(names
            , null
            , SecurityUtils.loginUsername()
            , roomId
            , status
            , purpose
            , createdOnStart
            , createdOnEnd
            , startTimeStart
            , startTimeEnd
            , endTimeStart
            , endTimeEnd
            , createdBy
            , lastUpdatedBy
            , bookmark
            , keyword);
    }

    @Override
    public List<Ticket> filterAllBySite(List<String> names
        , String username, UUID roomId
        , Constants.StatusTicket status
        , Constants.Purpose purpose
        , LocalDateTime createdOnStart
        , LocalDateTime createdOnEnd
        , LocalDateTime startTimeStart
        , LocalDateTime startTimeEnd
        , LocalDateTime endTimeStart
        , LocalDateTime endTimeEnd
        , String createdBy
        , String lastUpdatedBy
        , String keyword) {
        return ticketRepository.filter(names
            , getListSite()
            , username
            , roomId
            , status
            , purpose
            , createdOnStart
            , createdOnEnd
            , startTimeStart
            , startTimeEnd
            , endTimeStart
            , endTimeEnd
            , createdBy
            , lastUpdatedBy
            , null
            , keyword);
    }

    private List<String> getListSite() {
        List<String> sites = new ArrayList<>();

        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "site is flase");
            }
            sites.add(SecurityUtils.getSiteId());
        } else {
            siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId())).forEach(o -> {
                sites.add(o.getId().toString());
            });
        }
        return sites;
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
                emailUtils.sendMailWithQRCode(customer.getEmail(), template.getSubject(), template.getBody(), qrCodeData);
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
        int count = ticketRepository.countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqual(roomId, startTime, endTime);
        return count > 0;
    }

    /**
     * The function checks if a user has a ticket within a specified time range.
     *
     * @param username  The username of the user for whom we want to check if they have a ticket in the given time range.
     * @param startTime The start time of the ticket validity period.
     * @param endTime   The endTime parameter represents the end time of a ticket.
     * @return The method is returning a boolean value.
     */
    private boolean isUserHaveTicketInTime(String username, LocalDateTime startTime, LocalDateTime endTime) {
        int count = ticketRepository.countByUsernameAndEndTimeGreaterThanEqualAndStartTimeLessThanEqual(username, startTime, endTime);
        return count > 0;
    }

    /**
     * The function generates a meeting code based on the purpose and current date.
     *
     * @param purpose The purpose parameter is of type Constants.Purpose, which is an enum that represents the purpose of
     *                the meeting. The possible values for purpose are CONFERENCES, INTERVIEW, MEETING, OTHERS, and WORKING.
     * @return The method is returning a String value.
     */
    private static String generateMeetingCode(Constants.Purpose purpose, String username) {
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

        String input = username + dateCreated;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());

            long randomNumber = 0;
            for (int i = 0; i < 8; i++) {
                randomNumber = (randomNumber << 8) | (hash[i] & 0xff);
            }

            return per + dateCreated + String.format("%04d", Math.abs(randomNumber));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }
}
