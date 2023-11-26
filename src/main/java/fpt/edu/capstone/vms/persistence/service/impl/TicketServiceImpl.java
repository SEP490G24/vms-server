package fpt.edu.capstone.vms.persistence.service.impl;

import com.google.zxing.WriterException;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.*;
import fpt.edu.capstone.vms.persistence.repository.*;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    final CustomerTicketMapRepository customerTicketMapRepository;
    final EmailUtils emailUtils;
    final AuditLogRepository auditLogRepository;
    final SettingUtils settingUtils;
    final UserRepository userRepository;
    final ReasonRepository reasonRepository;


    private static final String TICKET_TABLE_NAME = "Ticket";
    private static final String CUSTOMER_TICKET_TABLE_NAME = "CustomerTicketMap";


    public TicketServiceImpl(TicketRepository ticketRepository, CustomerRepository customerRepository,
                             TemplateRepository templateRepository, ModelMapper mapper, RoomRepository roomRepository,
                             SiteRepository siteRepository,
                             CustomerTicketMapRepository customerTicketMapRepository, EmailUtils emailUtils, AuditLogRepository auditLogRepository, SettingUtils settingUtils, UserRepository userRepository, ReasonRepository reasonRepository) {
        this.ticketRepository = ticketRepository;
        this.templateRepository = templateRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.roomRepository = roomRepository;
        this.siteRepository = siteRepository;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.emailUtils = emailUtils;
        this.auditLogRepository = auditLogRepository;
        this.settingUtils = settingUtils;
        this.userRepository = userRepository;
        this.reasonRepository = reasonRepository;
        this.init(ticketRepository);
    }


    /**
     * The function creates a ticket based on the provided ticket information, checks for room availability, and sets the
     * ticket status accordingly.
     *
     * @param ticketInfo An object of type ITicketController.CreateTicketInfo, which contains information about the ticket
     *                   being created.
     * @return The method is returning a Ticket object
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, NullPointerException.class})
    public Ticket create(ITicketController.CreateTicketInfo ticketInfo) {

        String username = SecurityUtils.loginUsername();

        var ticketDto = mapper.map(ticketInfo, Ticket.class);
        //check purpose
        if (ticketInfo.getPurpose() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose is empty");
        }

        //Tạo meeting
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
        if (ticketInfo.getRoomId() != null) {
            checkRoom(ticketInfo, ticketDto);
        }
        ticketDto.setUsername(username);

        if (ticketInfo.isDraft() == true) {
            ticketDto.setStatus(Constants.StatusTicket.DRAFT);
            Ticket ticket = ticketRepository.save(ticketDto);
            setDataCustomer(ticketInfo, ticket);
            return ticket;
        } else {

            if (ticketInfo.getPurpose().equals(Constants.Purpose.OTHERS)) {
                if (StringUtils.isEmpty(ticketInfo.getPurposeNote())) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose other is empty");
                }
            }

            ticketDto.setStatus(Constants.StatusTicket.PENDING);
            Ticket ticket = ticketRepository.save(ticketDto);
            Room room = null;
            if (ticketInfo.getRoomId() != null) {
                room = roomRepository.findById(ticketInfo.getRoomId()).orElse(null);
            }
            setDataCustomer(ticketInfo, ticket);
            var customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
            if (customerTicketMaps.isEmpty())
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
            sendQr(customerTicketMaps, ticket, room);

            auditLogRepository.save(new AuditLog(ticket.getSiteId()
                , siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null).getOrganizationId().toString()
                , ticket.getId().toString()
                , TICKET_TABLE_NAME
                , Constants.AuditType.CREATE
                , null
                , ticket.toString()));

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

        if (ObjectUtils.isEmpty(room)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room is null");
        }

        if (!room.getSiteId().equals(UUID.fromString(ticket.getSiteId())))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User can not create meeting in this room");

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

//        if (isUserHaveTicketInTime(SecurityUtils.loginUsername(), startTime, endTime)) {
//            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User have meeting in this time");
//        }
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
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "site is null");
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
                    createCustomerTicket(ticket, customer.getId(), generateCheckInCode());
                } else {
                    createCustomerTicket(ticket, customerExist.getId(), generateCheckInCode());
                }
            }
        }

        if (oldCustomers != null) {
            for (String oldCustomer : oldCustomers) {
                if (StringUtils.isEmpty(oldCustomer.trim()))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
                if (!customerRepository.existsByIdAndAndOrganizationId(UUID.fromString(oldCustomer), orgId))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is null");
                createCustomerTicket(ticket, UUID.fromString(oldCustomer.trim()), generateCheckInCode());
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
            Ticket oldValue = ticket;
            ticketRepository.save(ticket.setBookmark(true));
            auditLogRepository.save(new AuditLog(ticket.getSiteId()
                , siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null).getOrganizationId().toString()
                , ticket.getId().toString()
                , TICKET_TABLE_NAME
                , Constants.AuditType.UPDATE
                , oldValue.toString()
                , ticket.toString()));

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
            auditLogRepository.save(new AuditLog(ticket.getSiteId()
                , siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null).getOrganizationId().toString()
                , ticket.getId().toString()
                , TICKET_TABLE_NAME
                , Constants.AuditType.DELETE
                , ticket.toString()
                , null));
            deleteTicketCustomerMap(ticket);
            ticketRepository.delete(ticket);
            return true;
        }
        return false;
    }

    private void deleteTicketCustomerMap(Ticket ticket) {
        var customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
        if (customerTicketMaps != null) {
            customerTicketMaps.forEach(o -> {
                auditLogRepository.save(new AuditLog(ticket.getSiteId()
                    , siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null).getOrganizationId().toString()
                    , o.getId().toString()
                    , CUSTOMER_TICKET_TABLE_NAME
                    , Constants.AuditType.DELETE
                    , o.toString()
                    , null));
            });
        }
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

        if (!startTime.isAfter(currentTime.plusHours(2))) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Meetings cannot be canceled at least 2 hours before they start.");
        }
        if (SecurityUtils.getOrgId() != null) {
            if (StringUtils.isEmpty(ticket.getSiteId().trim()))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SiteId is null");
            if (!siteRepository.existsByIdAndOrganizationId(UUID.fromString(ticket.getSiteId()), UUID.fromString(SecurityUtils.getOrgId())))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You don't have permission to do this.");
            settingUtils.loadSettingsSite(ticket.getSiteId());
        } else {
            settingUtils.loadSettingsSite(SecurityUtils.getSiteId());
        }

        Template template = templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL))).orElse(null);
        Reason reason = reasonRepository.findById(cancelTicket.getTicketId()).orElse(null);
        if (ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), SecurityUtils.loginUsername())) {
            Ticket oldValue = ticket;
            ticket.setStatus(Constants.StatusTicket.CANCEL);
            ticketRepository.save(ticket);
            List<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
            if (!customerTicketMaps.isEmpty()) {
                customerTicketMaps.forEach(o -> {
                    Customer customer = o.getCustomerEntity();

                    if (ObjectUtils.isEmpty(template)) {
                        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't not found template");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                    String date = ticket.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String startTime1 = ticket.getStartTime().format(formatter);
                    String endTime = ticket.getEndTime().format(formatter);

                    Map<String, String> parameterMap = new HashMap<>();
                    parameterMap.put("customerName", customer.getVisitorName());
                    parameterMap.put("meetingName", ticket.getName());
                    parameterMap.put("dateTime", date);
                    parameterMap.put("startTime", startTime1);
                    parameterMap.put("endTime", endTime);
                    parameterMap.put("reason", reason.getName());
                    String replacedTemplate = emailUtils.replaceEmailParameters(template.getBody(), parameterMap);

                    emailUtils.sendMailWithQRCode(customer.getEmail(), template.getSubject(), replacedTemplate, null, ticket.getSiteId());
                });

            }
            auditLogRepository.save(new AuditLog(ticket.getSiteId()
                , siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null).getOrganizationId().toString()
                , ticket.getId().toString()
                , TICKET_TABLE_NAME
                , Constants.AuditType.UPDATE
                , oldValue.toString()
                , ticket.toString()));
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, NullPointerException.class})
    public Ticket updateTicket(ITicketController.UpdateTicketInfo ticketInfo) {
        if (ticketInfo.getId() == null)
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "TicketId is null");

        Ticket ticketMap = mapper.map(ticketInfo, Ticket.class);
        LocalDateTime updateStartTime = ticketMap.getStartTime();
        LocalDateTime updateEndTime = ticketMap.getEndTime();

        Ticket ticket = ticketRepository.findById(ticketInfo.getId()).orElse(null);

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = ticket.getStartTime();

        if (!startTime.isAfter(currentTime.plusHours(2))) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Meetings cannot be updated at least 2 hours before they start.");
        }

        if (ObjectUtils.isEmpty(ticket)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found ticket by id " + ticketInfo.getId());
        }

        if (!ticket.getUsername().equals(SecurityUtils.loginUsername())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ticket is not for you!!");
        }

        if (StringUtils.isNotEmpty(ticketMap.getRoomId().toString())) {
            if (ticketInfo.getRoomId().equals(ticket.getRoomId())) {
                Room room = roomRepository.findById(ticketInfo.getRoomId()).orElse(null);

                if (ObjectUtils.isEmpty(room)) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room is null");
                }

                if (!room.getSiteId().equals(UUID.fromString(ticket.getSiteId())))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User can not create meeting in this room");

                if (isRoomBooked(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Room have meeting in this time");
                }
            }
        }

        LocalDateTime endTime = ticket.getEndTime();

        if (updateStartTime != null && updateEndTime == null && !updateStartTime.isEqual(startTime)) {
            checkTimeForTicket(updateStartTime, endTime);
        } else if (updateEndTime != null && updateEndTime == null && !updateEndTime.isEqual(endTime)) {
            checkTimeForTicket(startTime, updateEndTime);
        } else if (updateStartTime != null && updateEndTime != null) {
            checkTimeForTicket(updateStartTime, updateEndTime);
        }

        if (ticketMap.getPurpose() == Constants.Purpose.OTHERS && ticketMap.getPurposeNote() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose Note must not be null when Purpose is other");
        } else if (ticketMap.getPurpose() != Constants.Purpose.OTHERS && ticketMap.getPurposeNote() != null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Purpose Note must be null when Purpose is not other");
        }

        Ticket oldValue = ticket;
        ticketRepository.save(ticket.update(ticketMap));
        Room room = roomRepository.findById(ticket.getRoomId()).orElse(null);
        List<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
        if (customerTicketMaps != null) {
            customerTicketMaps.forEach(o -> {
                Customer customer = o.getCustomerEntity();
                sendEmail(customer, ticket, room, o.getCheckInCode(), true);
            });
        }

        if (ticketInfo.getNewCustomers() != null) {
            checkNewCustomers(ticketInfo.getNewCustomers(), ticket, room);
        }

        auditLogRepository.save(new AuditLog(ticket.getSiteId()
            , siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null).getOrganizationId().toString()
            , ticket.getId().toString()
            , TICKET_TABLE_NAME
            , Constants.AuditType.UPDATE
            , oldValue.toString()
            , ticket.toString()));

        return ticket;
    }

    public void checkNewCustomers(List<ICustomerController.NewCustomers> newCustomers, Ticket ticket, Room room) {
        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "site is false");
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
                if (customerExist == null) {
                    var _customer = mapper.map(customerDto, Customer.class);
                    _customer.setOrganizationId(orgId);
                    Customer customer = customerRepository.save(_customer);
                    String checkInCode = generateCheckInCode();
                    createCustomerTicket(ticket, customer.getId(), checkInCode);
                    sendEmail(customer, ticket, room, checkInCode, false);
                } else {
                    String checkInCode = generateCheckInCode();
                    createCustomerTicket(ticket, customerExist.getId(), checkInCode);
                    sendEmail(customerExist, ticket, room, checkInCode, false);
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

        List<String> usernames = new ArrayList<>();
        usernames.add(SecurityUtils.loginUsername());

        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return ticketRepository.filter(pageableSort
            , names
            , null
            , usernames
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
        , List<String> names
        , List<String> sites
        , List<String> usernames
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
        , String keyword) {

        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return ticketRepository.filter(pageableSort
            , names
            , SecurityUtils.getListSiteToString(siteRepository, sites)
            , usernames
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
        List<String> usernames = new ArrayList<>();
        usernames.add(SecurityUtils.loginUsername());
        return ticketRepository.filter(names
            , null
            , usernames
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
        , List<String> sites
        , List<String> usernames
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
        , String keyword) {
        return ticketRepository.filter(names
            , SecurityUtils.getListSiteToString(siteRepository, sites)
            , usernames
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
    public ITicketController.TicketByQRCodeResponseDTO findByQRCode(String checkInCode) {
        CustomerTicketMap customerTicketMap = customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInCode);
        String site = SecurityUtils.getSiteId();
        if (!site.equals(customerTicketMap.getTicketEntity().getSiteId())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ticket can not found in site");
        }
        return mapper.map(customerTicketMap, ITicketController.TicketByQRCodeResponseDTO.class);
    }

    @Override
    @Transactional
    public ITicketController.TicketByQRCodeResponseDTO checkInCustomer(ITicketController.CheckInPayload checkInPayload) {
        CustomerTicketMap customerTicketMap = customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode());
        customerTicketMap.setStatus(checkInPayload.getStatus());
        customerTicketMap.setReasonId(checkInPayload.getReasonId());
        customerTicketMap.setReasonNote(checkInPayload.getReasonNote());
        if (checkInPayload.getStatus().equals(Constants.StatusTicket.CHECK_IN)) {
            customerTicketMap.setCheckInTime(LocalDateTime.now());
        } else if (checkInPayload.getStatus().equals(Constants.StatusTicket.CHECK_OUT)) {
            customerTicketMap.setCheckOutTime(LocalDateTime.now());
            customerTicketMap.setCheckOut(true);
            customerTicketMap.setCardId(null);
        }
        customerTicketMapRepository.save(customerTicketMap);
        Ticket ticket = ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId()).orElse(null);

        assert ticket != null;
        auditLogRepository.save(new AuditLog(ticket.getSiteId()
            , Objects.requireNonNull(siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null)).getOrganizationId().toString()
            , customerTicketMap.getId().toString()
            , CUSTOMER_TICKET_TABLE_NAME
            , Constants.AuditType.CREATE
            , null
            , customerTicketMap.toString()));
        return mapper.map(customerTicketMap, ITicketController.TicketByQRCodeResponseDTO.class)
            .setSiteId(ticket.getSiteId());
    }

    @Override
    public ITicketController.TicketFilterDTO findByTicketForUser(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't not found ticket");
        }
        if (!ticket.getUsername().equals(SecurityUtils.loginUsername())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Can't not view this ticket");
        }
        ITicketController.TicketFilterDTO ticketFilterDTO = mapper.map(ticket, ITicketController.TicketFilterDTO.class);
        return ticketFilterDTO;
    }

    @Override
    public ITicketController.TicketFilterDTO findByTicketForAdmin(UUID ticketId, String siteId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't not found ticket");
        }
        if (SecurityUtils.getOrgId() != null) {
            if (siteId == null) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Must to choose siteId for organization admin");
            }
            Site site = siteRepository.findById(UUID.fromString(siteId)).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "site is null");
            }
            if (!site.getOrganizationId().equals(SecurityUtils.getOrgId())) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "This admin can't view this ticket of customer");
            }
            return mapper.map(ticket, ITicketController.TicketFilterDTO.class);
        } else {
            if (!ticket.getSiteId().equals(SecurityUtils.getSiteId())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "This admin can't view this ticket of customer");
            }
            return mapper.map(ticket, ITicketController.TicketFilterDTO.class);
        }
    }

    @Override
    public Page<ITicketController.TicketByQRCodeResponseDTO> filterTicketAndCustomer(Pageable pageable
        , List<String> sites
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
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        Page<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.filter(pageableSort, sites, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd
            , roomId
            , status
            , purpose
            , keyword);
        List<ITicketController.TicketByQRCodeResponseDTO> ticketByQRCodeResponseDTOS = mapper.map(customerTicketMaps.getContent(), new TypeToken<List<ITicketController.TicketByQRCodeResponseDTO>>() {
        }.getType());

        return new PageImpl(ticketByQRCodeResponseDTOS, pageable, customerTicketMaps.getTotalElements());
    }

    @Override
    public boolean addCardCustomerTicket(ITicketController.CustomerTicketCardDTO customerTicketCardDTO) {
        CustomerTicketMap customerTicketMap = customerTicketMapRepository.findByCheckInCodeIgnoreCase(customerTicketCardDTO.getCheckInCode());
        if (customerTicketMap != null) {
            Ticket ticket = ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId()).orElse(null);
            if (!customerTicketMap.getStatus().equals(Constants.StatusTicket.CHECK_IN)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Customer is not check-in, please check in before add card");
            }
            if (SecurityUtils.getOrgId() != null) {
                if (StringUtils.isEmpty(ticket.getSiteId().trim()))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SiteId is null");
                if (!siteRepository.existsByIdAndOrganizationId(UUID.fromString(ticket.getSiteId()), UUID.fromString(SecurityUtils.getOrgId())))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You don't have permission to do this.");
                settingUtils.loadSettingsSite(ticket.getSiteId());
            } else {
                settingUtils.loadSettingsSite(SecurityUtils.getSiteId());
            }
            if (!settingUtils.getBoolean(Constants.SettingCode.CONFIGURATION_CARD)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Site is not using card");
            }
            if (customerTicketCardDTO.getCardId() == null) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Card is null");
            }
            if (customerTicketMapRepository.existsByCardIdAndStatus(customerTicketCardDTO.getCardId(), Constants.StatusTicket.CHECK_IN)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Card is exists with customer in this site");
            }
            customerTicketMap.setCardId(customerTicketCardDTO.getCardId());
            customerTicketMapRepository.save(customerTicketMap);
            return true;
        }
        return false;
    }

    @Override
    public ITicketController.TicketByRoomResponseDTO filterTicketByRoom(List<String> names, List<String> sites, List<String> usernames, UUID roomId, Constants.StatusTicket status, Constants.Purpose purpose, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, LocalDateTime startTimeStart, LocalDateTime startTimeEnd, LocalDateTime endTimeStart, LocalDateTime endTimeEnd, String createdBy, String lastUpdatedBy, String keyword) {
        List<Room> rooms;
        if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
            rooms = roomRepository.filter(null, SecurityUtils.getListSiteToUUID(siteRepository, sites), null, null, null, null, null);
        } else {
            rooms = roomRepository.filter(null, null, null, null, null, null, SecurityUtils.loginUsername());
        }

        List<Ticket> tickets;
        if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
            tickets = filterAllBySite(null, sites, null, null, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, null, null, keyword);
        } else {
            tickets = filterAllBySite(names, null, null, null, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, SecurityUtils.loginUsername(), null, keyword);
        }
        List<ITicketController.TicketFilterDTO> ticketFilterDTOS = mapper.map(tickets, new TypeToken<List<ITicketController.TicketFilterDTO>>() {
        }.getType());
        ticketFilterDTOS.forEach(o -> {
            setCustomer(o);

        });
        ITicketController.TicketByRoomResponseDTO ticketByRoomResponseDTO = new ITicketController.TicketByRoomResponseDTO();

        return ticketByRoomResponseDTO.builder().rooms(rooms).tickets(ticketFilterDTOS).build();
    }

    /**
     * The `sendQr` function sends an email to each customer in the `customerTicketMap` list, containing a QR code
     * generated from a meeting URL, along with other relevant information.
     *
     * @param customerTicketMap customerTicketMap is a list of objects of type CustomerTicketMap. Each CustomerTicketMap
     *                          object represents a mapping between a customer and a ticket.
     * @param ticket            The `ticket` parameter is an object of type `Ticket`. It represents a ticket that is associated with
     *                          the QR code being sent.
     */
    private void sendQr(List<CustomerTicketMap> customerTicketMap, Ticket ticket, Room room) {
        customerTicketMap.forEach(o -> {
            var customer = customerRepository.findById(o.getCustomerTicketMapPk().getCustomerId()).orElse(null);
            sendEmail(customer, ticket, room, o.getCheckInCode(), false);
        });
    }

    /**
     * The function `sendEmail` sends an email to a customer with a QR code image generated from a given URL.
     *
     * @param customer The customer object contains information about the customer, such as their name, email, and visitor
     *                 name.
     * @param ticket   The `ticket` parameter is an object of the `Ticket` class. It contains information about a ticket,
     *                 such as its ID and site ID.
     */
    public void sendEmail(Customer customer, Ticket ticket, Room room, String checkInCode, boolean isUpdate) {
        String meetingUrl = "https://web-vms.azurewebsites.net/check-in/" + checkInCode;

        if (ObjectUtils.isEmpty(customer))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Customer is empty");

        // Tạo mã QR code
        try {
            byte[] qrCodeData = QRcodeUtils.getQRCodeImage(meetingUrl, 800, 800);

            //template email
            String siteId = ticket.getSiteId();
            Site site = siteRepository.findById(UUID.fromString(siteId)).orElse(null);

            //get template email to setting site

            settingUtils.loadSettingsSite(siteId);

            Template template = templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CONFIRM_EMAIL))).orElse(null);

            User user = userRepository.findFirstByUsername(ticket.getUsername());
            if (ObjectUtils.isEmpty(template)) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't not found template");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            String date = ticket.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String startTime = ticket.getStartTime().format(formatter);
            String endTime = ticket.getEndTime().format(formatter);

            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("customerName", customer.getVisitorName());
            parameterMap.put("meetingName", ticket.getName());
            parameterMap.put("dateTime", date);
            parameterMap.put("startTime", startTime);
            parameterMap.put("endTime", endTime);
            String address = site.getAddress() != null ? site.getAddress() : site.getCommune().getName() + ", " + site.getDistrict().getName() + ", " + site.getProvince().getName();
            parameterMap.put("address", address);
            String roomName = room != null ? room.getName() : "Updating....";
            parameterMap.put("roomName", roomName);
            parameterMap.put("staffName", user.getFirstName() + " " + user.getLastName());
            parameterMap.put("staffPhone", user.getPhoneNumber());
            parameterMap.put("staffEmail", user.getEmail());
            parameterMap.put("checkInCode", checkInCode);
            String replacedTemplate = emailUtils.replaceEmailParameters(template.getBody(), parameterMap);

            String subject;
            if (isUpdate) {
                subject = "Update information of meeting #" + checkInCode;
            } else {
                subject = template.getSubject();
            }

            emailUtils.sendMailWithQRCode(customer.getEmail(), subject, replacedTemplate, qrCodeData, ticket.getSiteId());
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
    public void createCustomerTicket(Ticket ticket, UUID customerId, String checkInCode) {
        CustomerTicketMap customerTicketMap = new CustomerTicketMap();
        CustomerTicketMapPk pk = new CustomerTicketMapPk();
        pk.setTicketId(ticket.getId());
        pk.setCustomerId(customerId);
        customerTicketMap.setCustomerTicketMapPk(pk);
        customerTicketMap.setStatus(Constants.StatusTicket.PENDING);
        customerTicketMap.setCheckInCode(checkInCode);
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
    boolean isRoomBooked(UUID roomId, LocalDateTime startTime, LocalDateTime endTime) {
        int count = ticketRepository.countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqualAndStatusNotLike(roomId, startTime, endTime, Constants.StatusTicket.CANCEL);
        return count > 0;
    }

    /**
     * The function generates a meeting code based on the purpose and current date.
     *
     * @param purpose The purpose parameter is of type Constants.Purpose, which is an enum that represents the purpose of
     *                the meeting. The possible values for purpose are CONFERENCES, INTERVIEW, MEETING, OTHERS, and WORKING.
     * @return The method is returning a String value.
     */
    public static String generateMeetingCode(Constants.Purpose purpose, String username) {
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

    public static String generateCheckInCode() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String characters = upperCaseLetters + digits;

        SecureRandom random = new SecureRandom();

        StringBuilder checkInCodeBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            checkInCodeBuilder.append(randomChar);
        }

        return checkInCodeBuilder.toString();
    }

    private void setCustomer(ITicketController.TicketFilterDTO ticketFilterDTO) {
        List<ICustomerController.CustomerInfo> customerInfos = new ArrayList<>();
        customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticketFilterDTO.getId()).forEach(a -> {
            customerInfos.add(mapper.map(customerRepository.findById(a.getCustomerTicketMapPk().getCustomerId()).orElse(null), ICustomerController.CustomerInfo.class));
        });
        ticketFilterDTO.setCustomers(customerInfos);
    }
}
