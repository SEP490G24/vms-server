package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;

import java.util.List;


public interface SettingSiteMapRepositoryCustomer {
    List<ISettingSiteMapController.SettingSite> findAllBySiteIdAndGroupId(String siteId, Integer settingGroupId);
}
