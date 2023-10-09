package fpt.edu.capstone.vms.keycloak.sync.models.properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakUserProperties {
    String username = "";
    String password;
    String firstName = "";
    String lastName = "";
    String email = "";
    Boolean emailVerified = true;
    Boolean enabled = true;
    Map<String, List<String>> attributes = new HashMap<>();
}
