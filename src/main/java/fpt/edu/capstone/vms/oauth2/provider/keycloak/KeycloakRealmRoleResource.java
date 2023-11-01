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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class KeycloakRealmRoleResource implements IRoleResource {

    private final RolesResource rolesResource;
    private final ModelMapper mapper;

    private final String[] ignoreDefaultRoles;

    public KeycloakRealmRoleResource(
            Keycloak keycloak,
            @Value("${edu.fpt.capstone.vms.oauth2.keycloak.realm}") String realm,
            @Value("${edu.fpt.capstone.vms.oauth2.keycloak.ignore-default-roles}") String [] ignoreDefaultRoles,
            ModelMapper mapper) {
        this.ignoreDefaultRoles = ignoreDefaultRoles;
        this.rolesResource = keycloak.realm(realm).roles();
        this.mapper = mapper;
    }


    @Override
    public List<RoleDto> findAll() {
        /* fetch all role */
        var roles = this.rolesResource.list(false).stream()
            .filter(roleRepresentation -> !Arrays.asList(ignoreDefaultRoles).contains(roleRepresentation.getName()))
            .toList();
        var results = (List<RoleDto>) mapper.map(roles, new TypeToken<List<RoleDto>>() {
        }.getType());

        /* set permission for role */
        results.forEach(this::updatePermission4Role);

        return results;
    }

    @Override
    public Page<RoleDto> filter(IRoleController.RoleBasePayload roleBasePayload, Pageable pageable) {
        List<RoleRepresentation> roles = this.rolesResource.list(false);

        var filteredRoles = roles.stream()
            .filter(roleRepresentation -> {
                if (roleBasePayload.getCode() == null || roleRepresentation.getName().contains(roleBasePayload.getCode())) {
                    List<String> siteIds = roleBasePayload.getAttributes() != null ? roleBasePayload.getAttributes().get("site_id") : null;
                    List<String> names = roleBasePayload.getAttributes() != null ? roleBasePayload.getAttributes().get("name") : null;
                    return (siteIds == null || siteIds.isEmpty() ||
                        siteIds.stream().anyMatch(siteId ->
                            roleRepresentation.getAttributes().get("site_id") != null &&
                                roleRepresentation.getAttributes().get("site_id").contains(siteId)))
                        && (names == null || names.isEmpty() ||
                        names.stream().anyMatch(name ->
                            roleRepresentation.getAttributes().get("name") != null &&
                                roleRepresentation.getAttributes().get("name").get(0).contains(name)));
                }
                return false;
            })
            .toList();

        var results = (List<RoleDto>) mapper.map(filteredRoles, new TypeToken<List<RoleDto>>() {
        }.getType());

        results.forEach(this::updatePermission4Role);

        return new PageImpl(results,pageable,results.size());
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
        roleInsert.setName(site.getCode() + "_" + value.getCode());
        roleInsert.setAttributes(value.getAttributes());
        roleInsert.setDescription(value.getDescription());
        this.rolesResource.create(roleInsert);
        return mapper.map(roleInsert, RoleDto.class);
    }

    @Override
    public RoleDto update(String roleCode, RoleDto value) throws NotFoundException {
        var roleUpdate = this.rolesResource.get(roleCode);
        if (roleUpdate == null) throw new NotFoundException();
        var role = roleUpdate.toRepresentation();
        role.setAttributes(value.getAttributes());
        role.setDescription(value.getDescription());
        roleUpdate.update(role);
        return mapper.map(role, RoleDto.class);
    }

    @Override
    public RoleDto updatePermission(String roleCode, IPermissionResource.PermissionDto permissionDto, boolean state) {
        var roleUpdate = this.rolesResource.get(roleCode);
        if (state)
            roleUpdate.addComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        else
            roleUpdate.deleteComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        return mapper.map(roleUpdate, RoleDto.class);
    }

    @Override
    public void delete(String roleCode) {
        this.rolesResource.deleteRole(roleCode);
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
        var roleResource = this.rolesResource.get(role.getCode());
        role.setPermissionDtos(mapper.map(roleResource.getRoleComposites(), new TypeToken<Set<IPermissionResource.PermissionDto>>() {
        }.getType()));
    }

}
