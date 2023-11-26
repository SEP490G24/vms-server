package fpt.edu.capstone.vms.persistence.service;


import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
                                                    List<String> departmentIds,
                                                    List<String> siteIds,
                                                    Integer provinceId,
                                                    Integer districtId,
                                                    Integer communeId);

    List<IUserController.UserFilterResponse> filter(
        List<String> usernames,
        String role,
        LocalDateTime createdOnStart,
        LocalDateTime createdOnEnd,
        Boolean enable,
        String keyword,
        List<String> departmentIds,
        List<String> siteIds,
        Integer provinceId,
        Integer districtId,
        Integer communeId);

    User createUser(IUserResource.UserDto userDto);

    User createAdmin(IUserResource.UserDto userDto);

    User updateUser(IUserResource.UserDto userDto) throws NotFoundException;

    void changePasswordUser(String username, String oldPassword, String newPassword);

    User findByUsername(String username);

    Boolean deleteAvatar(String name, String newImage, String username);


}
