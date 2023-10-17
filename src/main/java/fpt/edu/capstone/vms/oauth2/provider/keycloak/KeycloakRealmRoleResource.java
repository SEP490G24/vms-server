package fpt.edu.capstone.vms.oauth2.provider.keycloak;

import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        List<RoleRepresentation> roles = this.rolesResource.list();

        // Create a Predicate to check the name
        Predicate<RoleRepresentation> namePredicate = roleRepresentation -> {
            if (roleBasePayload.getName() == null || roleBasePayload.getName().isEmpty()) {
                return true; // Do not filter by name if 'name' is null or empty
            }
            return roleRepresentation.getName().equals(roleBasePayload.getName());
        };

        // Create a Predicate to check the attributes
        Predicate<RoleRepresentation> attributesPredicate = roleRepresentation -> {
            Map<String, List<String>> roleAttributes = roleRepresentation.getAttributes();
            Map<String, List<String>> payloadAttributes = roleBasePayload.getAttributes();

            if (payloadAttributes == null || payloadAttributes.isEmpty()) {
                return true; // Do not filter by attributes if 'attributes' is null or empty
            }

            // Check if roleAttributes is null
            if (roleAttributes == null) {
                return false;
            }

            // Check if roleAttributes contains all attributes from payloadAttributes
            return payloadAttributes.entrySet().stream()
                .allMatch(payloadEntry -> {
                    String attributeName = payloadEntry.getKey();
                    List<String> attributeValues = payloadEntry.getValue();

                    if (roleAttributes.containsKey(attributeName)) {
                        List<String> roleAttributeValues = roleAttributes.get(attributeName);
                        return roleAttributeValues.containsAll(attributeValues);
                    }

                    return false;
                });
        };

        // Apply the Predicates to filter the list of roles
        List<RoleRepresentation> filteredRoles = roles.stream()
            .filter(namePredicate.and(attributesPredicate))
            .collect(Collectors.toList());

        // Chuyển đổi danh sách đã lọc thành danh sách RoleDto
        var results = (List<RoleDto>) mapper.map(roles, new TypeToken<List<RoleDto>>() {
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
    public RoleDto create(RoleDto value) {
        var roleInsert = new RoleRepresentation();
        roleInsert.setName(value.getName());
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
