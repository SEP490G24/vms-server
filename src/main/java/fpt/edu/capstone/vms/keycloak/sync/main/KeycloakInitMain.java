package fpt.edu.capstone.vms.keycloak.sync.main;

import fpt.edu.capstone.vms.keycloak.sync.constants.KeycloakConstants;
import fpt.edu.capstone.vms.keycloak.sync.helper.KeycloakInitHelper;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.Collections;

public class KeycloakInitMain {
    public static void main(String[] args) {

        /* Init Keycloak */
        var keycloak = KeycloakBuilder.builder()
                .serverUrl(KeycloakConstants.SEVER_URL)
                .realm(KeycloakConstants.REALM_MASTER)
                .clientId(KeycloakConstants.CLIENT)
                .grantType(OAuth2Constants.PASSWORD)
                .username(KeycloakConstants.USERNAME)
                .password(KeycloakConstants.PASSWORD)
                .build();

        /* Create Realm */
        var realm = new RealmRepresentation();
        realm.setRealm(KeycloakConstants.REALM);
        realm.setEnabled(true);
        keycloak.realms().create(realm);

        /* Init Keycloak Helper */
        var helper = new KeycloakInitHelper(keycloak.realm(KeycloakConstants.REALM));

        /* Init Users */
        helper.initUser(KeycloakConstants.admin);

        /* Init Roles */
        KeycloakConstants.roles.forEach(helper::initRole);

        /* Assign Role Composite for REALM_ADMIN Role */
        helper.assignRoleComposite2RealmAdmin(KeycloakConstants.REALM_ADMIN_ROLE);

        /* Assign Assign Role for REALM_ADMIN User */
        helper.assignRole2RealmAdminUser(KeycloakConstants.USERNAME, Collections.singletonList(KeycloakConstants.REALM_ADMIN_ROLE));

        /* Init Client Scope */
        KeycloakConstants.clientScopes.forEach(helper::initClientScope);

        /* Init Client */
        KeycloakConstants.clients.forEach(helper::initClient);
    }
}

