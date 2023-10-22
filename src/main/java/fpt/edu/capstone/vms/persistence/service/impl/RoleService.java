package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final IRoleResource roleResource;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    @Override
    public List<IRoleResource.RoleDto> findAll() {
        return roleResource.findAll();
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
        Site site = siteRepository.findById(UUID.fromString(dto.getSiteId())).orElse(null);
        if (ObjectUtils.isEmpty(site)) throw new CustomException(ErrorApp.BAD_REQUEST);
        return roleResource.create(site, dto);
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
    public void delete(String id) {
        roleResource.delete(id);
    }

    @Override
    public List<IRoleResource.RoleDto> getBySites(List<String> sites) {
        return roleResource.getBySites(sites);
    }
}
