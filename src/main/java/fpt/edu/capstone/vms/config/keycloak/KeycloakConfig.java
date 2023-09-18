package fpt.edu.capstone.vms.config.keycloak;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.issuer-uri}")
    private String issuerUri;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.realm}")
    private String realm;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.admin-username}")
    private String adminUsername;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.admin-password}")
    private String adminPassword;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.admin-client}")
    private String adminClient;

    @Bean
    public Keycloak keycloak() {
        String authServerUrl = issuerUri.substring(0, issuerUri.indexOf("/realms/"));
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(adminClient)
                .grantType(OAuth2Constants.PASSWORD)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }
}
