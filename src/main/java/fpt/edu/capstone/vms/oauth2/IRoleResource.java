package fpt.edu.capstone.vms.oauth2;


import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRoleResource {

    /**
     * Find all role (Keycloak: role is realm role)
     *
     * @return List<RoleDto>
     */
    List<RoleDto> findAll();


    /**
     * Filter role (Keycloak: role is realm role)
     *
     * @return List<RoleDto>
     */
    Page<RoleDto> filter(IRoleController.RoleBasePayload roleBasePayload, Pageable pageable);

    List<RoleDto> filter(IRoleController.RoleBasePayload roleBasePayload);

    /**
     * Find role by id (Keycloak: role is realm role)
     *
     * @param id variable identify for roles (Keycloak: id is roleName).
     * @return RoleDto if exists, null if not exists
     */
    RoleDto findById(String id);

    /**
     * Create role (Keycloak: role is realm role)
     *
     * @param dto DTO for role
     */
    RoleDto create(RoleDto dto);

    /**
     * Update role (Keycloak: role is realm role)
     *
     * @param id variable identify for roles (Keycloak: id is roleName).
     * @throws NotFoundException if not exists
     */
    RoleDto update(String id, RoleDto value) throws NotFoundException;

    /**
     * Update permission for role (Keycloak: role is realm role, permission is client role)
     *
     * @param id            variable identify for roles (Keycloak: id is roleName).
     * @param permissionDto permission data transfer object.
     * @param state         state permission for role.
     */
    RoleDto updatePermission(String id, IPermissionResource.PermissionDto permissionDto, boolean state);
    RoleDto updatePermissions(String id, List<IPermissionResource.PermissionDto> permissionDto, boolean state);

    /**
     * Delete role (Keycloak: role is realm role)
     *
     * @param id variable identify for roles (Keycloak: id is roleName).
     * @throws NotFoundException if not exists
     */
    void delete(String id);

    List<RoleDto> getBySites(List<String> sites);


    @Data
    @Accessors(chain = true)
    class RoleDto {
        private String code;
        private String description;
        private String siteId;
        private String organizationId;
        private Map<String, List<String>> attributes;
        private Set<IPermissionResource.PermissionDto> permissionDtos;
    }

}
