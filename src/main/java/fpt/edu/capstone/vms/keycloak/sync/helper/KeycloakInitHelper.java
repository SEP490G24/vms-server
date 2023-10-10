package fpt.edu.capstone.vms.keycloak.sync.helper;

import fpt.edu.capstone.vms.keycloak.sync.models.properties.KeycloakClientProperties;
import fpt.edu.capstone.vms.keycloak.sync.models.properties.KeycloakClientScopeProperties;
import fpt.edu.capstone.vms.keycloak.sync.models.properties.KeycloakRoleProperties;
import fpt.edu.capstone.vms.keycloak.sync.models.properties.KeycloakUserProperties;
import fpt.edu.capstone.vms.util.KeycloakUtils;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public final class KeycloakInitHelper {

    private final RealmResource realm;

    public final static String REALM_MANAGER_CLIENT_ID = "realm-management";
    public final static String ACCOUNT_CLIENT_ID = "account";

    public void assignRole2RealmAdminUser(String username, List<String> realmRoles) {
        var realmAdmin = realm.users().get(KeycloakUtils.findIdUser(realm, username));
        realmAdmin.roles().realmLevel().add(realmRoles.stream()
                .map(roleName -> realm.roles().get(roleName).toRepresentation())
                .collect(Collectors.toList()));
    }

    public void assignRoleComposite2RealmAdmin(String realmAdmin) {
        var adminRole = realm.roles().get(realmAdmin);
        var realmManagerRoles = realm.clients().get(KeycloakUtils.findIdClient(realm, REALM_MANAGER_CLIENT_ID)).roles().list();
        var accountRoles = realm.clients().get(KeycloakUtils.findIdClient(realm, ACCOUNT_CLIENT_ID)).roles().list();
        adminRole.addComposites(realmManagerRoles);
        adminRole.addComposites(accountRoles);
    }

    public void initUser(KeycloakUserProperties properties) {
        /* Define password credential */
        var passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(properties.getPassword());

        var user = new UserRepresentation();
        user.setUsername(properties.getUsername());
        user.setFirstName(properties.getFirstName());
        user.setLastName(properties.getLastName());
        user.setEmail(properties.getEmail());
        user.setEnabled(properties.getEnabled());
        user.setEmailVerified(properties.getEmailVerified());
        user.setCredentials(List.of(passwordCred));
        user.setAttributes(properties.getAttributes());
        realm.users().create(user);
    }

    public void initClient(KeycloakClientProperties properties) {
        var client = new ClientRepresentation();
        client.setClientId(properties.getClientId());
        client.setName(properties.getName());
        client.setDescription(properties.getDescription());
        client.setRootUrl(properties.getRootUrl());
        client.setAdminUrl(properties.getAdminUrl());
        client.setBaseUrl(properties.getBaseUrl());
        client.setSurrogateAuthRequired(false);
        client.setEnabled(true);
        client.setAlwaysDisplayInConsole(false);
        client.setClientAuthenticatorType("client-secret");
        client.setSecret(properties.getSecret());
        client.setRedirectUris(properties.getRedirectUris());
        client.setWebOrigins(properties.getWebOrigins());
        client.setNotBefore(0);
        client.setBearerOnly(false);
        client.setConsentRequired(false);
        client.setStandardFlowEnabled(true);
        client.setImplicitFlowEnabled(properties.getServiceAccountsEnabled());
        client.setDirectAccessGrantsEnabled(true);
        client.setServiceAccountsEnabled(properties.getServiceAccountsEnabled());
        client.setPublicClient(false);
        client.setFrontchannelLogout(true);
        client.setProtocol("openid-connect");
        client.setAuthenticationFlowBindingOverrides(new HashMap<>());
        client.setFullScopeAllowed(true);
        client.setNodeReRegistrationTimeout(-1);
        client.setDefaultClientScopes(Stream.concat(Stream.of("web-origins", "acr", "profile", "roles", "email"), properties.getDefaultClientScopes().stream()).toList());
        client.setOptionalClientScopes(Stream.concat(Stream.of("address", "phone", "offline_access", "microprofile-jwt"), properties.getOptionalClientScopes().stream()).toList());
        realm.clients().create(client);
    }

    public void initClientScope(KeycloakClientScopeProperties properties) {
        var clientScope = new ClientScopeRepresentation();
        clientScope.setName(properties.getName());
        clientScope.setDescription(properties.getDescription());
        clientScope.setProtocol("openid-connect");
        clientScope.setAttributes(new HashMap<>() {{
            put("include.in.token.scope", "true");
            put("display.on.consent.screen", "true");
            put("gui.order", "");
            put("consent.screen.text", "");
        }});
        var protocolMapper = initProtocolMapper(properties);
        clientScope.setProtocolMappers(Collections.singletonList(protocolMapper));
        realm.clientScopes().create(clientScope);
    }

    private ProtocolMapperRepresentation initProtocolMapper(KeycloakClientScopeProperties properties) {
        var protocolMapper = new ProtocolMapperRepresentation();
        protocolMapper.setName(properties.getName());
        protocolMapper.setProtocol("openid-connect");
        protocolMapper.setProtocolMapper("oidc-usermodel-attribute-mapper");
        protocolMapper.setConfig(new HashMap<>() {{
            put("userinfo.token.claim", "true");
            put("user.attribute", properties.getName());
            put("id.token.claim", "true");
            put("access.token.claim", "true");
            put("claim.name", properties.getName());
            put("jsonType.label", "String");
        }});
        return protocolMapper;
    }

    public void initRole(KeycloakRoleProperties properties) {
        var role = new RoleRepresentation();
        role.setName(properties.getName());
        role.setDescription(properties.getDescription());
        role.setComposite(properties.getComposite());
        role.setClientRole(properties.getClientRole());
        realm.roles().create(role);
    }



}
