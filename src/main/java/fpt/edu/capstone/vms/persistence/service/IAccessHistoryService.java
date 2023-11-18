package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IAccessHistoryService extends IGenericService<Ticket, UUID> {

    Page<IAccessHistoryController.AccessHistoryResponseDTO> accessHistory(
        Pageable pageable, String keyword, Constants.StatusTicket status,
        LocalDateTime formCheckInTime, LocalDateTime toCheckInTime,
        LocalDateTime formCheckOutTime, LocalDateTime toCheckOutTime, List<String> sites
    );

    IAccessHistoryController.AccessHistoryResponseDTO viewAccessHistoryDetail(UUID ticketId, UUID customerId);

    ByteArrayResource export(IAccessHistoryController.AccessHistoryFilter ticketFilterUser) throws JRException;
}
