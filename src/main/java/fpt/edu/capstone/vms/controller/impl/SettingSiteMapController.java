package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.service.ISettingSiteMapService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SettingSiteMapController implements ISettingSiteMapController {

    private final ISettingSiteMapService settingSiteService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<SettingSiteMap> findById( String siteId, Long settingId) {
        SettingSiteMapPk pk = new SettingSiteMapPk(settingId, UUID.fromString(siteId));
        return ResponseEntity.ok(settingSiteService.findById(pk));
    }

    @Override
    public ResponseEntity<SettingSiteMap> delete(String siteId, Long settingId) {
        SettingSiteMapPk pk = new SettingSiteMapPk(settingId, UUID.fromString(siteId));
        return settingSiteService.delete(pk);
    }

    @Override
    public ResponseEntity<?> createOrUpdateSettingSiteMap(SettingSiteInfo settingSiteInfo) {
        try {
            return ResponseEntity.ok(settingSiteService.createOrUpdateSettingSiteMap(settingSiteInfo));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(settingSiteService.findAll());
    }

    @Override
    public ResponseEntity<List<?>> findAllBySiteId(String siteId) {
        return ResponseEntity.ok(settingSiteService.getAllSettingSiteBySiteId(siteId));
    }

}
