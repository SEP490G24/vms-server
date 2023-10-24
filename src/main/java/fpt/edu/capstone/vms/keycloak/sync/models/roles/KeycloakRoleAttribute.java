package fpt.edu.capstone.vms.keycloak.sync.models.roles;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakRoleAttribute {
    String name;
    String feature;
    String description;
}
