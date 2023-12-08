package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.constants.I18n;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.SettingGroup;
import fpt.edu.capstone.vms.persistence.repository.SettingGroupRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingGroupService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

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

        if (ObjectUtils.isEmpty(entity)) throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);
        var settingGroup = settingGroupRepository.findById(id).orElse(null);

        if (ObjectUtils.isEmpty(settingGroup))
            throw new CustomException(ErrorApp.SETTING_GROUP_NOT_FOUND);

        return settingGroupRepository.save(settingGroup.update(entity));
    }

    @Override
    public List<SettingGroup> findAll() {
        var settingGroups = settingGroupRepository.findAll();
        settingGroups.stream().map(settingGroup ->
            settingGroup.setName(I18n.getMessage(settingGroup.getCode()))).collect(Collectors.toList());
        return settingGroups;
    }
}
