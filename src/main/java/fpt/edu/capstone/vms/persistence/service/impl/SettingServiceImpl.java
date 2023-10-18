package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SettingServiceImpl extends GenericServiceImpl<Setting, Long> implements ISettingService {

    private final SettingRepository settingRepository;

    public SettingServiceImpl(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
        this.init(settingRepository);
    }

}
