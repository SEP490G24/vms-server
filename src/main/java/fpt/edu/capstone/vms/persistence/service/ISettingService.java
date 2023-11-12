package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.List;


public interface ISettingService extends IGenericService<Setting, Long> {

    List<Setting> findAllByGroupIdAndSiteId(Integer groupId, String siteId);
}
