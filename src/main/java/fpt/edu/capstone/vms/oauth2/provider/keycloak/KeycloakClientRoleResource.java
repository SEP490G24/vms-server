package fpt.edu.capstone.vms.oauth2.provider.keycloak;

import fpt.edu.capstone.vms.controller.IPermissionController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fpt.edu.capstone.vms.constants.Constants.IGNORE_CLIENT_ID_KEYCLOAK;


@Slf4j
@Component
public class KeycloakClientRoleResource implements IPermissionResource {

    private final RealmResource realmResource;
    private final ModelMapper mapper;


    public KeycloakClientRoleResource(
        Keycloak keycloak,
        @Value("${edu.fpt.capstone.vms.oauth2.keycloak.realm}") String realm,
        ModelMapper mapper) {
        this.realmResource = keycloak.realm(realm);
        this.mapper = mapper;
    }

    @Override
    public List<ModuleDto> findAllModules(boolean fetchPermission) {
        var clients = this.realmResource.clients().findAll().stream()
            .filter(clientRepresentation -> !Arrays.asList(IGNORE_CLIENT_ID_KEYCLOAK).contains(clientRepresentation.getClientId()))
            .collect(Collectors.toList());
        var modules = (List<ModuleDto>) mapper.map(clients, new TypeToken<List<ModuleDto>>() {
        }.getType());
        if (fetchPermission) modules.forEach(module -> module.setPermissionDtos(findAllByModuleId(module.getId())));
        return modules;
    }

    @Override
    public List<PermissionDto> findAllByModuleId(String cId) {
        var permissions = this.realmResource.clients().get(cId).roles().list(false);
        var permissionDtos = (List<PermissionDto>) mapper.map(permissions, new TypeToken<List<PermissionDto>>() {
        }.getType());

        if (SecurityUtils.getUserDetails().isOrganizationAdmin()) {

            permissionDtos = permissionDtos.stream()
                .filter(permissionDto -> {
                    List<String> scopes = permissionDto.getAttributes().get("scope");
                    return scopes != null && scopes.contains("organization");
                })
                .peek(PermissionDto::initMetadata)
                .collect(Collectors.toList());
        } else if (SecurityUtils.getUserDetails().isSiteAdmin()) {

            permissionDtos = permissionDtos.stream()
                .filter(permissionDto -> {
                    List<String> scopes = permissionDto.getAttributes().get("scope");
                    return scopes != null && scopes.contains("site");
                })
                .peek(PermissionDto::initMetadata)
                .collect(Collectors.toList());
        }else {
            permissionDtos = permissionDtos.stream().peek(PermissionDto::initMetadata)
                .collect(Collectors.toList());
        }
        return permissionDtos;
    }


    @Override
    public List<PermissionDto> findAllOrgByModuleId(String cId) {
        var permissions = this.realmResource.clients().get(cId).roles().list(false);
        var permissionDtos = (List<PermissionDto>) mapper.map(permissions, new TypeToken<List<PermissionDto>>() {
        }.getType());
        var permissionsAdmin = permissionDtos.stream().filter(x -> "scope:organization".equals(x.getName())).findFirst();

        permissionDtos = permissionDtos.stream()
            .filter(permissionDto -> {
                List<String> scopes = permissionDto.getAttributes().get("scope");
                return scopes != null && scopes.contains("organization");
            })
            .peek(PermissionDto::initMetadata)
            .collect(Collectors.toList());

        var permissionsSite = permissionDtos.stream().filter(x -> "scope:site".equals(x.getName())).findFirst();
        if(permissionsAdmin.isPresent()){
            permissionDtos.add(permissionsAdmin.get());
        }
        if(permissionsSite.isPresent()){
            permissionDtos.remove(permissionsSite.get());
        }
        return permissionDtos;
    }

    @Override
    public PermissionDto findById(String cId, String roleName) {
        var permission = this.realmResource.clients().get(cId).roles().get(roleName).toRepresentation();
        return mapper.map(permission, PermissionDto.class).initMetadata();
    }

    @Override
    public PermissionDto create(String cId, PermissionDto value) {
        var permissionInsert = new RoleRepresentation();
        permissionInsert.setName(value.getName());
        permissionInsert.setAttributes(value.getAttributes());
        this.realmResource.clients().get(cId).roles().create(permissionInsert);
        return mapper.map(permissionInsert, PermissionDto.class);
    }

    @Override
    public PermissionDto update(String cId, String permissionName, PermissionDto value) throws NotFoundException {
        var permissionUpdate = this.realmResource.clients().get(cId).roles().get(permissionName);
        if (permissionUpdate == null) throw new NotFoundException();
        var permissionRepresentation = permissionUpdate.toRepresentation();
        if (value.getName() != null) permissionRepresentation.setName(value.getName());
        if (value.getAttributes() != null) permissionRepresentation.setAttributes(value.getAttributes());
        permissionUpdate.update(permissionRepresentation);
        return mapper.map(permissionUpdate, PermissionDto.class);
    }

    @Override
    public void updateAttribute(String cId, Map<String, List<String>> attributes, List<PermissionDto> permissionDtos) {
        var permissionsRepresentation = (List<RoleRepresentation>) mapper.map(permissionDtos, new TypeToken<List<RoleRepresentation>>() {
        }.getType());
        permissionsRepresentation.forEach(roleRepresentation -> attributes.forEach((key, value) -> {
            roleRepresentation.getAttributes().put(key, value);
            this.realmResource.clients().get(cId).roles().get(roleRepresentation.getName()).update(roleRepresentation);
        }));
    }

    @Override
    public void delete(String cId, String roleName) {
        this.realmResource.clients().get(cId).roles().deleteRole(roleName);
    }

    @Override
    public List filter(IPermissionController.PermissionFilterPayload filterPayload) {
        return null;
    }

}
