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

        List<RoleRepresentation> roles = this.rolesResource.list(false);

        var role = roles.stream()
            .filter(roleRepresentation -> {

                if (roleRepresentation.getName().toUpperCase().contains(roleBasePayload.getName().toUpperCase())) {
                    return true;
                }
                return false;
            }).toList();

        var results = (List<RoleDto>) mapper.map(role, new TypeToken<List<RoleDto>>() {
        }.getType());

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
        roleInsert.setDescription(value.getDescription());
        this.rolesResource.create(roleInsert);
        return mapper.map(roleInsert, RoleDto.class);
    }

    @Override
    public RoleDto update(String roleName, RoleDto value) throws NotFoundException {
        var roleUpdate = this.rolesResource.get(roleName);
        if (roleUpdate == null) throw new NotFoundException();
        var role = roleUpdate.toRepresentation();
        role.setAttributes(value.getAttributes());
        role.setDescription(value.getDescription());
        roleUpdate.update(role);
        return mapper.map(role, RoleDto.class);
    }

    @Override
    public RoleDto updatePermission(String roleName, IPermissionResource.PermissionDto permissionDto, boolean state) {
        var roleUpdate = this.rolesResource.get(roleName);
        if (state)
            roleUpdate.addComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        else
            roleUpdate.deleteComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        return mapper.map(roleUpdate, RoleDto.class);
    }

    @Override
    public void delete(String roleName) {
        this.rolesResource.deleteRole(roleName);
    }

    @Override
    public List<RoleDto> getBySites(List<String> sites) {
        List<RoleRepresentation> roles = this.rolesResource.list(false);

        var role = roles.stream()
            .filter(roleRepresentation -> {

                if (roleRepresentation.getAttributes() != null && roleRepresentation.getAttributes().get("site_id") != null) {
                    String siteId = roleRepresentation.getAttributes().get("site_id").get(0);
                    return sites.contains(siteId);
                }
                return false;
            }).toList();

        var results = (List<RoleDto>) mapper.map(role, new TypeToken<List<RoleDto>>() {
        }.getType());

        results.forEach(this::updatePermission4Role);

        return results;
    }

    private void updatePermission4Role(RoleDto role) {
        var roleResource = this.rolesResource.get(role.getName());
        role.setPermissionDtos(mapper.map(roleResource.getRoleComposites(), new TypeToken<Set<IPermissionResource.PermissionDto>>() {
        }.getType()));
    }

}
