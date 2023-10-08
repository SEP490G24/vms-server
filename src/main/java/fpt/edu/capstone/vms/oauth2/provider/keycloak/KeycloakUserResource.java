package fpt.edu.capstone.vms.oauth2.provider.keycloak;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnExpression(value = "'${edu.fpt.capstone.vms.oauth2.provider}'.equals('keycloak')")
public class KeycloakUserResource implements IUserResource {

    private final Keycloak keycloak;
    private final ModelMapper mapper;
    private final String REALM;
    private final UsersResource usersResource;
    private final RolesResource rolesResource;


    public KeycloakUserResource(
        Keycloak keycloak,
        ModelMapper mapper, @Value("${edu.fpt.capstone.vms.oauth2.keycloak.realm}") String realm
    ) {
        this.keycloak = keycloak;
        this.mapper = mapper;
        this.REALM = realm;
        RealmResource realmResource = keycloak.realm(REALM);
        this.usersResource = realmResource.users();
        this.rolesResource = realmResource.roles();
    }

    @Override
    public String create(UserDto userDto) {

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(Constants.Claims.OrgId, List.of(SecurityUtils.getOrgId()));

        /* Define password credential */
        var passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(userDto.getPassword());

        var user = new UserRepresentation();
        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setEnabled(userDto.getIsEnable());
        user.setEmailVerified(false);
        user.setCredentials(List.of(passwordCred));

        try (var response = usersResource.create(user)) {
            // createOrUpdate user
            String userId = CreatedResponseUtil.getCreatedId(response);

            // assign role
            RoleRepresentation roleRepresentation = rolesResource.get(userDto.getRole().toString()).toRepresentation();
            usersResource.get(userId).roles().realmLevel().add(List.of(roleRepresentation));

            return userId;
        }
    }

    @Override
    public boolean update(UserDto userDto) {
        var userResource = usersResource.get(userDto.getOpenid());
        UserRepresentation modifiedUser = userResource.toRepresentation();

        // Update password credential if password not null or empty
        if (!StringUtils.isEmpty(userDto.getPassword())) {
            var passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(userDto.getPassword());
            modifiedUser.setCredentials(List.of(passwordCred));
        }

        modifiedUser.setEmail(userDto.getEmail());
        if (userDto.getIsEnable() != null) modifiedUser.setEnabled(userDto.getIsEnable());
        userResource.update(modifiedUser);

        return true;
    }

    @Override
    public void changeState(String userId, boolean stateEnable) {
        RealmResource realmResource = keycloak.realm(REALM);

        UserRepresentation modifiedUser = realmResource.users().get(userId).toRepresentation();
        modifiedUser.setEnabled(stateEnable);

        realmResource.users().get(userId).update(modifiedUser);
    }


    @Override
    public void delete(String userId) {
        UserResource user = keycloak.realm(REALM).users().get(userId);
        user.remove();
    }

    @Override
    public List<UserDto> users() {
        UsersResource usersResource = keycloak.realm(REALM).users();


        return usersResource.list()
            .stream()
            .map(u -> {
                Constants.UserRole userRole = null;
                RoleScopeResource roleScopeResource = usersResource.get(u.getId()).roles().realmLevel();
                List<RoleRepresentation> roles = roleScopeResource.listAll();
                for (RoleRepresentation role : roles) {
                    try {
                        userRole = Constants.UserRole.valueOf(role.getName());
                        break;
                    } catch (Exception e) {
                    }
                }

                return mapper.map(u, UserDto.class)
                    .setRole(userRole);
            })
            .collect(Collectors.toList());
    }
}
