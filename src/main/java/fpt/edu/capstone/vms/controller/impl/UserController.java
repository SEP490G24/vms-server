package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@AllArgsConstructor
public class UserController implements IUserController {

    private final IUserService userService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> filter(UserFilter filter,boolean isPageable,Pageable pageable) {
        return isPageable ? ResponseEntity.ok(
                userService.filter(
                        pageable,
                        filter.getUsernames(),
                        filter.getRoles(),
                        filter.getCreatedOnStart(),
                        filter.getCreatedOnEnd(),
                        filter.getEnable(),
                        filter.getKeyword(),
                        filter.getDepartmentId())) : ResponseEntity.ok(
            userService.filter(
                filter.getUsernames(),
                filter.getRoles(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getEnable(),
                filter.getKeyword(),
                filter.getDepartmentId()));
    }

    @Override
    public ResponseEntity<?> create(CreateUserInfo userInfo) {
        User userEntity = userService.createUser(mapper.map(userInfo, IUserResource.UserDto.class)
                .setRole(Constants.UserRole.STAFF));
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
    public ResponseEntity<?> updateProfile(@Valid UpdateUserInfo userInfo) throws NotFoundException {
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
    @Override
    public ResponseEntity<?> export(UserFilter userFilter) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "danh_sach_nguoi_dung.xlsx");
        return ResponseEntity.status(HttpStatus.SC_OK).headers(headers).body(userService.export(userFilter));
    }

    @Override
    public ResponseEntity<ByteArrayResource> downloadExcel() throws IOException {
        return userService.downloadExcel();
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePasswordUserDto changePasswordUserDto) {
        try {
            userService.changePasswordUser(changePasswordUserDto);
            return ResponseEntity.ok().build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Object> importUser(MultipartFile file) {
       return userService.importUser(file);
    }

}
