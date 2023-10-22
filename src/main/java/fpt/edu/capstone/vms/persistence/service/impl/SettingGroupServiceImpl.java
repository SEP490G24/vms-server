package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.SettingGroup;
import fpt.edu.capstone.vms.persistence.repository.SettingGroupRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingGroupService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class SettingGroupServiceImpl extends GenericServiceImpl<SettingGroup, Long> implements ISettingGroupService {

    private final SettingGroupRepository settingGroupRepository;

    public SettingGroupServiceImpl(SettingGroupRepository settingGroupRepository) {
        this.settingGroupRepository = settingGroupRepository;
        this.init(settingGroupRepository);
    }

    /**
     * The function updates a setting group entity with the provided data and returns the updated entity.
     *
     * @param entity The "entity" parameter is an instance of the SettingGroup class that contains the updated values for a
     * specific setting group.
     * @param id The "id" parameter is the unique identifier of the setting group that needs to be updated.
     * @return The method is returning a SettingGroup object.
     */
    @Override
    public SettingGroup update(SettingGroup entity, Long id) {

        if (ObjectUtils.isEmpty(entity)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is empty");
        var settingGroup = settingGroupRepository.findById(id).orElse(null);

        if (ObjectUtils.isEmpty(settingGroup))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found setting group by id: " + id);

        return settingGroupRepository.save(settingGroup.update(entity));
    }

}
