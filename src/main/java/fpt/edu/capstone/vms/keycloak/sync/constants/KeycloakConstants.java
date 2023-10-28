package fpt.edu.capstone.vms.keycloak.sync.constants;

import fpt.edu.capstone.vms.keycloak.sync.models.properties.KeycloakClientProperties;
import fpt.edu.capstone.vms.keycloak.sync.models.properties.KeycloakClientScopeProperties;
import fpt.edu.capstone.vms.keycloak.sync.models.properties.KeycloakRoleProperties;
import fpt.edu.capstone.vms.keycloak.sync.models.properties.KeycloakUserProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KeycloakConstants {

    public final static String REALM_ADMIN_ROLE = "REALM_ADMIN";

    public final static String REALM_MASTER = "master";
    public final static String SEVER_URL = "https://idm-vms.azurewebsites.net";
    public static final String CLIENT = "admin-cli";
    public final static String USERNAME = "admin";
    public final static String PASSWORD = "admin";

    public final static String REALM = "vms";

    private final static String ORG_ID = "org_id";
    private final static String SITE_ID = "site_id";

    public final static String CLIENT_APP = "vms-app";
    public final static String CLIENT_API = "vms-api";

    public final static KeycloakUserProperties admin = new KeycloakUserProperties()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setFirstName("Realm")
                .setLastName("Admin")
                .setEmail("admin@fpt.edu.vn");

    public final static List<KeycloakRoleProperties> roles = new ArrayList<>() {{
        add(new KeycloakRoleProperties()
                .setName(REALM_ADMIN_ROLE)
                .setDescription("Realm admin role"));
    }};

    public final static List<KeycloakClientProperties> clients = new ArrayList<>() {{
        add(new KeycloakClientProperties()
            .setClientId(CLIENT_APP)
            .setName("VMS Application")
            .setSecret("7r5uiBkk4lDoXKzwt20UYWACNRxwfeZD")
            .setBaseUrl("https://web-vms.azurewebsites.net")
            .setRedirectUris(Collections.singletonList("https://web-vms.azurewebsites.net/*"))
            .setImplicitFlowEnabled(true)
            .setServiceAccountsEnabled(true)
            .setDefaultClientScopes(new ArrayList<>() {{
                add(ORG_ID);
                add(SITE_ID);
            }}));
        add(new KeycloakClientProperties()
            .setClientId(CLIENT_API)
            .setName("VMS API Resource")
            .setSecret("uq9MroK6qI2gs77wEIKG6ZroKCnzgZMt"));
    }};

    public final static List<KeycloakClientScopeProperties> clientScopes = new ArrayList<>() {{
        add(new KeycloakClientScopeProperties()
            .setName(ORG_ID)
            .setDescription("Organization ID"));
        add(new KeycloakClientScopeProperties()
            .setName(SITE_ID)
            .setDescription("Site ID"));
    }};
}
