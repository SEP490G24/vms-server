package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.repository.SettingRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
public class SettingServiceImpl extends GenericServiceImpl<Setting, Long> implements ISettingService {

    private final SettingRepository settingRepository;

    public SettingServiceImpl(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
        this.init(settingRepository);
    }

    @Override
    public Setting update(Setting entity, Long id) {
        if (!StringUtils.isEmpty(entity.getCode())) {
            if (settingRepository.existsByCode(entity.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code is exist");
            }
        }

        if (ObjectUtils.isEmpty(entity)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is empty");
        var settingEntity = settingRepository.findById(id).orElse(null);

        if (ObjectUtils.isEmpty(settingEntity))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found setting by id: " + id);

        return settingRepository.save(settingEntity.update(entity));
    }
}
