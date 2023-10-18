package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISettingGroupController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.SettingGroup;
import fpt.edu.capstone.vms.persistence.service.ISettingGroupService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@AllArgsConstructor
public class SettingGroupController implements ISettingGroupController {

    private final ISettingGroupService settingGroupService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<SettingGroup> findById(Long id) {
        return ResponseEntity.ok(settingGroupService.findById(id));
    }

    @Override
    public ResponseEntity<SettingGroup> delete(Long id) {
        return settingGroupService.delete(id);
    }

    @Override
    public ResponseEntity<?> updateSettingGroup(Long id, UpdateSettingGroupInfo settingGroupInfo) {
        try {
            var settingGroup = settingGroupService.update(mapper.map(settingGroupInfo, SettingGroup.class), id);
            return ResponseEntity.ok(settingGroup);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(settingGroupService.findAll());
    }

    @Override
    public ResponseEntity<?> createSettingGroup(CreateSettingGroupInfo settingGroupInfo) {
        var settingGroup = settingGroupService.save(mapper.map(settingGroupInfo, SettingGroup.class));
        return ResponseEntity.ok(settingGroup);
    }


}
