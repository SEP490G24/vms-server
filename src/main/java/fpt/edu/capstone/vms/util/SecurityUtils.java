package fpt.edu.capstone.vms.util;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import fpt.edu.capstone.vms.constants.Constants.Claims;


import java.util.Collection;

import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_REALM_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.REALM_ADMIN;


public class SecurityUtils {



    public static UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var jwt = (Jwt) authentication.getPrincipal();
        return new UserDetails()
                .setOrgId(jwt.getClaim(Claims.OrgId))
                .setName(jwt.getClaim(Claims.Name))
                .setPreferredUsername(jwt.getClaim(Claims.PreferredUsername))
                .setGivenName(jwt.getClaim(Claims.GivenName))
                .setFamilyName(jwt.getClaim(Claims.FamilyName))
                .setEmail(jwt.getClaim(Claims.Email))
                .setRoles(authentication.getAuthorities())
                .setAdmin(authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(PREFIX_REALM_ROLE + REALM_ADMIN))
                );
    }

    public static String loginUsername() {
        return getUserDetails().preferredUsername;
    }

    public static String getOrgId() {
        return getUserDetails().orgId;
    }

    @Data
    @Accessors(chain = true)
    public static class UserDetails {
        private String orgId;
        private String name;
        private String preferredUsername;
        private String givenName;
        private String familyName;
        private String email;
        private boolean isAdmin;
        private Collection<? extends GrantedAuthority> roles;
    }
}
