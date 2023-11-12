package fpt.edu.capstone.vms.security.converter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConditionalOnExpression(value = "'${edu.fpt.capstone.vms.oauth2.provider}'.equals('keycloak')")
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    /**
     * Prefix used for realm level roles.
     */
    public static final String PREFIX_REALM_ROLE = "ROLE_realm_";
    /**
     * Prefix used in combination with the resource (client) name for resource level roles.
     */
    public static final String PREFIX_RESOURCE_ROLE = "ROLE_";

    /**
     * Name of the claim containing the realm level roles
     */
    private static final String CLAIM_REALM_ACCESS = "realm_access";
    /**
     * Name of the claim containing the resources (clients) the user has access to.
     */
    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    /**
     * Name of the claim containing roles. (Applicable to realm and resource level.)
     */
    private static final String CLAIM_ROLES = "roles";
    /**
     * Name of role realm admin
     */
    public static final String REALM_ADMIN = "REALM_ADMIN";

    /**
     * Name of scope organization admin
     */
    public static final String SCOPE_ORGANIZATION = "scope:organization";

    /**
     * Name of scope site admin
     */
    public static final String SCOPE_SITE = "scope:site";

    @Value("${edu.fpt.capstone.vms.oauth2.keycloak.client-id}")
    private String resourceId;

    /**
     * Extracts the realm and resource level roles from a JWT token distinguishing between them using prefixes.
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Collection that will hold the extracted roles
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // Realm roles
        grantedAuthorities.addAll(extractRealmRoles(jwt));

        // Resource (client) roles
        grantedAuthorities.addAll(extractResourceRoles(jwt));

        return grantedAuthorities;
    }

    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // Realm roles
        // Get the part of the access token that holds the roles assigned on realm level
        Map<String, Collection<String>> realmAccess = jwt.getClaim(CLAIM_REALM_ACCESS);

        // Verify that the claim exists and is not empty
        if (realmAccess != null && !realmAccess.isEmpty()) {
            // From the realm_access claim get the roles
            Collection<String> roles = realmAccess.get(CLAIM_ROLES);
            // Check if any roles are present
            if (roles != null && !roles.isEmpty()) {
                // Iterate of the roles and add them to the granted authorities
                Collection<GrantedAuthority> realmRoles = roles.stream()
                    // Prefix all realm roles with "ROLE_realm_"
                    .map(role -> new SimpleGrantedAuthority(PREFIX_REALM_ROLE + role))
                    .collect(Collectors.toList());
                grantedAuthorities.addAll(realmRoles);
            }
        }

        return grantedAuthorities;
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // Resource (client) roles
        // A user might have access to multiple resources all containing their own roles. Therefore, it is a map of
        // resource each possibly containing a "roles" property.
        Map<String, Map<String, Collection<String>>> resourceAccess = jwt.getClaim(CLAIM_RESOURCE_ACCESS);

        // Check if resources are assigned
        if (resourceAccess != null && !resourceAccess.isEmpty()) {
            // Iterate of all the resources
            resourceAccess.forEach((resource, resourceClaims) -> {
                // Iterate of the "roles" claim inside the resource claims
                resourceClaims.get(CLAIM_ROLES).forEach(
                    // Add the role to the granted authority prefixed with ROLE_ and the name of the resource
                    role -> grantedAuthorities.add(new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + role))
                );
            });
        }

        return grantedAuthorities;
    }

}
