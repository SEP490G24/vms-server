package fpt.edu.capstone.vms.keycloak.sync.models.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakRoleExtractResults {
    private List<KeycloakRoleConstraint> constraints;
    private Map<String, Map<String, KeycloakRoleAttribute>> roles;

    public Map<String, List<String>> generateRoleMapForWeb() {
        if(null == roles || roles.isEmpty()) return null;
        Map<String, List<String>> mapWebRole = new HashMap<>();
        for (String roleName : roles.keySet()) {
            mapWebRole.put(roleName.toUpperCase().replace(":", "_"), List.of(roleName));
        }
        return mapWebRole;
    }
}
