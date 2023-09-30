package fpt.edu.capstone.vms.persistence.service;



import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;

import java.util.List;


public interface IRoleService {
    List<IRoleResource.RoleDto> findAll();

    IRoleResource.RoleDto findById(String id);

    IRoleResource.RoleDto create(IRoleResource.RoleDto dto);

    IRoleResource.RoleDto update(String id, IRoleResource.RoleDto dto) throws NotFoundException;

    IRoleResource.RoleDto updatePermission(String id, IPermissionResource.PermissionDto permissionDto, boolean state);

    void delete(String id);

}
