package fpt.edu.capstone.vms.keycloak.sync.main;


import fpt.edu.capstone.vms.keycloak.sync.constants.KeycloakConstants;
import fpt.edu.capstone.vms.keycloak.sync.helper.KeyCloakRoleSyncHelper;
import fpt.edu.capstone.vms.keycloak.sync.helper.KeycloakRoleExtractorHelper;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleExtractConfig;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;

import java.io.File;
import java.io.IOException;

import static fpt.edu.capstone.vms.keycloak.sync.constants.KeycloakConstants.CLIENT_API;
import static fpt.edu.capstone.vms.keycloak.sync.constants.KeycloakConstants.CLIENT_APP;

public class PermissionExtractorMain {

    public static void main(String[] args) throws IOException {

        final String rolesPathsInput = String.format("permission/%s/roles.json", CLIENT_APP);

        final String rolesPathsOutput = String.format("permission/%s/permission-setting.json", CLIENT_APP);
        final String extractPathsOutput = String.format("permission/%s/permission-setting.json", CLIENT_API);

        var keycloak = KeycloakBuilder.builder()
                .serverUrl(KeycloakConstants.SEVER_URL)
                .realm(KeycloakConstants.REALM_MASTER)
                .clientId(KeycloakConstants.CLIENT)
                .grantType(OAuth2Constants.PASSWORD)
                .username(KeycloakConstants.USERNAME)
                .password(KeycloakConstants.PASSWORD)
                .build();

        /* Adapter roles path web to permission keycloak*/
//        KeycloakRoleExtractorHelper.adaptRolePaths2File(new File(rolesPathsInput), CLIENT_APP, rolesPathsOutput);

        /* Extract roles server to permission keycloak*/
        KeycloakRoleExtractorHelper.extract2File(KeycloakRoleExtractConfig.builder()
                .startWith("fpt.edu.capstone.vms.controller")
                .endWith("controller")
                .cleanup("true").build(), extractPathsOutput);


        var keyCloakRoleSyncHelper = new KeyCloakRoleSyncHelper(keycloak, KeycloakConstants.REALM);

//        keyCloakRoleSyncHelper.startToSync(new File(rolesPathsOutput), CLIENT_APP);

        keyCloakRoleSyncHelper.startToSync(new File(extractPathsOutput), CLIENT_API);
    }
}
