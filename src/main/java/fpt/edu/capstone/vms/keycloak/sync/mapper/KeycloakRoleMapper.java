package fpt.edu.capstone.vms.keycloak.sync.mapper;

import fpt.edu.capstone.vms.keycloak.sync.models.LanguageCode;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.KeycloakRoleAttribute;
import fpt.edu.capstone.vms.keycloak.sync.models.roles.RoleAttributes;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public final class KeycloakRoleMapper {
    public static RoleRepresentation update(Map<String, KeycloakRoleAttribute> roleDetailMap, RoleRepresentation roleRepresentation, boolean overwrite) {
        roleRepresentation.setDescription(roleDetailMap.get(LanguageCode.VN.getValue()).getDescription());
        Map<String, List<String>> attributes = generateAttributes(roleDetailMap);
        if (!overwrite) {
            attributes = mergeMap(roleRepresentation.getAttributes(), attributes);
        }
        roleRepresentation.setAttributes(attributes);

        return roleRepresentation;
    }

    public static RoleRepresentation create(Map<String, KeycloakRoleAttribute> roleDetailMap, final String roleName) {

        var newRole = new RoleRepresentation();
        newRole.setName(roleName);
        newRole.setAttributes(generateAttributes(roleDetailMap));
        //TODO: verify 2 below fields
        newRole.setComposite(false);
        newRole.setClientRole(true);

        return newRole;
    }

    private static Map<String, List<String>> mergeMap(Map<String, List<String>> source, Map<String, List<String>> destination) {
        return Stream.of(source, destination)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (sourceValue, destinationValue) -> sourceValue));
    }

    private static Map<String, List<String>> generateAttributes(Map<String, KeycloakRoleAttribute> roleDetailMap) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(RoleAttributes.GROUP.getValue(), Collections.singletonList(roleDetailMap.get(LanguageCode.VN.getValue()).getFeature()));
        attributes.put(RoleAttributes.SCOPE.getValue(), Arrays.asList("system", "organization", "site"));
        Stream.of(LanguageCode.values()).forEach(languageCode -> {
            attributes.put(RoleAttributes.FEATURE.getValue() + ":" + languageCode.getValue(), Collections.singletonList(roleDetailMap.get(languageCode.getValue()).getFeature()));
            attributes.put(RoleAttributes.NAME.getValue() + ":" + languageCode.getValue(), Collections.singletonList(roleDetailMap.get(languageCode.getValue()).getName()));
        });
        return attributes;
    }

    public static Map<String, KeycloakRoleAttribute> convert2RoleMap(String role, Map<String, Map<String, KeycloakRoleAttribute>> oldRoles) {
        String[] roleDetails = role.trim().split(":");
        Map<String, KeycloakRoleAttribute> results = new HashMap<>();
        if (!roleDetails[0].equalsIgnoreCase("r")) {
            throw new IllegalArgumentException("Role name must start with r!! Current value " + role);
        }

        KeycloakRoleAttribute keycloakRoleAttribute = new KeycloakRoleAttribute();
        Map<String, KeycloakRoleAttribute> roleMap = oldRoles == null ? null : oldRoles.get(role);

        if (roleMap != null) {
            return roleMap;
        } else {
            keycloakRoleAttribute.setDescription(role);
            keycloakRoleAttribute.setFeature(roleDetails[1]);
            keycloakRoleAttribute.setName(roleDetails[2]);

            Stream.of(LanguageCode.values()).forEach(languageCode -> {
                results.put(languageCode.getValue(), keycloakRoleAttribute);
            });

            return results;
        }
    }

    public static Map<String, KeycloakRoleAttribute> convert2RoleMap(String role) {
        String[] roleDetails = role.trim().split(":");
        Map<String, KeycloakRoleAttribute> results = new HashMap<>();
        KeycloakRoleAttribute keycloakRoleAttribute = new KeycloakRoleAttribute();
        keycloakRoleAttribute.setDescription(role);
        keycloakRoleAttribute.setFeature(roleDetails[0]);
        keycloakRoleAttribute.setName(roleDetails[1]);

        Stream.of(LanguageCode.values()).forEach(languageCode -> {
            results.put(languageCode.getValue(), keycloakRoleAttribute);
        });

        return results;
    }
}
