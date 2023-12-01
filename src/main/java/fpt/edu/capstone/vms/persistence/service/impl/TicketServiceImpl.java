package fpt.edu.capstone.vms.persistence.service.impl;

import com.google.zxing.WriterException;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.constants.I18n;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMapPk;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.ReasonRepository;
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.EmailUtils;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.QRcodeUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import fpt.edu.capstone.vms.util.SettingUtils;
import fpt.edu.capstone.vms.util.Utils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
            throw new CustomException(ErrorApp.PURPOSE_NOT_FOUND);
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
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            if (!siteRepository.existsByIdAndOrganizationId(UUID.fromString(ticketInfo.getSiteId()), UUID.fromString(SecurityUtils.getOrgId())))
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
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
                    throw new CustomException(ErrorApp.PURPOSE_OTHER_NOT_NULL_OTHER);
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
                throw new CustomException(ErrorApp.CUSTOMER_NOT_FOUND);
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
            throw new CustomException(ErrorApp.ROOM_NOT_FOUND);
        }

        if (!room.getSiteId().equals(UUID.fromString(ticket.getSiteId())))
            throw new CustomException(ErrorApp.ROOM_USER_CAN_NOT_CREATE_TICKET);

        if (isRoomBooked(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
            throw new CustomException(ErrorApp.ROOM_HAVE_TICKET_IN_THIS_TIME);
        }

    }

    private void checkTimeForTicket(LocalDateTime startTime, LocalDateTime endTime) {

        if (startTime.isAfter(endTime)) {
            throw new CustomException(ErrorApp.TICKET_START_TIME_IS_AFTER_THAN_END_TIME);
        }

        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes(); // Chuyển thời gian thành phút

        if (minutes < 15) {
            throw new CustomException(ErrorApp.TICKET_TIME_MEETING_MUST_GREATER_THAN_15_MINUTES);
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
            throw new CustomException(ErrorApp.CUSTOMER_IS_NULL);
        }

        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }
        if (newCustomers != null) {
            for (ICustomerController.NewCustomers customerDto : newCustomers) {
                if (ObjectUtils.isEmpty(customerDto))
                    throw new CustomException(ErrorApp.CUSTOMER_NOT_FOUND);
                if (!Utils.isCCCDValid(customerDto.getIdentificationNumber())) {
                    throw new CustomException(ErrorApp.CUSTOMER_IDENTITY_NOT_CORRECT);
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
                    throw new CustomException(ErrorApp.CUSTOMER_NOT_FOUND);
                if (!customerRepository.existsByIdAndAndOrganizationId(UUID.fromString(oldCustomer), orgId))
                    throw new CustomException(ErrorApp.CUSTOMER_NOT_IN_ORGANIZATION);
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
            throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);
        }
        var ticket = ticketRepository.findById(UUID.fromString(ticketBookmark.getTicketId())).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new CustomException(ErrorApp.TICKET_NOT_FOUND);
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
            throw new CustomException(ErrorApp.TICKET_NOT_FOUND);
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
            throw new CustomException(ErrorApp.TICKET_NOT_FOUND);
        }

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = ticket.getStartTime();

        if (!startTime.isAfter(currentTime.plusHours(2))) {
            throw new CustomException(ErrorApp.TICKET_TIME_CANCEL_MEETING_MUST_BEFORE_2_HOURS);
        }
        if (SecurityUtils.getOrgId() != null) {
            if (StringUtils.isEmpty(ticket.getSiteId().trim()))
                throw new CustomException(ErrorApp.SITE_ID_NULL);
            if (!siteRepository.existsByIdAndOrganizationId(UUID.fromString(ticket.getSiteId()), UUID.fromString(SecurityUtils.getOrgId())))
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            settingUtils.loadSettingsSite(ticket.getSiteId());
        } else {
            settingUtils.loadSettingsSite(SecurityUtils.getSiteId());
        }

        Template template = templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL))).orElse(null);
        Reason reason = reasonRepository.findById(cancelTicket.getReasonId()).orElse(null);
        if (ticketRepository.existsByIdAndUsername(cancelTicket.getTicketId(), SecurityUtils.loginUsername())) {
            Ticket oldValue = ticket;
            ticket.setStatus(Constants.StatusTicket.CANCEL);
            ticketRepository.save(ticket);
            List<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());
            if (!customerTicketMaps.isEmpty()) {
                customerTicketMaps.forEach(o -> {
                    Customer customer = o.getCustomerEntity();
                    if (ObjectUtils.isEmpty(template)) {
                        throw new CustomException(ErrorApp.TEMPLATE_NOT_FOUND);
                    }
                    sendEmailCancel(ticket, customer, template, reason);
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

    public void sendEmailCancel(Ticket ticket, Customer customer, Template template, Reason reason) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String date = ticket.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String startTime1 = ticket.getStartTime().format(formatter);
        String endTime = ticket.getEndTime().format(formatter);
        User user = userRepository.findFirstByUsername(ticket.getUsername());

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("customerName", customer.getVisitorName());
        parameterMap.put("staffName", user.getFirstName() + " " + user.getLastName());
        parameterMap.put("meetingName", ticket.getName());
        parameterMap.put("dateTime", date);
        parameterMap.put("startTime", startTime1);
        parameterMap.put("endTime", endTime);
        parameterMap.put("reason", reason != null ? I18n.getMessage(reason.getCode()) : "Updating...");
        String replacedTemplate = emailUtils.replaceEmailParameters(template.getBody(), parameterMap);

        emailUtils.sendMailWithQRCode(customer.getEmail(), template.getSubject(), replacedTemplate, null, ticket.getSiteId());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, NullPointerException.class})
    public Ticket updateTicket(ITicketController.UpdateTicketInfo ticketInfo) {
        if (ticketInfo.getId() == null)
            throw new CustomException(ErrorApp.TICKET_ID_NULL);

        Ticket ticketMap = mapper.map(ticketInfo, Ticket.class);
        LocalDateTime updateStartTime = ticketMap.getStartTime();
        LocalDateTime updateEndTime = ticketMap.getEndTime();

        Ticket ticket = ticketRepository.findById(ticketInfo.getId()).orElse(null);

        if (ticket.getStatus().equals(Constants.StatusTicket.CANCEL)) {
            throw new CustomException(ErrorApp.TICKET_IS_CANCEL_CAN_NOT_DO_UPDATE);
        }
        if (ticket.getStatus().equals(Constants.StatusTicket.COMPLETE)) {
            throw new CustomException(ErrorApp.TICKET_IS_COMPLETE_CAN_NOT_DO_UPDATE);
        }
        if (ticket.getEndTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorApp.TICKET_IS_EXPIRED_CAN_NOT_UPDATE);
        }

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = ticket.getStartTime();

        if (!startTime.isAfter(currentTime.plusHours(2))) {
            throw new CustomException(ErrorApp.TICKET_TIME_UPDATE_MEETING_MUST_BEFORE_2_HOURS);
        }

        if (ObjectUtils.isEmpty(ticket)) {
            throw new CustomException(ErrorApp.TICKET_NOT_FOUND);
        }

        if (!ticket.getUsername().equals(SecurityUtils.loginUsername())) {
            throw new CustomException(ErrorApp.TICKET_YOU_CAN_UPDATE_THIS_TICKET);
        }

        if (StringUtils.isNotEmpty(ticketInfo.getRoomId())) {
            if (!ticketInfo.getRoomId().equals(ticket.getRoomId())) {
                Room room = roomRepository.findById(UUID.fromString(ticketInfo.getRoomId())).orElse(null);

                if (ObjectUtils.isEmpty(room)) {
                    throw new CustomException(ErrorApp.ROOM_NOT_FOUND);
                }

                if (!room.getSiteId().equals(UUID.fromString(ticket.getSiteId())))
                    throw new CustomException(ErrorApp.ROOM_USER_CAN_NOT_CREATE_TICKET);

                if (isRoomBooked(UUID.fromString(ticketInfo.getRoomId()), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
                    throw new CustomException(ErrorApp.ROOM_HAVE_TICKET_IN_THIS_TIME);
                }
                ticketMap.setRoomId(UUID.fromString(ticketInfo.getRoomId()));
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
            throw new CustomException(ErrorApp.PURPOSE_OTHER_NOT_NULL_OTHER);
        } else if (ticketMap.getPurpose() != Constants.Purpose.OTHERS && ticketMap.getPurposeNote() != null) {
            throw new CustomException(ErrorApp.PURPOSE_OTHER_MUST_NULL_WHEN_TYPE_NOT_OTHER);
        }

        if (ticketInfo.isDraft() == true && ticket.getStatus().equals(Constants.StatusTicket.PENDING)) {
            throw new CustomException(ErrorApp.TICKET_IS_PENDING_CAN_NOT_SAVE_IS_DRAFT);
        }

        if (ticketInfo.isDraft() == true) {
            ticket.setStatus(Constants.StatusTicket.DRAFT);
        } else {
            ticket.setStatus(Constants.StatusTicket.PENDING);
        }

        Ticket oldValue = ticket;
        ticketRepository.save(ticket.update(ticketMap));


        Room room = roomRepository.findById(ticket.getRoomId()).orElse(null);
        if (ticketInfo.getOldCustomers() != null) {
            checkOldCustomers(ticketInfo.getOldCustomers(), ticket, room, ticketInfo.isDraft());
        }

        if (ticketInfo.getNewCustomers() != null) {
            checkNewCustomers(ticketInfo.getNewCustomers(), ticket, room, ticketInfo.isDraft());
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

    public void checkOldCustomers(List<String> oldCustomers, Ticket ticket, Room room, boolean isDraft) {
        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }

        List<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticket.getId());

        // List of customer id old
        List<String> CustomerOfTicket = customerTicketMaps.stream()
            .map(customerTicketMap -> customerTicketMap.getCustomerTicketMapPk().getCustomerId().toString())
            .collect(Collectors.toList());

        // Customer to add
        List<String> customersToAdd = oldCustomers.stream()
            .filter(customerToRemove -> !CustomerOfTicket.contains(customerToRemove))
            .collect(Collectors.toList());

        // Customer to remove
        List<String> customersToRemove = CustomerOfTicket.stream()
            .filter(customerToAdd -> !oldCustomers.contains(customerToAdd))
            .collect(Collectors.toList());

        Template template = templateRepository.findById(UUID.fromString(settingUtils.getOrDefault(Constants.SettingCode.TICKET_TEMPLATE_CANCEL_EMAIL))).orElse(null);

        if (!customersToRemove.isEmpty()) {
            for (String customer : customersToRemove) {
                if (StringUtils.isEmpty(customer.trim()))
                    throw new CustomException(ErrorApp.CUSTOMER_NOT_FOUND);
                CustomerTicketMapPk customerTicketMapPk = new CustomerTicketMapPk();
                customerTicketMapPk.setTicketId(ticket.getId());
                customerTicketMapPk.setCustomerId(UUID.fromString(customer.trim()));
                CustomerTicketMap customerTicketMap = customerTicketMapRepository.findById(customerTicketMapPk).orElse(null);
                if (customerTicketMap != null) {
                    customerTicketMapRepository.delete(customerTicketMap);
                    if (!isDraft) {
                        sendEmailCancel(ticket, customerTicketMap.getCustomerEntity(), template, null);
                    }
                }
            }
        }

        if (!customersToAdd.isEmpty()) {
            for (String customer : customersToAdd) {
                if (StringUtils.isEmpty(customer.trim()))
                    throw new CustomException(ErrorApp.CUSTOMER_NOT_FOUND);
                if (!customerRepository.existsByIdAndAndOrganizationId(UUID.fromString(customer), orgId))
                    throw new CustomException(ErrorApp.CUSTOMER_NOT_IN_ORGANIZATION);
                createCustomerTicket(ticket, UUID.fromString(customer.trim()), generateCheckInCode());
                if (!isDraft) {
                    customerRepository.findById(UUID.fromString(customer.trim())).ifPresent(customerEntity -> sendEmail(customerEntity, ticket, room, generateCheckInCode(), false));
                }
            }
        }

        if (customersToRemove.isEmpty() && customersToAdd.isEmpty() && !isDraft) {
            CustomerOfTicket.forEach(customer -> {
                CustomerTicketMapPk customerTicketMapPk = new CustomerTicketMapPk();
                customerTicketMapPk.setTicketId(ticket.getId());
                customerTicketMapPk.setCustomerId(UUID.fromString(customer.trim()));
                CustomerTicketMap customerTicketMap = customerTicketMapRepository.findById(customerTicketMapPk).orElse(null);
                sendEmail(customerTicketMap.getCustomerEntity(), ticket, room, customerTicketMap.getCheckInCode(), true);
            });
        }

    }

    public void checkNewCustomers(List<ICustomerController.NewCustomers> newCustomers, Ticket ticket, Room room, boolean isDraft) {
        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }

        if (newCustomers != null) {
            for (ICustomerController.NewCustomers customerDto : newCustomers) {
                if (ObjectUtils.isEmpty(customerDto))
                    throw new CustomException(ErrorApp.CUSTOMER_IS_NULL);
                if (!Utils.isCCCDValid(customerDto.getIdentificationNumber())) {
                    throw new CustomException(ErrorApp.CUSTOMER_IDENTITY_NOT_CORRECT);
                }
                Customer customerExist = customerRepository.findByIdentificationNumberAndOrganizationId(customerDto.getIdentificationNumber(), orgId);
                if (customerExist == null) {
                    var _customer = mapper.map(customerDto, Customer.class);
                    _customer.setOrganizationId(orgId);
                    Customer customer = customerRepository.save(_customer);
                    String checkInCode = generateCheckInCode();
                    createCustomerTicket(ticket, customer.getId(), checkInCode);
                    if (!isDraft)
                        sendEmail(customer, ticket, room, checkInCode, false);
                } else {
                    String checkInCode = generateCheckInCode();
                    createCustomerTicket(ticket, customerExist.getId(), checkInCode);
                    if (!isDraft)
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
        if (customerTicketMap.getTicketEntity().getEndTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorApp.TICKET_IS_EXPIRED);
        }
        String site = SecurityUtils.getSiteId();
        if (!site.equals(customerTicketMap.getTicketEntity().getSiteId())) {
            throw new CustomException(ErrorApp.SITE_NOT_FOUND);
        }
        return mapper.map(customerTicketMap, ITicketController.TicketByQRCodeResponseDTO.class);
    }

    @Override
    @Transactional
    public ITicketController.TicketByQRCodeResponseDTO updateStatusCustomerOfTicket(ITicketController.CheckInPayload checkInPayload) {
        CustomerTicketMap customerTicketMap = customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInPayload.getCheckInCode());
        if (customerTicketMap.getTicketEntity().getStatus().equals(Constants.StatusTicket.CANCEL)) {
            throw new CustomException(ErrorApp.TICKET_IS_CANCEL_CAN_NOT_DO_CHECK);
        }
        if (customerTicketMap.getTicketEntity().getStatus().equals(Constants.StatusTicket.COMPLETE)) {
            throw new CustomException(ErrorApp.TICKET_IS_COMPLETE_CAN_NOT_DO_CHECK);
        }
        if (customerTicketMap.getTicketEntity().getStatus().equals(Constants.StatusTicket.DRAFT)) {
            throw new CustomException(ErrorApp.TICKET_IS_DRAFT_CAN_NOT_DO_CHECK);
        }
        if (checkInPayload.getStatus().equals(Constants.StatusCustomerTicket.CHECK_IN)) {
            if (customerTicketMap.getStatus().equals(Constants.StatusCustomerTicket.CHECK_IN)) {
                throw new CustomException(ErrorApp.CUSTOMER_IS_CHECK_IN);
            }
            if (customerTicketMap.isCheckOut()) {
                throw new CustomException(ErrorApp.CUSTOMER_IS_CHECK_OUT);
            }
            if (customerTicketMap.getTicketEntity().getEndTime().isBefore(LocalDateTime.now())) {
                throw new CustomException(ErrorApp.TICKET_IS_EXPIRED_CAN_NOT_CHECK_IN);
            }
            if (customerTicketMap.getTicketEntity().getStartTime().isAfter(LocalDateTime.now().minusHours(1))) {
                throw new CustomException(ErrorApp.TICKET_NOT_START_CAN_NOT_CHECK_IN);
            }
            customerTicketMap.setStatus(checkInPayload.getStatus());
            customerTicketMap.setCheckInTime(LocalDateTime.now());
            customerTicketMapRepository.save(customerTicketMap);
        } else if (checkInPayload.getStatus().equals(Constants.StatusCustomerTicket.CHECK_OUT)) {
            if (!customerTicketMap.getStatus().equals(Constants.StatusCustomerTicket.CHECK_IN)) {
                throw new CustomException(ErrorApp.CUSTOMER_NOT_CHECK_IN_TO_CHECK_OUT);
            }
            if (customerTicketMap.getTicketEntity().getStartTime().isAfter(LocalDateTime.now())) {
                throw new CustomException(ErrorApp.TICKET_NOT_START_CAN_NOT_CHECK_OUT);
            }
            customerTicketMap.setCheckOutTime(LocalDateTime.now());
            customerTicketMap.setCheckOut(true);
            customerTicketMap.setCardId(null);
            customerTicketMap.setStatus(checkInPayload.getStatus());
            customerTicketMapRepository.save(customerTicketMap);
            Integer count = customerTicketMapRepository.countAllByStatusAndAndCustomerTicketMapPk_TicketId(Constants.StatusCustomerTicket.CHECK_IN, customerTicketMap.getCustomerTicketMapPk().getTicketId());
            if (count == 0) {
                Ticket ticket = ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId()).orElse(null);
                if (ticket != null) {
                    if (ticket.getStartTime().isAfter(LocalDateTime.now())) {
                        throw new CustomException(ErrorApp.TICKET_NOT_START_CAN_NOT_CHECK_OUT);
                    }
                    ticket.setStatus(Constants.StatusTicket.COMPLETE);
                    ticketRepository.save(ticket);
                }
            }
        } else if (checkInPayload.getStatus().equals(Constants.StatusCustomerTicket.REJECT)) {
            if (customerTicketMap.getStatus().equals(Constants.StatusCustomerTicket.CHECK_IN)) {
                throw new CustomException(ErrorApp.CUSTOMER_IS_CHECK_IN);
            }
            if (customerTicketMap.isCheckOut()) {
                throw new CustomException(ErrorApp.CUSTOMER_IS_CHECK_OUT);
            }
            if (customerTicketMap.getTicketEntity().getEndTime().isAfter(LocalDateTime.now())) {
                throw new CustomException(ErrorApp.TICKET_IS_EXPIRED_CAN_NOT_REJECT);
            }
            if (customerTicketMap.getTicketEntity().getStartTime().isBefore(LocalDateTime.now().minusHours(1))) {
                throw new CustomException(ErrorApp.TICKET_NOT_START_CAN_NOT_REJECT);
            }
            customerTicketMap.setReasonId(checkInPayload.getReasonId());
            customerTicketMap.setReasonNote(checkInPayload.getReasonNote());
            customerTicketMap.setCheckInTime(LocalDateTime.now());
            customerTicketMap.setStatus(checkInPayload.getStatus());
            customerTicketMapRepository.save(customerTicketMap);
        }
        Ticket ticket = ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId()).orElse(null);

        assert ticket != null;
        auditLogRepository.save(new AuditLog(ticket.getSiteId()
            , Objects.requireNonNull(siteRepository.findById(UUID.fromString(ticket.getSiteId())).orElse(null)).getOrganizationId().toString()
            , customerTicketMap.getId().toString()
            , CUSTOMER_TICKET_TABLE_NAME
            , Constants.AuditType.CREATE
            , null
            , customerTicketMap.toString()));
        var ticketByQRCodeResponseDTO = mapper.map(customerTicketMap, ITicketController.TicketByQRCodeResponseDTO.class);
        ticketByQRCodeResponseDTO.setSiteId(ticket.getSiteId());
        return ticketByQRCodeResponseDTO;
    }

    @Override
    public ITicketController.TicketFilterDTO findByTicketForUser(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new CustomException(ErrorApp.TICKET_NOT_FOUND);
        }
        if (!ticket.getUsername().equals(SecurityUtils.loginUsername())) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        ITicketController.TicketFilterDTO ticketFilterDTO = mapper.map(ticket, ITicketController.TicketFilterDTO.class);
        return ticketFilterDTO;
    }

    @Override
    public ITicketController.TicketFilterDTO findByTicketForAdmin(UUID ticketId, String siteId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ObjectUtils.isEmpty(ticket)) {
            throw new CustomException(ErrorApp.TICKET_NOT_FOUND);
        }
        if (SecurityUtils.getOrgId() != null) {
            if (siteId == null) {
                throw new CustomException(ErrorApp.MUST_TO_CHOOSE_SITE);
            }
            Site site = siteRepository.findById(UUID.fromString(siteId)).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            }
            if (!site.getOrganizationId().equals(SecurityUtils.getOrgId())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
            return mapper.map(ticket, ITicketController.TicketFilterDTO.class);
        } else {
            if (!ticket.getSiteId().equals(SecurityUtils.getSiteId())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
            return mapper.map(ticket, ITicketController.TicketFilterDTO.class);
        }
    }

    @Override
    public Page<CustomerTicketMap> filterTicketAndCustomer(Pageable pageable
        , List<String> sites
        , List<String> names
        , UUID roomId
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
        Page<CustomerTicketMap> customerTicketMaps = customerTicketMapRepository.filter(pageable, sites, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd
            , roomId
            , Constants.StatusCustomerTicket.CHECK_IN
            , purpose
            , keyword);

        return customerTicketMaps;
    }

    @Override
    public boolean addCardCustomerTicket(ITicketController.CustomerTicketCardDTO customerTicketCardDTO) {
        CustomerTicketMap customerTicketMap = customerTicketMapRepository.findByCheckInCodeIgnoreCase(customerTicketCardDTO.getCheckInCode());
        if (customerTicketMap != null) {
            Ticket ticket = ticketRepository.findById(customerTicketMap.getCustomerTicketMapPk().getTicketId()).orElse(null);
            if (!customerTicketMap.getStatus().equals(Constants.StatusCustomerTicket.CHECK_IN)) {
                throw new CustomException(ErrorApp.CUSTOMER_NOT_CHECK_IN_TO_ADD_CARD);
            }
            if (SecurityUtils.getOrgId() != null) {
                if (StringUtils.isEmpty(ticket.getSiteId().trim()))
                    throw new CustomException(ErrorApp.SITE_NOT_FOUND);
                if (!siteRepository.existsByIdAndOrganizationId(UUID.fromString(ticket.getSiteId()), UUID.fromString(SecurityUtils.getOrgId())))
                    throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
                settingUtils.loadSettingsSite(ticket.getSiteId());
            } else {
                settingUtils.loadSettingsSite(SecurityUtils.getSiteId());
            }
            if (!settingUtils.getBoolean(Constants.SettingCode.CONFIGURATION_CARD)) {
                throw new CustomException(ErrorApp.SITE_NOT_USE_CARD);
            }
            if (customerTicketCardDTO.getCardId() == null) {
                throw new CustomException(ErrorApp.CARD_ID_NULL);
            }
            if (customerTicketMapRepository.existsByCardIdAndStatus(customerTicketCardDTO.getCardId(), Constants.StatusCustomerTicket.CHECK_IN)) {
                throw new CustomException(ErrorApp.CARD_IS_EXIST_WITH_CUSTOMER_IN_SITE);
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
        List<Ticket> tickets;
        if (SecurityUtils.getUserDetails().isOrganizationAdmin() || SecurityUtils.getUserDetails().isSiteAdmin()) {
            tickets = filterAllBySite(null, sites, null, null, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, null, null, keyword);
            rooms = roomRepository.filter(null, SecurityUtils.getListSiteToUUID(siteRepository, sites), null, null, null, null, null);
        } else {
            tickets = filterAllBySite(names, null, null, null, status, purpose, createdOnStart, createdOnEnd, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, SecurityUtils.loginUsername(), null, keyword);
            rooms = roomRepository.filter(null, null, null, null, null, null, SecurityUtils.loginUsername());
        }
        ITicketController.TicketByRoomResponseDTO ticketByRoomResponseDTO = new ITicketController.TicketByRoomResponseDTO();
        ticketByRoomResponseDTO.setTickets(tickets);
        ticketByRoomResponseDTO.setRooms(rooms);
        return ticketByRoomResponseDTO;
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
            throw new CustomException(ErrorApp.CUSTOMER_NOT_FOUND);

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
                throw new CustomException(ErrorApp.TEMPLATE_NOT_FOUND);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            String date = ticket.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String startTime = ticket.getStartTime().format(formatter);
            String endTime = ticket.getEndTime().format(formatter);
            String addressSite = site.getCommune().getName() + ", " + site.getDistrict().getName() + ", " + site.getProvince().getName();
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("customerName", customer.getVisitorName() != null ? customer.getVisitorName() : "Updating...");
            parameterMap.put("meetingName", ticket.getName() != null ? ticket.getName() : "Updating...");
            parameterMap.put("dateTime", date);
            parameterMap.put("startTime", startTime);
            parameterMap.put("endTime", endTime);
            String address = site.getAddress() != null ? site.getAddress() + ", " + addressSite : site.getCommune().getName() + ", " + site.getDistrict().getName() + ", " + site.getProvince().getName();
            parameterMap.put("address", address != null ? address : "Updating...");
            String roomName = room != null ? room.getName() : "Updating....";
            parameterMap.put("roomName", roomName != null ? roomName : "Updating...");
            parameterMap.put("staffName", user.getFirstName() + " " + user.getLastName());
            parameterMap.put("staffPhone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "Updating...");
            parameterMap.put("staffEmail", user.getEmail() != null ? user.getEmail() : "Updating...");
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
        customerTicketMap.setStatus(Constants.StatusCustomerTicket.PENDING);
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
}
