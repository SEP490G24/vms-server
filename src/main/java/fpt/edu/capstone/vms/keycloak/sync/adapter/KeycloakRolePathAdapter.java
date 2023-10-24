package fpt.edu.capstone.vms.keycloak.sync.adapter;

import fpt.edu.capstone.vms.keycloak.sync.mapper.KeycloakRoleMapper;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleAttribute;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleExtractResults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeycloakRolePathAdapter extends KeycloakRoleExtractResults {
    private final List<String> rolePaths;
    private final String module;

    public KeycloakRolePathAdapter(List<String> rolePaths, String module) {
        this.rolePaths = rolePaths;
        this.module = module;
    }

    @Override
    public Map<String, Map<String, KeycloakRoleAttribute>> getRoles() {
        Map<String, Map<String, KeycloakRoleAttribute>> roles = new HashMap<>();
        /* Convert role string to roles map details */
        rolePaths.forEach(role -> {
            roles.put(role, KeycloakRoleMapper.convert2RoleMap(role));
        });
        return roles;
    }
}
