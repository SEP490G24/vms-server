package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
public class UserController implements IUserController {

    private final IUserService userService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> filter(UserFilterRequest filter, boolean isPageable, Pageable pageable) {
        return isPageable ? ResponseEntity.ok(
            userService.filter(
                pageable,
                filter.getUsernames(),
                filter.getRole(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getEnable(),
                filter.getKeyword(),
                filter.getDepartmentId(),
                filter.getSiteId(),
                filter.getProvinceId(),
                filter.getDistrictId(),
                filter.getCommuneId())) : ResponseEntity.ok(
            userService.filter(
                filter.getUsernames(),
                filter.getRole(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getEnable(),
                filter.getKeyword(),
                filter.getDepartmentId(),
                filter.getSiteId(),
                filter.getProvinceId(),
                filter.getDistrictId(),
                filter.getCommuneId()));
    }

    @Override
    public ResponseEntity<?> create(CreateUserInfo userInfo) {
        User userEntity = userService.createUser(mapper.map(userInfo, IUserResource.UserDto.class));
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
    public ResponseEntity<?> updateProfile(@Valid UpdateProfileUserInfo userInfo) throws NotFoundException {
        String username = SecurityUtils.loginUsername();
        User userEntity = userService.updateUser(mapper.map(userInfo, IUserResource.UserDto.class).setUsername(username));
        return ResponseEntity.ok(mapper.map(userEntity, IUserResource.UserDto.class));
    }

    @Override
    public ResponseEntity<?> updateState(UpdateState updateState) {
        if (userService.updateState(updateState.getEnable(), updateState.getUsername()) > 0) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<?> viewMyProfile() {
        String username = SecurityUtils.loginUsername();
        return ResponseEntity.ok(mapper.map(userService.findByUsername(username), ProfileUser.class));
    }

    @Override
    public ResponseEntity<?> export(UserFilterRequest userFilter) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "users.xlsx");
        return ResponseEntity.status(HttpStatus.SC_OK).headers(headers).body(userService.export(userFilter));
    }

    @Override
    public ResponseEntity<ByteArrayResource> downloadExcel() throws IOException {
        return userService.downloadExcel();
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePasswordUserDto changePasswordUserDto) {
        try {
            String username = SecurityUtils.loginUsername();
            userService.changePasswordUser(username, changePasswordUserDto.getOldPassword(), changePasswordUserDto.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

//    @Override
//    public ResponseEntity<?> updateRole(String username, List<String> roles) {
//        userService.updateRole(username, roles);
//        return ResponseEntity.ok().build();
//    }

    @Override
    public ResponseEntity<Object> importUser(MultipartFile file) {
        return userService.importUser(file);
    }

}
