package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class UserController implements IUserController {

    private final IUserService userService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> info(String username) throws NotFoundException {
        User user = userService.findByUsername(username);
        if(user == null) {
            throw new NotFoundException(String.format("Can't found user with username is %s", username));
        }
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<?> filter(UserFilter filter) {
        return ResponseEntity.ok(
                userService.filter(
                        filter.getPageNumber(),
                        filter.getUsernames(),
                        filter.getRoles(),
                        filter.getCreatedOnStart(),
                        filter.getCreatedOnEnd(),
                        filter.getState()));
    }

    @Override
    public ResponseEntity<?> create(CreateUserInfo userInfo) {
        User userEntity = userService.createUser(mapper.map(userInfo, IUserResource.UserDto.class)
                .setRole(Constants.UserRole.AGENT_ACCOUNT));
        return ResponseEntity.ok(mapper.map(userEntity, IUserResource.UserDto.class));
    }

    @Override
    public ResponseEntity<?> update(String username, @Valid UpdateUserInfo userInfo) throws NotFoundException {
        User userEntity = userService.updateUser(
            mapper.map(userInfo, IUserResource.UserDto.class)
                .setUsername(username));
        return ResponseEntity.ok(mapper.map(userEntity, IUserResource.UserDto.class));
    }

    @Override
    public ResponseEntity<?> updateProfile(CreateUserInfo userInfo) throws NotFoundException {
        String username = SecurityUtils.loginUsername();
        userInfo.setUsername(username);
        User userEntity = userService.updateUser(mapper.map(userInfo, IUserResource.UserDto.class));
        return ResponseEntity.ok(mapper.map(userEntity, IUserResource.UserDto.class));
    }

    @Override
    public ResponseEntity<?> updateState(Constants.UserState state) {
        String username = SecurityUtils.loginUsername();
        if (userService.updateState(state, username) > 0) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<?> handleAuthSuccess() {
        String username = SecurityUtils.loginUsername();
        userService.handleAuthSuccess(username);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> viewMyProfile() {
        String username = SecurityUtils.loginUsername();
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @Override
    public ResponseEntity<?> sync() {
        userService.synAccountFromKeycloak();
        return ResponseEntity.ok().build();
    }
}
