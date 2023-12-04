package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IPermissionController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.persistence.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService {

    private final IPermissionResource permissionResource;

    @Override
    public List<IPermissionResource.ModuleDto> findAllModules(boolean fetchPermission) {
        return permissionResource.findAllModules(fetchPermission);
    }

    @Override
    public List<IPermissionResource.PermissionDto> findAllByModuleId(String moduleId) {
        return permissionResource.findAllByModuleId(moduleId);
    }

    @Override
    public List<IPermissionResource.PermissionDto> findAllOrgByModuleId(String moduleId) {
        return permissionResource.findAllOrgByModuleId(moduleId);
    }

    @Override
    public IPermissionResource.PermissionDto findById(String moduleId, String permissionId) {
        return permissionResource.findById(moduleId, permissionId);
    }

    @Override
    public IPermissionResource.PermissionDto create(String moduleId, IPermissionResource.PermissionDto dto) {
        return permissionResource.create(moduleId, dto);
    }

    @Override
    public IPermissionResource.PermissionDto update(String moduleId, String permissionId, IPermissionResource.PermissionDto dto) throws NotFoundException {
        return permissionResource.update(moduleId, permissionId, dto);
    }

    @Override
    public void updateAttribute(String mId, Map<String, List<String>> attributes, List<IPermissionResource.PermissionDto> permissionDtos) {
        permissionResource.updateAttribute(mId, attributes, permissionDtos);
    }

    @Override
    public void delete(String moduleId, String permissionId) {
        permissionResource.delete(moduleId, permissionId);
    }

    @Override
    public List<IPermissionResource.PermissionDto> filter(IPermissionController.PermissionFilterPayload filterPayload) {
        return permissionResource.filter(filterPayload);
    }
}
