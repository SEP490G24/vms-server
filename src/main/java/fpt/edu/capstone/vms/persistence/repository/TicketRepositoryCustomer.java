package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.controller.ITicketController;

import java.util.List;


public interface TicketRepositoryCustomer {
    List<ITicketController.TicketFilterDTO> findAllBySiteIdAndGroupId(String siteId, Integer settingGroupId);
}
