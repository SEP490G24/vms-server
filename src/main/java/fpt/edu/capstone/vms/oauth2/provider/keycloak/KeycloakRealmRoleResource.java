package fpt.edu.capstone.vms.oauth2.provider.keycloak;

import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.entity.Site;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static fpt.edu.capstone.vms.constants.Constants.IGNORE_ROLE_REALM_KEYCLOAK;


@Slf4j
@Component
public class KeycloakRealmRoleResource implements IRoleResource {

    private final RolesResource rolesResource;
    private final ModelMapper mapper;

//    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.ignore-default-roles}")
//    private String[] ignoreDefaultRoles;

    public KeycloakRealmRoleResource(
            Keycloak keycloak,
            @Value("${edu.fpt.capstone.vms.oauth2.keycloak.realm}") String realm,
            ModelMapper mapper) {
        this.rolesResource = keycloak.realm(realm).roles();
        this.mapper = mapper;
    }


    @Override
    public List<RoleDto> findAll() {
        /* fetch all role */
        var roles = this.rolesResource.list().stream()
            .filter(roleRepresentation -> !Arrays.asList(IGNORE_ROLE_REALM_KEYCLOAK).contains(roleRepresentation.getName()))
            .toList();
        var results = (List<RoleDto>) mapper.map(roles, new TypeToken<List<RoleDto>>() {
        }.getType());

        /* set permission for role */
        results.forEach(this::updatePermission4Role);

        return results;
    }

    @Override
    public List<RoleDto> filter(IRoleController.RoleBasePayload roleBasePayload) {

        // Get the list of all roles
        List<RoleRepresentation> roles = this.rolesResource.list(false);

        var role = roles.stream()
            .filter(roleRepresentation -> {

                if (roleBasePayload.getName().contains(roleRepresentation.getName()) && roleRepresentation.getAttributes() != null) {
                    List<String> siteIds = roleRepresentation.getAttributes().get("site_id");
                    return siteIds != null && siteIds.contains(roleBasePayload.getAttributes().get("site_id").get(0));
                }
                return false;
            }).toList();


        // Chuyển đổi danh sách đã lọc thành danh sách RoleDto
        var results = (List<RoleDto>) mapper.map(role, new TypeToken<List<RoleDto>>() {
        }.getType());

        // Set quyền cho từng vai trò
        results.forEach(this::updatePermission4Role);

        return results;
    }

    @Override
    public RoleDto findById(String roleName) {
        var role = this.rolesResource.get(roleName);
        var roleRepresentation = role.toRepresentation();
        var roleDto = mapper.map(roleRepresentation, RoleDto.class);
        updatePermission4Role(roleDto);
        return roleDto;
    }

    @Override
    public RoleDto create(Site site, RoleDto value) {
        var roleInsert = new RoleRepresentation();
        roleInsert.setName(site.getCode() + "_" + value.getName());
        value.getAttributes().put("site_id", List.of(site.getId().toString()));
        roleInsert.setAttributes(value.getAttributes());
        this.rolesResource.create(roleInsert);
        return mapper.map(roleInsert, RoleDto.class);
    }

    @Override
    public RoleDto update(String roleName, RoleDto value) throws NotFoundException {
        var roleUpdate = this.rolesResource.get(roleName);
        if (roleUpdate == null) throw new NotFoundException();
        roleUpdate.toRepresentation().setName(value.getName());
        roleUpdate.toRepresentation().setAttributes(value.getAttributes());
        roleUpdate.update(roleUpdate.toRepresentation());
        return mapper.map(roleUpdate, RoleDto.class);
    }

    @Override
    public RoleDto updatePermission(String roleName, IPermissionResource.PermissionDto permissionDto, boolean state) {
        var roleUpdate = this.rolesResource.get(roleName);
        if (state) roleUpdate.addComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        else roleUpdate.deleteComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        return mapper.map(roleUpdate, RoleDto.class);
    }

    @Override
    public void delete(String roleName) {
        this.rolesResource.deleteRole(roleName);
    }

    private void updatePermission4Role(RoleDto role) {
        var roleResource = this.rolesResource.get(role.getName());
        role.setPermissionDtos(mapper.map(roleResource.getRoleComposites(), new TypeToken<Set<IPermissionResource.PermissionDto>>() {}.getType()));
    }

}
