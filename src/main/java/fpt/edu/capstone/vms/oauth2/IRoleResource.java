package fpt.edu.capstone.vms.oauth2;


import fpt.edu.capstone.vms.exception.NotFoundException;
import lombok.Data;
import lombok.experimental.Accessors;

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

    /**
     * Delete role (Keycloak: role is realm role)
     *
     * @param id variable identify for roles (Keycloak: id is roleName).
     * @throws NotFoundException if not exists
     */
    void delete(String id);


    @Data
    @Accessors(chain = true)
    class RoleDto {
        private String name;
        private Map<String, List<String>> attributes;
        private Set<IPermissionResource.PermissionDto> permissionDtos;
    }

}
