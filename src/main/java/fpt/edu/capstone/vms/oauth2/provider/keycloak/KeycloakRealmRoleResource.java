package fpt.edu.capstone.vms.oauth2.provider.keycloak;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Slf4j
@Component
public class KeycloakRealmRoleResource implements IRoleResource {

    private final RolesResource rolesResource;
    private final ModelMapper mapper;
    private final SiteRepository siteRepository;

    private final String[] ignoreDefaultRoles;

    public KeycloakRealmRoleResource(
        Keycloak keycloak,
        @Value("${edu.fpt.capstone.vms.oauth2.keycloak.realm}") String realm,
        @Value("${edu.fpt.capstone.vms.oauth2.keycloak.ignore-default-roles}") String[] ignoreDefaultRoles,
        ModelMapper mapper, SiteRepository siteRepository) {
        this.ignoreDefaultRoles = ignoreDefaultRoles;
        this.siteRepository = siteRepository;
        this.rolesResource = keycloak.realm(realm).roles();
        this.mapper = mapper;
    }


    @Override
    public List<RoleDto> findAll() {
        /* fetch all role */
        var roles = this.rolesResource.list(false).stream()
            .filter(roleRepresentation -> !Arrays.asList(ignoreDefaultRoles).contains(roleRepresentation.getName()))
            .toList();
        var results = (List<RoleDto>) mapper.map(roles, new TypeToken<List<RoleDto>>() {
        }.getType());

        /* set permission for role */
        results.forEach(this::updatePermission4Role);

        return results;
    }

    @Override
    public Page<RoleDto> filter(IRoleController.RoleBasePayload roleBasePayload, Pageable pageable) {
        List<RoleRepresentation> roles = this.rolesResource.list(false);

        var filteredRoles = roles.stream()
            .filter(roleRepresentation -> {
                if (roleBasePayload.getCode() == null || roleRepresentation.getName().contains(roleBasePayload.getCode())) {
                    List<String> siteIds = roleBasePayload.getAttributes() != null ? roleBasePayload.getAttributes().get("siteId") : null;
                    List<String> names = roleBasePayload.getAttributes() != null ? roleBasePayload.getAttributes().get("name") : null;
                    return (siteIds == null || siteIds.isEmpty() ||
                        siteIds.stream().anyMatch(siteId ->
                            roleRepresentation.getAttributes().get("site_id") != null &&
                                roleRepresentation.getAttributes().get("site_id").contains(siteId)))
                        && (names == null || names.isEmpty() ||
                        names.stream().anyMatch(name ->
                            roleRepresentation.getAttributes().get("name") != null &&
                                roleRepresentation.getAttributes().get("name").get(0).contains(name)));
                }
                return false;
            })
            .toList();


        var results = (List<RoleDto>) mapper.map(filteredRoles, new TypeToken<List<RoleDto>>() {
        }.getType());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredRoles.size());
        List<RoleDto> pageRoles = results.subList(start, end);
        results.forEach(this::updatePermission4Role);
        pageRoles.forEach(this::updatePermission4Role);
        return new PageImpl<>(pageRoles, pageable, filteredRoles.size());
    }

    @Override
    public List<RoleDto> filter(IRoleController.RoleBasePayload roleBasePayload) {
        List<RoleRepresentation> roles = this.rolesResource.list(false);
        List<RoleRepresentation> filteredRoles;
        if (SecurityUtils.getUserDetails().isRealmAdmin()) {
             filteredRoles = new ArrayList<>();
        } else {
            filteredRoles = roles.stream()
                .filter(roleRepresentation -> {
                    if (roleBasePayload.getCode() == null || roleRepresentation.getName().contains(roleBasePayload.getCode())) {
                        List<String> siteIds = roleBasePayload.getAttributes() != null ? roleBasePayload.getAttributes().get("siteId") : null;
                        List<String> names = roleBasePayload.getAttributes() != null ? roleBasePayload.getAttributes().get("name") : null;
                        return (siteIds == null || siteIds.isEmpty() ||
                            siteIds.stream().anyMatch(siteId ->
                                roleRepresentation.getAttributes().get("site_id") != null &&
                                    roleRepresentation.getAttributes().get("site_id").contains(siteId)))
                            && (names == null || names.isEmpty() ||
                            names.stream().anyMatch(name ->
                                roleRepresentation.getAttributes().get("name") != null &&
                                    roleRepresentation.getAttributes().get("name").get(0).contains(name)));
                    }
                    return false;
                })
                .toList();
        }
        var results = (List<RoleDto>) mapper.map(filteredRoles, new TypeToken<List<RoleDto>>() {
        }.getType());
        results.forEach(this::updatePermission4Role);

        if(SecurityUtils.getUserDetails().isSiteAdmin()){
            Iterator<RoleDto> iterator = results.iterator();

            while (iterator.hasNext()) {
                RoleDto result = iterator.next();

                Optional<IPermissionResource.PermissionDto> permissionDtoOptional = result.getPermissionDtos().stream()
                    .filter(x -> "scope:site".equals(x.getName()))
                    .findFirst();

                if (permissionDtoOptional.isPresent()) {
                    // Nếu tồn tại PermissionDto có tên là "scope:site", thì xóa result khỏi danh sách.
                    iterator.remove();
                }
            }
        }

        return results;
    }


    @Override
    public RoleDto findById(String roleName) {
        var role = this.rolesResource.get(roleName);
        var roleRepresentation = role.toRepresentation();
        var roleDto = mapper.map(roleRepresentation, RoleDto.class);
        updatePermission4Role(roleDto);
        return roleDto;
    }

    @Override
    public RoleDto create(RoleDto value) {
        var roleInsert = new RoleRepresentation();
        String siteId = null;
        String orgId = null;

        List<String> siteIdList = value.getAttributes().get("site_id");
        List<String> orgIdList = value.getAttributes().get("org_id");

        if (!CollectionUtils.isEmpty(siteIdList)) {
            siteId = siteIdList.get(0);
        } else {
            if (!SecurityUtils.getUserDetails().isRealmAdmin()) {
                siteId = SecurityUtils.getSiteId();
                value.getAttributes().put("site_id", Collections.singletonList(siteId));
            }
        }

        if (!CollectionUtils.isEmpty(orgIdList)) {
            orgId = orgIdList.get(0);
        }
        if (orgId != null) {
            roleInsert.setName(value.getCode());
        } else if (siteId != null) {
            Site site = siteRepository.findById(UUID.fromString(siteId)).orElse(null);
            if (ObjectUtils.isEmpty(site)) throw new CustomException(ErrorApp.BAD_REQUEST);
            roleInsert.setName((site.getCode() + "_" + value.getCode()).toUpperCase());
        }
        roleInsert.setAttributes(value.getAttributes());
        roleInsert.setDescription(value.getDescription());
        this.rolesResource.create(roleInsert);

        //add permistion
        if (value.getPermissionDtos() != null && !value.getPermissionDtos().isEmpty()) {
            List<IPermissionResource.PermissionDto> stringList = new ArrayList<>();
            stringList.addAll(value.getPermissionDtos());
            var roleUpdate = this.rolesResource.get(roleInsert.getName());
            for (IPermissionResource.PermissionDto p : stringList
            ) {
                roleUpdate.addComposites(Collections.singletonList(mapper.map(p, RoleRepresentation.class)));
            }
        }

        return mapper.map(roleInsert, RoleDto.class);
    }

    @Override
    public RoleDto update(String roleCode, RoleDto value) throws NotFoundException {
        var roleUpdate = this.rolesResource.get(roleCode);
        if (roleUpdate == null) throw new NotFoundException();
        var role = roleUpdate.toRepresentation();
        if (value.getAttributes() != null && role.getAttributes() != null) {
            String siteId = role.getAttributes().get("site_id").get(0);
            List<String> sites = SecurityUtils.getListSiteToString(siteRepository, Collections.emptyList());
            if (sites.contains(siteId)) {
                if (value.getAttributes().get("name") != null) {
                    var newName = value.getAttributes().get("name").get(0);
                    if (!StringUtils.isEmpty(newName)) {
                        role.getAttributes().put("name", Collections.singletonList(newName));
                    }
                }
            } else {
                throw new CustomException(ErrorApp.FORBIDDEN);
            }
        }
        role.setDescription(value.getDescription());
        roleUpdate.update(role);
        return mapper.map(role, RoleDto.class);
    }

    @Override
    public RoleDto updatePermission(String roleCode, IPermissionResource.PermissionDto permissionDto, boolean state) {
        var roleUpdate = this.rolesResource.get(roleCode);
        if (state)
            roleUpdate.addComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        else
            roleUpdate.deleteComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        return mapper.map(roleUpdate, RoleDto.class);
    }

    @Override
    public RoleDto updatePermissions(String roleCode, List<IPermissionResource.PermissionDto> permissionDto, boolean state) {
        var roleUpdate = this.rolesResource.get(roleCode);
        if (state)
            roleUpdate.addComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        else
            roleUpdate.deleteComposites(Collections.singletonList(mapper.map(permissionDto, RoleRepresentation.class)));
        return mapper.map(roleUpdate, RoleDto.class);
    }

    @Override
    public void delete(String roleCode) {
        this.rolesResource.deleteRole(roleCode);
    }

    @Override
    public List<RoleDto> getBySites(List<String> sites) {
        List<RoleRepresentation> roles = this.rolesResource.list(false);

        var role = roles.stream()
            .filter(roleRepresentation -> {

                if (roleRepresentation.getAttributes() != null && roleRepresentation.getAttributes().get("site_id") != null) {
                    String siteId = roleRepresentation.getAttributes().get("site_id").get(0);
                    return sites.contains(siteId);
                }
                return false;
            }).toList();

        var results = (List<RoleDto>) mapper.map(role, new TypeToken<List<RoleDto>>() {
        }.getType());

        results.forEach(this::updatePermission4Role);

        return results;
    }

    private void updatePermission4Role(RoleDto role) {
        var roleResource = this.rolesResource.get(role.getCode());
        role.setPermissionDtos(mapper.map(roleResource.getRoleComposites(), new TypeToken<Set<IPermissionResource.PermissionDto>>() {
        }.getType()));
    }

}
