package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.IAccessHistoryService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessHistoryServiceImpl extends GenericServiceImpl<Ticket, UUID> implements IAccessHistoryService {

    final ModelMapper mapper;
    final SiteRepository siteRepository;
    final CustomerTicketMapRepository customerTicketMapRepository;


    public AccessHistoryServiceImpl(TicketRepository ticketRepository,
                                    ModelMapper mapper, SiteRepository siteRepository,
                                    CustomerTicketMapRepository customerTicketMapRepository) {
        this.mapper = mapper;
        this.siteRepository = siteRepository;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.init(ticketRepository);
    }


    @Override
    public Page<CustomerTicketMap> accessHistory(Pageable pageable, String keyword, List<Constants.StatusCustomerTicket> status,
                                                 LocalDateTime formCheckInTime, LocalDateTime toCheckInTime,
                                                 LocalDateTime formCheckOutTime, LocalDateTime toCheckOutTime, List<String> sites) {
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        if (CollectionUtils.isEmpty(status)) {
            status = List.of(Constants.StatusCustomerTicket.CHECK_IN, Constants.StatusCustomerTicket.CHECK_OUT);
            Page<CustomerTicketMap> customerTicketMapPage = customerTicketMapRepository.accessHistory(pageableSort, SecurityUtils.getListSiteToString(siteRepository, sites), formCheckInTime, toCheckInTime, formCheckOutTime, toCheckOutTime, status, keyword != null ? keyword.toUpperCase() : null, null);
            return customerTicketMapPage;
        } else {
            if (status.contains(Constants.StatusCustomerTicket.CHECK_IN) || status.contains(Constants.StatusCustomerTicket.CHECK_OUT)
            ) {
                Page<CustomerTicketMap> customerTicketMapPage = customerTicketMapRepository.accessHistory(pageableSort, SecurityUtils.getListSiteToString(siteRepository, sites), formCheckInTime, toCheckInTime, formCheckOutTime, toCheckOutTime, status, keyword != null ? keyword.toUpperCase() : null, null);
                return customerTicketMapPage;
            } else {
                return null;
            }
        }
    }

    @Override
    public IAccessHistoryController.AccessHistoryResponseDTO viewAccessHistoryDetail(String checkInCode) {
        var customerTicketMap = customerTicketMapRepository.findByCheckInCode(checkInCode);
        return mapper.map(customerTicketMap, IAccessHistoryController.AccessHistoryResponseDTO.class);
    }
}
