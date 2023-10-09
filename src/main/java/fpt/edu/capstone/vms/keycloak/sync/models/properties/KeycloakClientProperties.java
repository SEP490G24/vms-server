package fpt.edu.capstone.vms.keycloak.sync.models.properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakClientProperties {
    String clientId = "";
    String name = "";
    String description = "";
    String secret = "";
    String rootUrl = "";
    String adminUrl = "";
    String baseUrl = "";
    List<String> redirectUris = Collections.singletonList("/*");
    List<String> webOrigins = Collections.singletonList("");
    Boolean serviceAccountsEnabled = false;
    Boolean implicitFlowEnabled = false;
    List<String> defaultClientScopes = new ArrayList<>();
    List<String> optionalClientScopes = new ArrayList<>();
}
