package fpt.edu.capstone.vms.keycloak.sync.models.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakRoleExtractConfig {
    private String startWith;
    private String endWith;
    private String cleanup;
}
