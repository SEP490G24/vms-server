package fpt.edu.capstone.vms.persistence.service;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface IUserService {

    Page<User> filter(int pageNumber,
                      List<String> usernames,
                      List<Constants.UserRole> roles,
                      LocalDateTime createdOnStart,
                      LocalDateTime createdOnEnd,
                      Constants.UserState state);

    List<User> availableUsers();

    List<User> filterAvailableUsers(List<String> usernames);

    User createUser(IUserResource.UserDto userDto);

    User updateUser(IUserResource.UserDto userDto) throws NotFoundException;

    int updateState(Constants.UserState state, String username);

    void handleAuthSuccess(String username);

    void deleteUser(String username);

    User findByUsername(String username);

    void synAccountFromKeycloak();
}
