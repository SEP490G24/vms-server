package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.CardCheckInHistory;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.List;


public interface ICardCheckInHistoryService extends IGenericService<CardCheckInHistory, Integer> {

    boolean checkCard(ICardController.CardCheckDTO cardCheckDTO);

    List<ITicketController.CardCheckInHistoryDTO> getAllCardHistoryOfCustomer(String checkInCode);
}
