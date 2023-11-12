package fpt.edu.capstone.vms.config.keycloak;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class KeycloakProperties {
    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.issuer-uri}")
    private String issuerUri;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.realm}")
    private String realm;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.credentials-secret}")
    private String clientSecret;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.client-id}")
    private String clientId;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.admin-username}")
    private String username;

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.admin-password}")
    private String password;

    public String getAuthUrl() {
        return issuerUri.substring(0, issuerUri.indexOf("/realms/"));
    }

}
