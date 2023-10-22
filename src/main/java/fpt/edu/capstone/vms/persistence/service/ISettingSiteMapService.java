package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.List;
import java.util.UUID;

public interface ISettingSiteMapService extends IGenericService<SettingSiteMap, SettingSiteMapPk> {

    SettingSiteMap createOrUpdateSettingSiteMap(ISettingSiteMapController.SettingSiteInfo settingSiteInfo);

    List<SettingSiteMap> getAllSettingSiteBySiteId(String siteId);

    List<ISettingSiteMapController.SettingSiteDTO> findAllBySiteIdAndGroupId(String siteId, Integer settingGroupId);

}

