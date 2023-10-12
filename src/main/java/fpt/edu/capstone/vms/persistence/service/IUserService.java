package fpt.edu.capstone.vms.persistence.service;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface IUserService {

    Page<User> filter(int pageNumber,
                      List<String> usernames,
                      List<Constants.UserRole> roles,
                      LocalDateTime createdOnStart,
                      LocalDateTime createdOnEnd,
                      Boolean enable,
                      String keyword);

    User createUser(IUserResource.UserDto userDto);

    User updateUser(IUserResource.UserDto userDto) throws NotFoundException;

    void changePasswordUser(IUserController.ChangePasswordUserDto userDto);

    int updateState(boolean isEnable, String username);

    void handleAuthSuccess(String username);

    void deleteUser(String username);

    User findByUsername(String username);

    void synAccountFromKeycloak();

    ByteArrayResource export(IUserController.UserFilter userFilter);

    void deleteAvatar(String name, String username);
}
