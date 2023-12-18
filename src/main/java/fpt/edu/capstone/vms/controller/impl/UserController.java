package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.persistence.service.excel.ExportUser;
import fpt.edu.capstone.vms.persistence.service.excel.ImportUser;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
public class UserController implements IUserController {

    private final IUserService userService;
    private final ImportUser importUser;
    private final ExportUser exportUser;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> filter(UserFilterRequest filter, boolean isPageable, Pageable pageable) {

        if (isPageable) {
            Page<UserFilterResponse> userFilterResponsePage = userService.filter(
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
                filter.getCommuneId()
            );

            List<UserFilterResponse> userFilter = mapper.map(userFilterResponsePage.getContent(), new TypeToken<List<UserFilterResponse>>() {
            }.getType());
            ;
            userFilter.removeIf(userFilterResponse -> (SecurityUtils.loginUsername().equals(userFilterResponse.getUsername())));
            long remainingElements = userFilterResponsePage.getTotalElements() - userFilter.size();
            return ResponseEntity.ok(new PageImpl(userFilter, pageable, remainingElements));

        } else {
            List<UserFilterResponse> userFilterResponseList = userService.filter(
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
                filter.getCommuneId()
            );

            userFilterResponseList.removeIf(userFilterResponse -> (SecurityUtils.loginUsername().equals(userFilterResponse.getUsername())));
            return ResponseEntity.ok(userFilterResponseList);
        }

    }

    @Override
    public ResponseEntity<?> create(CreateUserInfo userInfo) {
        try {
            User userEntity = userService.createUser(mapper.map(userInfo, IUserResource.UserDto.class));
            return ResponseUtils.getResponseEntityStatus(mapper.map(userEntity, IUserResource.UserDto.class));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> update(String username, @Valid UpdateUserInfo userInfo) throws NotFoundException {
        try {
            User userEntity = userService.updateUser(
                mapper.map(userInfo, IUserResource.UserDto.class)
                    .setUsername(username));
            return ResponseUtils.getResponseEntityStatus(mapper.map(userEntity, IUserResource.UserDto.class));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateProfile(@Valid UpdateProfileUserInfo userInfo) throws NotFoundException {
        try {
            String username = SecurityUtils.loginUsername();
            User userEntity = userService.updateUser(mapper.map(userInfo, IUserResource.UserDto.class).setUsername(username));
            return ResponseEntity.ok(mapper.map(userEntity, IUserResource.UserDto.class));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> viewMyProfile() {
        try {
            String username = SecurityUtils.loginUsername();
            return ResponseEntity.ok(mapper.map(userService.findByUsername(username), ProfileUser.class));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> export(UserFilterRequest userFilter) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "users.xlsx");
            return ResponseEntity.status(HttpStatus.SC_OK).headers(headers).body(exportUser.export(userFilter));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> downloadExcel() {
        return importUser.downloadExcel();
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePasswordUserDto changePasswordUserDto) {
        try {
            String username = SecurityUtils.loginUsername();
            userService.changePasswordUser(username, changePasswordUserDto.getOldPassword(), changePasswordUserDto.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Object> importUser(String siteId, MultipartFile file) {
        try {
            return importUser.importUser(siteId, file);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
