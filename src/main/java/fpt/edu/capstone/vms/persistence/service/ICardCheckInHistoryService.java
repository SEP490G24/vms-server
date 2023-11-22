package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.persistence.entity.CardCheckInHistory;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;


public interface ICardCheckInHistoryService extends IGenericService<CardCheckInHistory, Integer> {

    boolean checkCard(ICardController.CardCheckDTO cardCheckDTO);
}
