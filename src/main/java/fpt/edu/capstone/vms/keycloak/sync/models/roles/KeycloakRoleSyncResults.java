package fpt.edu.capstone.vms.keycloak.sync.models.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakRoleSyncResults {
    private long inserted;
    private long updated;
}
