package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.SettingGroup;
import fpt.edu.capstone.vms.persistence.repository.SettingGroupRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingGroupService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SettingGroupServiceImpl extends GenericServiceImpl<SettingGroup, Long> implements ISettingGroupService {

    private final SettingGroupRepository settingGroupRepository;

    public SettingGroupServiceImpl(SettingGroupRepository settingGroupRepository) {
        this.settingGroupRepository = settingGroupRepository;
        this.init(settingGroupRepository);
    }

}
