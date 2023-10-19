package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoleControllerImpl implements IRoleController {

    private final IRoleService roleService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @Override
    public ResponseEntity<?> getById(String id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @Override
    public ResponseEntity<?> filter(RoleFilterPayload filterPayload) {
        return ResponseEntity.ok(roleService.filter(filterPayload));
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
    public ResponseEntity<?> delete(String id) {
        roleService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getBySites(List<String> sites) {
        return ResponseEntity.ok(roleService.getBySites(sites));
    }
}
