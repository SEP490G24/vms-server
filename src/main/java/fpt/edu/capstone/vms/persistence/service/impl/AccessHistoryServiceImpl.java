package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.*;
import fpt.edu.capstone.vms.persistence.service.IAccessHistoryService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.EmailUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessHistoryServiceImpl extends GenericServiceImpl<Ticket, UUID> implements IAccessHistoryService {

    final ModelMapper mapper;
    final TicketRepository ticketRepository;
    final RoomRepository roomRepository;
    final TemplateRepository templateRepository;
    final CustomerRepository customerRepository;
    final SiteRepository siteRepository;
    final OrganizationRepository organizationRepository;
    final CustomerTicketMapRepository customerTicketMapRepository;
    final EmailUtils emailUtils;
    final AuditLogServiceImpl auditLogService;
    final AuditLogRepository auditLogRepository;
    private static final String TICKET_TABLE_NAME = "Ticket";
    private static final String CUSTOMER_TICKET_TABLE_NAME = "CustomerTicketMap";


    public AccessHistoryServiceImpl(TicketRepository ticketRepository, CustomerRepository customerRepository,
                                    TemplateRepository templateRepository, ModelMapper mapper, RoomRepository roomRepository,
                                    SiteRepository siteRepository, OrganizationRepository organizationRepository,
                                    CustomerTicketMapRepository customerTicketMapRepository, EmailUtils emailUtils, AuditLogServiceImpl auditLogService, AuditLogRepository auditLogRepository) {
        this.ticketRepository = ticketRepository;
        this.templateRepository = templateRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.roomRepository = roomRepository;
        this.siteRepository = siteRepository;
        this.organizationRepository = organizationRepository;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.emailUtils = emailUtils;
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
        this.init(ticketRepository);
    }


    @Override
    public Page<IAccessHistoryController.AccessHistoryResponseDTO> accessHistory(Pageable pageable, String keyword, Constants.StatusTicket status,
                                                                                 LocalDateTime formCheckInTime, LocalDateTime toCheckInTime,
                                                                                 LocalDateTime formCheckOutTime, LocalDateTime toCheckOutTime, String site) {

        List<String> sites = getListSite(site);
        Page<CustomerTicketMap> customerTicketMapPage = customerTicketMapRepository.accessHistory(pageable, sites, formCheckInTime, toCheckInTime, formCheckOutTime, toCheckOutTime, status, keyword);
        List<IAccessHistoryController.AccessHistoryResponseDTO> accessHistoryResponseDTOS = mapper.map(customerTicketMapPage.getContent(), new TypeToken<List<IAccessHistoryController.AccessHistoryResponseDTO>>() {
        }.getType());
        return new PageImpl(accessHistoryResponseDTOS, pageable, accessHistoryResponseDTOS.size());
    }

    private List<String> getListSite(String site) {
        List<String> sites = new ArrayList<>();
        if (site != null) {
            sites.add(site);
        } else {
            if (SecurityUtils.getOrgId() != null) {
                siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId())).forEach(o ->
                    sites.add(o.getId().toString()));
            } else {
                sites.add(UUID.fromString(SecurityUtils.getSiteId()).toString());
            }
        }
        return sites;
    }
}
