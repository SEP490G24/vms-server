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
import org.apache.commons.lang3.BooleanUtils;
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
import java.util.Map;
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

        //check time
        checkTimeForTicket(startTime, endTime);
        BooleanUtils.toBooleanDefaultIfNull(ticketInfo.isDraft(), false);

        if (SecurityUtils.getOrgId() != null) {
            if (StringUtils.isEmpty(ticketInfo.getSiteId().trim()))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SiteId is null");
            if (!siteRepository.existsByIdAndOrganizationId(UUID.fromString(ticketInfo.getSiteId()), UUID.fromString(SecurityUtils.getOrgId())))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SiteId is not in organization");
            ticketDto.setSiteId(ticketInfo.getSiteId());
        } else {
            ticketDto.setSiteId(SecurityUtils.getSiteId());
        }

        //check room
        checkRoom(ticketInfo, ticketDto);
        ticketDto.setUsername(username);

        if (ticketInfo.isDraft() == true) {
            ticketDto.setStatus(Constants.StatusTicket.DRAFT);
            Ticket ticket = ticketRepository.save(ticketDto);
            setDataCustomer(ticketInfo, ticket);
            return ticket;
        } else {

            if (startTime == null || startTime.toString().trim().isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Start time is empty");
            }

            if (endTime == null || endTime.toString().trim().isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "End time is empty");
            }

            // check template
            if (StringUtils.isEmpty(ticketInfo.getTemplateId().toString())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "TemplateId is empty");
            }

            Template template = templateRepository.findById(ticketDto.getTemplateId()).orElse(null);

            if (ObjectUtils.isEmpty(template)) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't not found ticket");
            }

            if (!template.getSiteId().equals(UUID.fromString(ticketDto.getSiteId())))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User can not create meeting in this template");

            //check purpose
            if (StringUtils.isEmpty(ticketDto.getPurpose().toString())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose is empty");
            }

            if (ticketDto.getPurpose().equals("OTHERS")) {
                if (StringUtils.isEmpty(ticketInfo.getPurposeNote())) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose other is empty");
                }
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
     * The function `checkRoom` checks if a room is available for booking based on the provided ticket information.
     *
     * @param ticketInfo The `ticketInfo` parameter is an object of type `ITicketController.CreateTicketInfo`. It contains
     *                   information about the ticket being created, such as the room ID, start time, and end time.
     * @param ticket     The "ticket" parameter is an instance of the Ticket class. It represents the ticket for creating a
     *                   meeting in a room.
     */
    private void checkRoom(ITicketController.CreateTicketInfo ticketInfo, Ticket ticket) {
        Room room = roomRepository.findById(ticketInfo.getRoomId()).orElse(null);

        if (!room.getSiteId().equals(UUID.fromString(ticket.getSiteId())))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User can not create meeting in this room");

        if (ObjectUtils.isEmpty(room)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room is null");
        }

        if (isRoomBooked(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room have meeting in this time");
        }

    }

    private void checkTimeForTicket(LocalDateTime startTime, LocalDateTime endTime) {

        if (startTime.isAfter(endTime)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Time is not true");
        }

        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes(); // Chuyển thời gian thành phút

        if (minutes < 15) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Time meeting must greater than 15 minutes");
        }

        if (isUserHaveTicketInTime(SecurityUtils.loginUsername(), startTime, endTime)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User have meeting in this time");
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
     * The function cancels a ticket if it exists and meets the cancellation criteria, and sends an email to the customer
     * with a QR code if applicable.
     *
     * @param cancelTicket The `cancelTicket` parameter is an object of type `ITicketController.CancelTicket`. It contains
     *                     the following properties:
     * @return The method is returning a Boolean value.
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, NullPointerException.class})
    public Boolean cancelTicket(ITicketController.CancelTicket cancelTicket) {
        var ticket = ticketRepository.findById(cancelTicket.getTicketId()).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Ticket is empty");
        }

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = ticket.getStartTime();

        if (startTime.isAfter(currentTime.plusHours(2))) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Meetings cannot be canceled at least 2 hours before they start.");
        }


        if (ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), SecurityUtils.loginUsername())) {
            ticket.setStatus(Constants.StatusTicket.CANCEL);
            ticketRepository.save(ticket);
            List<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
            if (!customerTicketMaps.isEmpty()) {
                customerTicketMaps.forEach(o -> {
                    Customer customer = o.getCustomerEntity();

                    Template template = templateRepository.findById(cancelTicket.getTemplateId()).orElse(null);
                    if (ObjectUtils.isEmpty(template)) {
                        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't not found template");
                    }
                    Map<String, String> parameterMap = Map.of("ten_nguoi_nhan", customer.getVisitorName());
                    String replacedTemplate = emailUtils.replaceEmailParameters(template.getBody(), parameterMap);

                    emailUtils.sendMailWithQRCode(customer.getEmail(), template.getSubject(), replacedTemplate, null, ticket.getSiteId());
                });

            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, NullPointerException.class})
    public Ticket updateTicket(ITicketController.UpdateTicketInfo ticketInfo) {
        if (StringUtils.isEmpty(ticketInfo.getId().toString()))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "TicketId is null");

        Ticket ticketMap = mapper.map(ticketInfo, Ticket.class);
        LocalDateTime updateStartTime = ticketMap.getStartTime();
        LocalDateTime updateEndTime = ticketMap.getEndTime();

        Ticket ticket = ticketRepository.findById(ticketInfo.getId()).orElse(null);

        if (!ticket.getUsername().equals(SecurityUtils.loginUsername())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ticket is not for you!!");
        }

        if (ObjectUtils.isEmpty(ticket)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found ticket by id " + ticketInfo.getId());
        }

        if (StringUtils.isNotEmpty(ticketMap.getRoomId().toString())) {
            if (ticketInfo.getRoomId().equals(ticket.getRoomId())) {
                Room room = roomRepository.findById(ticketInfo.getRoomId()).orElse(null);

                if (!room.getSiteId().equals(UUID.fromString(ticket.getSiteId())))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User can not create meeting in this room");

                if (ObjectUtils.isEmpty(room)) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room is null");
                }

                if (isRoomBooked(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room have meeting in this time");
                }
            }
        }

        LocalDateTime startTime = ticket.getStartTime();
        LocalDateTime endTime = ticket.getEndTime();

        if (updateStartTime != null && updateEndTime == null && !updateStartTime.isEqual(startTime)) {
            checkTimeForTicket(updateStartTime, endTime);
        } else if (updateEndTime != null && updateEndTime == null && !updateEndTime.isEqual(endTime)) {
            checkTimeForTicket(startTime, updateEndTime);
        } else if (updateStartTime != null && updateEndTime != null) {
            checkTimeForTicket(updateStartTime, updateEndTime);
        }

        if (StringUtils.isNotEmpty(ticketMap.getPurpose().toString())) {
            if (!ticketMap.getPurpose().equals(Constants.Purpose.OTHERS) && ticketMap.getPurposeNote() != null) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose Note must be note when Purpose is other");
            }
        }

        ticketRepository.save(ticket.update(ticketMap));

        if (ticketInfo.getNewCustomers() != null) {
            checkNewCustomers(ticketInfo.getNewCustomers(), ticket);
        }
        return ticket;
    }

    private void checkNewCustomers(List<ICustomerController.NewCustomers> newCustomers, Ticket ticket) {
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
                    sendEmail(customer, ticket, ticket.getTemplate());
                } else {
                    createCustomerTicket(ticket, customerExist.getId());
                    sendEmail(customerExist, ticket, ticket.getTemplate());
                }
            }
        }
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
     * The `sendQr` function sends an email to each customer in the `customerTicketMap` list, containing a QR code
     * generated from a meeting URL, along with other relevant information.
     *
     * @param customerTicketMap customerTicketMap is a list of objects of type CustomerTicketMap. Each CustomerTicketMap
     *                          object represents a mapping between a customer and a ticket.
     * @param ticket            The `ticket` parameter is an object of type `Ticket`. It represents a ticket that is associated with
     *                          the QR code being sent.
     * @param template          The `template` parameter is an object of type `Template`. It contains the email template that will
     *                          be used to send the email to the customer. The template object has properties such as `subject` and `body`, which
     *                          represent the subject and content of the email respectively.
     */
    private void sendQr(List<CustomerTicketMap> customerTicketMap, Ticket ticket, Template template) {
        customerTicketMap.forEach(o -> {
            var customer = customerRepository.findById(o.getCustomerTicketMapPk().getCustomerId()).orElse(null);
            sendEmail(customer, ticket, template);
        });
    }

    /**
     * The function `sendEmail` sends an email to a customer with a QR code image generated from a given URL.
     *
     * @param customer The customer object contains information about the customer, such as their name, email, and visitor
     *                 name.
     * @param ticket   The `ticket` parameter is an object of the `Ticket` class. It contains information about a ticket,
     *                 such as its ID and site ID.
     * @param template The `template` parameter is an object of type `Template`. It contains the email template that will
     *                 be used to send the email. The `Template` class likely has properties such as `subject` and `body`, which store the
     *                 subject and body of the email template, respectively.
     */
    private void sendEmail(Customer customer, Ticket ticket, Template template) {
        String meetingUrl = "https://web-vms.azurewebsites.net/ticket/" + ticket.getId().toString() + "/customer/" + customer.getId().toString();

        if (ObjectUtils.isEmpty(customer))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Customer is empty");

        // Tạo mã QR code
        try {
            byte[] qrCodeData = QRcodeUtils.getQRCodeImage(meetingUrl, 800, 800);
            assert template != null;

            Map<String, String> parameterMap = Map.of("ten_nguoi_nhan", customer.getVisitorName());
            String replacedTemplate = emailUtils.replaceEmailParameters(template.getBody(), parameterMap);

            emailUtils.sendMailWithQRCode(customer.getEmail(), template.getSubject(), replacedTemplate, qrCodeData, ticket.getSiteId());
        } catch (WriterException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
