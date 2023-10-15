package fpt.edu.capstone.vms.persistence.service;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public interface IUserService {

    Page<IUserController.UserFilter> filter(Pageable pageable,
                                            List<String> usernames,
                                            List<Constants.UserRole> roles,
                                            LocalDateTime createdOnStart,
                                            LocalDateTime createdOnEnd,
                                            Boolean enable,
                                            String keyword,
                                            String department);

    List<IUserController.UserFilter> filter(
                                            List<String> usernames,
                                            List<Constants.UserRole> roles,
                                            LocalDateTime createdOnStart,
                                            LocalDateTime createdOnEnd,
                                            Boolean enable,
                                            String keyword,
                                            String department);

    User createUser(IUserResource.UserDto userDto);

    User updateUser(IUserResource.UserDto userDto) throws NotFoundException;

    void changePasswordUser(IUserController.ChangePasswordUserDto userDto);

    int updateState(boolean isEnable, String username);

    void handleAuthSuccess(String username);

    void deleteUser(String username);

    User findByUsername(String username);

    void synAccountFromKeycloak();

    ByteArrayResource export(IUserController.UserFilter userFilter);

    Boolean deleteAvatar(String name, String newImage, String username);

    ResponseEntity<Object> importUser(MultipartFile file);

    ResponseEntity<ByteArrayResource> downloadExcel() throws IOException;
}
