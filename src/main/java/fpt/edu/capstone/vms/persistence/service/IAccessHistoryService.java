package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IAccessHistoryService extends IGenericService<Ticket, UUID> {

    Page<CustomerTicketMap> accessHistory(
        Pageable pageable, String keyword, List<Constants.StatusCustomerTicket> status,
        LocalDateTime formCheckInTime, LocalDateTime toCheckInTime,
        LocalDateTime formCheckOutTime, LocalDateTime toCheckOutTime, List<String> sites
    );

    IAccessHistoryController.AccessHistoryResponseDTO viewAccessHistoryDetail(String checkInCode);
}
