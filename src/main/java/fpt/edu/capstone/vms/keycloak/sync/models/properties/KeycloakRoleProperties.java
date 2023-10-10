package fpt.edu.capstone.vms.keycloak.sync.models.properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakRoleProperties {
    String name = "";
    String description = "";
    Boolean composite = true;
    Boolean clientRole = false;
}
