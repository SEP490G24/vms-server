package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;

import java.util.List;
import java.util.UUID;



public interface SettingSiteMapRepositoryCustomer {
    List<ISettingSiteMapController.SettingSiteDTO> findAllBySiteIdAndGroupId(String siteId, Integer settingGroupId);
}
