package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoleControllerImpl implements IRoleController {

    private final IRoleService roleService;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> getAll(String siteId) {
        List<String> sites = new ArrayList<>();
        if (!StringUtils.isEmpty(siteId)) {
            sites.add(siteId);
        }
        return ResponseEntity.ok(roleService.getBySites(SecurityUtils.getListSiteToString(siteRepository, sites)));
    }

    @Override
    public ResponseEntity<?> getById(String id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @Override
    public ResponseEntity<?> filter(RoleFilterPayload filterPayload, boolean isPageable, Pageable pageable) {
        if (filterPayload.getAttributes() != null) {
            List<String> sites = filterPayload.getAttributes().get("siteId");
            filterPayload.getAttributes().put("siteId", SecurityUtils.getListSiteToString(siteRepository, sites));
        }
        return isPageable ? ResponseEntity.ok(roleService.filter(filterPayload, pageable)) : ResponseEntity.ok(roleService.filter(filterPayload));
    }

    @Override
    public ResponseEntity<?> create(CreateRolePayload payload) {
        return ResponseEntity.ok(roleService.create(mapper.map(payload, IRoleResource.RoleDto.class)));
    }

    @Override
    public ResponseEntity<?> update(String id, UpdateRolePayload payload) throws NotFoundException {
        return ResponseEntity.ok(roleService.update(id, mapper.map(payload, IRoleResource.RoleDto.class)));
    }

    @Override
    public ResponseEntity<?> updatePermission(String id, UpdateRolePermissionPayload payload) {
        return ResponseEntity.ok(roleService.updatePermission(id, payload.getPermissionDto(), payload.isState()));
    }

    @Override
    public ResponseEntity<?> updatePermissions(String id, UpdateRolePermissionsPayload payload) {
        return ResponseEntity.ok(roleService.updatePermissions(id, payload.getPermissionsDto(), payload.isState()));
    }

    @Override
    public ResponseEntity<?> delete(String id) {
        roleService.delete(id);
        return ResponseEntity.ok().build();
    }
}
