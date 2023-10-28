package fpt.edu.capstone.vms.persistence.service;


import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface IUserService {

    Page<IUserController.UserFilterResponse> filter(Pageable pageable,
                                                    List<String> usernames,
                                                    String role,
                                                    LocalDateTime createdOnStart,
                                                    LocalDateTime createdOnEnd,
                                                    Boolean enable,
                                                    String keyword,
                                                    String department);

    List<IUserController.UserFilterResponse> filter(
        List<String> usernames,
        String role,
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

    //void synAccountFromKeycloak();

    ByteArrayResource export(IUserController.UserFilterRequest userFilter);

    Boolean deleteAvatar(String name, String newImage, String username);

    ResponseEntity<Object> importUser(MultipartFile file);

    ResponseEntity<ByteArrayResource> downloadExcel() throws IOException;

    //void updateRole(String username, List<String> roles);
}
