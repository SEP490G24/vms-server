package fpt.edu.capstone.vms.util;

import fpt.edu.capstone.vms.constants.Constants.Claims;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_REALM_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.PREFIX_RESOURCE_ROLE;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.REALM_ADMIN;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_ORGANIZATION;
import static fpt.edu.capstone.vms.security.converter.JwtGrantedAuthoritiesConverter.SCOPE_SITE;


public class SecurityUtils {

    public static UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var jwt = (Jwt) authentication.getPrincipal();
        var userDetails = new UserDetails()
            .setName(jwt.getClaim(Claims.Name))
            .setPreferredUsername(jwt.getClaim(Claims.PreferredUsername))
            .setGivenName(jwt.getClaim(Claims.GivenName))
            .setFamilyName(jwt.getClaim(Claims.FamilyName))
            .setEmail(jwt.getClaim(Claims.Email))
            .setOrgId(jwt.getClaim(Claims.OrgId))
            .setSiteId(jwt.getClaim(Claims.SiteId))
            .setRoles(authentication.getAuthorities());

        /* Check scope users */
        authentication.getAuthorities().forEach((grantedAuthority) -> {
            switch (grantedAuthority.getAuthority()) {
                case PREFIX_REALM_ROLE + REALM_ADMIN -> userDetails.setRealmAdmin(true);
                case PREFIX_RESOURCE_ROLE + SCOPE_ORGANIZATION -> userDetails.setOrganizationAdmin(true);
                case PREFIX_RESOURCE_ROLE + SCOPE_SITE -> userDetails.setSiteAdmin(true);
            }
        });
        return userDetails;
    }

    public static String loginUsername() {
        return getUserDetails().preferredUsername;
    }

    public static String fullName() {
        return getUserDetails().getGivenName() + "" + getUserDetails().getFamilyName();
    }

    public static String getOrgId() {
        return getUserDetails().orgId;
    }

    public static String getSiteId() {
        return getUserDetails().siteId;
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
        private String siteId;
        private boolean isRealmAdmin = false;
        private boolean isOrganizationAdmin = false;
        private boolean isSiteAdmin = false;
        private Collection<? extends GrantedAuthority> roles;
    }

    public static Boolean checkSiteAuthorization(SiteRepository siteRepository, String siteId) {
        if (SecurityUtils.getOrgId() != null) {
            if (siteId == null) return false;
            var checkSite = siteRepository.existsByIdAndOrganizationId(UUID.fromString(siteId), UUID.fromString(SecurityUtils.getOrgId()));

            if (!checkSite) {
                return false;
            }
        } else {
            if (SecurityUtils.getSiteId() == null) return false;

            if (!SecurityUtils.getSiteId().equals(siteId)) {
                return false;
            }
        }
        return true;
    }

    public static Boolean checkDepartmentInSite(DepartmentRepository departmentRepository, String existsBySiteId, String siteId) {
        var checkDepartment = departmentRepository.existsByIdAndSiteId(UUID.fromString(existsBySiteId), UUID.fromString(siteId));

        if (!checkDepartment) {
            return false;
        }
        return true;
    }

    public static Boolean checkOrganizationAuthor(SiteRepository siteRepository, String organizationId) {

        if (SecurityUtils.getOrgId() != null) {
            if (!SecurityUtils.getOrgId().equals(organizationId)) {
                return false;
            }
        } else {
            if (SecurityUtils.getSiteId() == null) return false;
            var check = siteRepository.existsByIdAndOrganizationId(UUID.fromString(SecurityUtils.getSiteId()), UUID.fromString(organizationId));
            if (!check) {
                return false;
            }
        }
        return true;
    }

    public static List<UUID> getListSiteToUUID(SiteRepository siteRepository, List<String> siteId) {

        if (SecurityUtils.getOrgId() == null && siteId != null && !siteId.isEmpty()) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        List<UUID> sites = new ArrayList<>();
        if (SecurityUtils.getOrgId() != null) {
            if (siteId == null || siteId.isEmpty()) {
                var listSite = siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId()));
                if (listSite.isEmpty()) {
                    throw new CustomException(ErrorApp.SITE_PLEASE_CREATE_SITE);
                }
                listSite.forEach(o -> {
                    sites.add(o.getId());
                });
            } else {
                siteId.forEach(o -> {
                    if (!SecurityUtils.checkSiteAuthorization(siteRepository, o)) {
                        throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
                    }
                    sites.add(UUID.fromString(o));
                });
            }
        } else {
            sites.add(UUID.fromString(SecurityUtils.getSiteId()));
        }

        return sites;
    }

    public static List<String> getListSiteToString(SiteRepository siteRepository, List<String> siteId) {

        if (SecurityUtils.getOrgId() == null && (siteId != null && !siteId.isEmpty())) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        List<String> sites = new ArrayList<>();
        if (SecurityUtils.getOrgId() != null) {
            if (siteId == null || siteId.isEmpty()) {
                var listSite = siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId()));
                if (listSite.isEmpty()) {
                    throw new CustomException(ErrorApp.SITE_PLEASE_CREATE_SITE);
                }
                listSite.forEach(o -> {
                    sites.add(o.getId().toString());
                });
            } else {
                siteId.forEach(o -> {
                    if (!SecurityUtils.checkSiteAuthorization(siteRepository, o)) {
                        throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
                    }
                    sites.add(o);
                });
            }
        } else {
            sites.add(SecurityUtils.getSiteId());
        }

        return sites;
    }

}
