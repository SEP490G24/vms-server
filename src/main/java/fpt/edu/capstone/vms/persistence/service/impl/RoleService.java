package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final IRoleResource roleResource;
    private final UserRepository userRepository;

    @Override
    public List<IRoleResource.RoleDto> findAll() {
        return roleResource.findAll();
    }

    @Override
    public Page<IRoleResource.RoleDto> filter(IRoleController.RoleBasePayload roleBasePayload, Pageable pageable) {
        return roleResource.filter(roleBasePayload, pageable);
    }

    @Override
    public List<IRoleResource.RoleDto> filter(IRoleController.RoleBasePayload roleBasePayload) {
        return roleResource.filter(roleBasePayload);
    }


    @Override
    public IRoleResource.RoleDto findById(String id) {
        return roleResource.findById(id);
    }

    @Override
    public IRoleResource.RoleDto create(IRoleResource.RoleDto dto) {
        return roleResource.create(dto);
    }

    @Override
    public IRoleResource.RoleDto update(String id, IRoleResource.RoleDto dto) throws NotFoundException {
        return roleResource.update(id, dto);
    }

    @Override
    public IRoleResource.RoleDto updatePermission(String id, IPermissionResource.PermissionDto permissionDto, boolean state) {
        return roleResource.updatePermission(id, permissionDto, state);
    }

    @Override
    public IRoleResource.RoleDto updatePermissions(String id,List<IPermissionResource.PermissionDto> permissionDto, boolean state) {
        return roleResource.updatePermissions(id, permissionDto, state);
    }

    @Override
    public void delete(String id) {
        List<User> users =  userRepository.findByRole(id);
        if(users.isEmpty()){
            roleResource.delete(id);
        }else {
         throw new CustomException(ErrorApp.ROLE_USED);
        }
    }

    @Override
    public List<IRoleResource.RoleDto> getBySites(List<String> sites) {
        return roleResource.getBySites(sites);
    }
}
