package fpt.edu.capstone.vms.keycloak.sync.models.roles;

import lombok.Getter;

@Getter
public enum RoleAttributes {
    FEATURE("feature"),
    NAME("name"),
    GROUP("group");

    private final String value;

    RoleAttributes(String language) {
        this.value = language;
    }

}
