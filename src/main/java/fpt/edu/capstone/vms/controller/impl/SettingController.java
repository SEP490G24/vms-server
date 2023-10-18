package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISettingController;
import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.service.ISettingService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class SettingController implements ISettingController {

    private final ISettingService settingService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<Setting> findById(Long id) {
        return ResponseEntity.ok(settingService.findById(id));
    }

    @Override
    public ResponseEntity<Setting> delete(Long id) {
        return settingService.delete(id);
    }

    @Override
    public ResponseEntity<?> update(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(settingService.findAll());
    }

    @Override
    public ResponseEntity<?> createSetting(CreateSettingInfo settingSiteInfo) {
        var ticket = settingService.save(mapper.map(settingSiteInfo, Setting.class));
        return ResponseEntity.ok(ticket);
    }
}
