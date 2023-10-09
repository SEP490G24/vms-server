package fpt.edu.capstone.vms.keycloak.sync.helper;

import fpt.edu.capstone.vms.keycloak.sync.mapper.KeycloakRoleMapper;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleExtractResults;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleSyncResults;
import fpt.edu.capstone.vms.util.JacksonUtils;
import fpt.edu.capstone.vms.util.KeycloakUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class KeyCloakRoleSyncHelper {

    private static final Logger log = LoggerFactory.getLogger(KeyCloakRoleSyncHelper.class);

    private final RealmResource realmResource;

    public KeyCloakRoleSyncHelper(Keycloak keycloak, String realm) {
        this.realmResource = keycloak.realm(realm);
    }


    public KeycloakRoleSyncResults startToSync(File settingFile, String clientId){
        return execute(Objects.requireNonNull(JacksonUtils.getObject(settingFile, KeycloakRoleExtractResults.class)), clientId,false);
    }

    public KeycloakRoleSyncResults startToSync(File settingFile, String clientId, boolean overwrite){
        return execute(Objects.requireNonNull(JacksonUtils.getObject(settingFile, KeycloakRoleExtractResults.class)), clientId, overwrite);
    }

    public KeycloakRoleSyncResults startToSync(KeycloakRoleExtractResults keycloakRoleExtractResults, String clientId){
        return execute(keycloakRoleExtractResults, clientId,false);
    }

    public KeycloakRoleSyncResults startToSync(KeycloakRoleExtractResults keycloakRoleExtractResults, String clientId, boolean overwrite){
        return execute(keycloakRoleExtractResults, clientId, overwrite);
    }

    private KeycloakRoleSyncResults execute(KeycloakRoleExtractResults keycloakRoleExtractResults, String clientId, boolean overwrite) {

        var id = KeycloakUtils.findIdClient(realmResource, clientId);

        var rolesResource = this.realmResource.clients().get(id).roles();
        List<RoleRepresentation> roleRepresentations = rolesResource.list(false);

        List<RoleRepresentation> willInsert = new ArrayList<>();
        List<RoleRepresentation> willUpdate = new ArrayList<>();

        keycloakRoleExtractResults.getRoles().forEach((roleName, roleDetailMap) -> {
            Optional<RoleRepresentation> existedRole = roleRepresentations.stream().filter(roleRepresentation -> roleRepresentation.getName().equals(roleName)).findFirst();
            if (existedRole.isPresent()) {
                willUpdate.add(KeycloakRoleMapper.update(roleDetailMap, existedRole.get(), overwrite));
            } else {
                willInsert.add(KeycloakRoleMapper.create(roleDetailMap, roleName));
            }
        });

        this.insertRange(rolesResource, willInsert);
        this.updateRange(rolesResource, willUpdate);

        log.info("-------------------------------------------");
        log.info("|     Success import keycloak config      |");
        log.info("|     Updated {} role(s)                  |", willUpdate.size());
        log.info("|     Inserted {} role(s)                 |", willInsert.size());
        log.info("-------------------------------------------");

        return KeycloakRoleSyncResults.builder()
                .inserted(willInsert.size())
                .updated(willUpdate.size())
                .build();
    }

    private void insertRange(RolesResource rolesResource, List<RoleRepresentation> roles) {
        roles.forEach(rolesResource::create);
    }

    private void updateRange(RolesResource rolesResource, List<RoleRepresentation> roles) {
        roles.forEach(roleRepresentation -> {
            var roleResource = rolesResource.get(roleRepresentation.getName());
            roleResource.update(roleRepresentation);
        });
    }
}
