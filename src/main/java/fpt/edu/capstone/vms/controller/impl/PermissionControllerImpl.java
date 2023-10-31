package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IPermissionController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.persistence.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PermissionControllerImpl implements IPermissionController {

    private final IPermissionService permissionService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> getAllModule(boolean fetchPermission) {
        return ResponseEntity.ok(permissionService.findAllModules(fetchPermission));
    }

    @Override
    public ResponseEntity<?> getAllByModuleId(String mId) {
        return ResponseEntity.ok(permissionService.findAllByModuleId(mId));
    }

    @Override
    public ResponseEntity<?> getByIdAndModuleId(String mId, String pId) {
        return ResponseEntity.ok(permissionService.findById(mId, pId));
    }

    @Override
    public ResponseEntity<?> filter(PermissionFilterPayload filterPayload, Pageable pageable) {
        var permissions = permissionService.filter(filterPayload);
        return ResponseEntity.ok(new PageImpl(permissions, pageable, permissions.size()));
    }

    @Override
    public ResponseEntity<?> create(String mId, CreatePermissionPayload payload) {
        return ResponseEntity.ok(permissionService.create(mId, mapper.map(payload, IPermissionResource.PermissionDto.class)));
    }

    @Override
    public ResponseEntity<?> update(String mId, String pId, UpdatePermissionPayload payload) throws NotFoundException {
        return ResponseEntity.ok(permissionService.update(mId, pId, mapper.map(payload, IPermissionResource.PermissionDto.class)));
    }

    @Override
    public ResponseEntity<?> updateAttribute(String mId, UpdateAttributePermissionPayload payload) {
        permissionService.updateAttribute(mId, payload.getAttributes(), payload.getPermissionDtos());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> delete(String mId, String pId) {
        permissionService.delete(mId, pId);
        return ResponseEntity.ok().build();
    }
}
