package fpt.edu.capstone.vms.persistence.service;


import fpt.edu.capstone.vms.controller.IPermissionController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;

import java.util.List;
import java.util.Map;

public interface IPermissionService {
    List<IPermissionResource.ModuleDto> findAllModules(boolean fetchPermission);

    List<IPermissionResource.PermissionDto> findAllByModuleId(String moduleId);
    List<IPermissionResource.PermissionDto> findAllOrgByModuleId(String moduleId);

    IPermissionResource.PermissionDto findById(String moduleId, String permissionId);

    IPermissionResource.PermissionDto create(String moduleId, IPermissionResource.PermissionDto dto);

    IPermissionResource.PermissionDto update(String moduleId, String permissionId, IPermissionResource.PermissionDto dto) throws NotFoundException;

    void updateAttribute(String mId, Map<String, List<String>> attributes, List<IPermissionResource.PermissionDto> permissionDtos);

    void delete(String moduleId, String permissionId);


    List<IPermissionResource.PermissionDto> filter(IPermissionController.PermissionFilterPayload filterPayload);
}
