package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISiteDepartmentMapController;
import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMap;
import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMapPk;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.service.impl.SiteDepartmentMapServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SiteDepartmentMapController implements ISiteDepartmentMapController {
    private final SiteDepartmentMapServiceImpl siteDepartmentMapService;

    @Override
    public ResponseEntity<SiteDepartmentMap> findById(UUID departmentId, UUID siteId) {
        SiteDepartmentMapPk pk = new SiteDepartmentMapPk(departmentId, siteId);
        return ResponseEntity.ok(siteDepartmentMapService.findById(pk));
    }

    @Override
    public ResponseEntity<SiteDepartmentMap> delete(UUID departmentId, UUID siteId) {
        SiteDepartmentMapPk pk = new SiteDepartmentMapPk(departmentId, siteId);
        return siteDepartmentMapService.delete(pk);
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(siteDepartmentMapService.findAll());
    }

    @Override
    public ResponseEntity<?> createTicket(createSiteDepartmentMapInfo siteDepartmentMapInfo) {
        SiteDepartmentMapPk pk = new SiteDepartmentMapPk(siteDepartmentMapInfo.getDepartmentId(), siteDepartmentMapInfo.getSiteId());
        SiteDepartmentMap siteDepartmentMap = new SiteDepartmentMap();
        siteDepartmentMap.setId(pk);
        return ResponseEntity.ok(siteDepartmentMapService.save(siteDepartmentMap));
    }

}
