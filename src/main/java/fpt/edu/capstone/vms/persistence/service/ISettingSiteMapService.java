package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.List;

public interface ISettingSiteMapService extends IGenericService<SettingSiteMap, SettingSiteMapPk> {

    SettingSiteMap createOrUpdateSettingSiteMap(ISettingSiteMapController.SettingSiteInfo settingSiteInfo);

    ISettingSiteMapController.SettingSiteDTO findAllBySiteIdAndGroupId(Integer settingGroupId, List<String> sites);

    Boolean setDefaultValueBySite(String siteId);

    ISettingSiteMapController.SettingSiteMapDTO findBySiteIdAndCode(String siteId, String code);

}

