package fpt.edu.capstone.vms.keycloak.sync.models.roles;

import lombok.Data;

@Data
public class KeycloakRoleConstraint {
    private String path;
    private String method;
    private String[] roles;
    private String[] scopes;
}
