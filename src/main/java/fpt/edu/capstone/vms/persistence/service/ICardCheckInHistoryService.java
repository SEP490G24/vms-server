package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.CardCheckInHistory;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ICardCheckInHistoryService extends IGenericService<CardCheckInHistory, Integer> {

    boolean checkCard(ICardController.CardCheckDTO cardCheckDTO);

    Page<ITicketController.CardCheckInHistoryDTO> getAllCardHistoryOfCustomer(Pageable pageable, String checkInCode);
}
