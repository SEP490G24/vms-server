package fpt.edu.capstone.vms.controller;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Account Service")
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IUserController {

    @PostMapping("/filter")
    @Operation(summary = "Filter")
    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> filter(@RequestBody UserFilterRequest userFilterRequest, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @PostMapping("")
    @Operation(summary = "Create new user")
    @PreAuthorize("hasRole('r:user:create')")
    ResponseEntity<?> create(@RequestBody @Valid CreateUserInfo userInfo);

    @PutMapping("/{username}")
    @Operation(summary = "Update user")
    @PreAuthorize("hasRole('r:user:update')")
    ResponseEntity<?> update(@PathVariable("username") String username, @RequestBody @Valid UpdateUserInfo userInfo) throws NotFoundException;

    @PutMapping("/profile")
    @Operation(summary = "Update my profile")
    ResponseEntity<?> updateProfile(@RequestBody @Valid UpdateProfileUserInfo userInfo) throws NotFoundException;

    @PutMapping("/update-state")
    @Operation(summary = "Update my state")
    ResponseEntity<?> updateState(@RequestBody @Valid UpdateState updateState);

    @GetMapping("/profile")
    @Operation(summary = "View my profile")
    ResponseEntity<?> viewMyProfile();

    @PostMapping("/export")
    @Operation(summary = "Export list of user to excel")
    @PreAuthorize("hasRole('r:user:export')")
    ResponseEntity<?> export(UserFilterRequest userFilterRequest);

    @GetMapping("/import")
    @Operation(summary = "download template import user")
    @PreAuthorize("hasRole('r:user:import')")
    ResponseEntity<ByteArrayResource> downloadExcel() throws IOException;

    @PostMapping("/import")
    @Operation(summary = "Import list of user use excel")
    @PreAuthorize("hasRole('r:user:import')")
    ResponseEntity<Object> importUser(@RequestBody MultipartFile file);

    @PostMapping("/change-password")
    @Operation(summary = "Change Password")
    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> changePassword(@RequestBody ChangePasswordUserDto changePasswordUserDto);

//    @PutMapping("/{username}/role")
//    @Operation(summary = "Update role")
//    @PreAuthorize("hasRole('r:role:update')")
//    ResponseEntity<?> updateRole(@PathVariable("username") String username,
//                                 @RequestBody List<String> roles);

    @Data
    class CreateUserInfo {
        @NotNull
        String username;
        @NotNull
        String password;
        @NotNull
        String firstName;
        @NotNull
        String lastName;
        String phoneNumber;
        String avatar;
        @NotNull
        String email;
        String countryCode;
        @NotNull
        UUID departmentId;
        LocalDate dateOfBirth;
        @NotNull
        Constants.Gender gender;
        Integer provinceId;
        Integer communeId;
        Integer districtId;
        Boolean enable;
        List<String> roles;
    }

    @Data
    class UpdateUserInfo {
        String firstName;
        String lastName;
        String phoneNumber;
        String avatar;
        String email;
        String countryCode;
        UUID departmentId;
        LocalDate dateOfBirth;
        Constants.Gender gender;
        Integer provinceId;
        Integer communeId;
        Integer districtId;
        Boolean enable;
        List<String> roles;
    }

    @Data
    class UpdateProfileUserInfo {
        String firstName;
        String lastName;
        String phoneNumber;
        String avatar;
        String email;
        String countryCode;
        UUID departmentId;
        LocalDate dateOfBirth;
        Constants.Gender gender;
        Integer provinceId;
        Integer communeId;
        Integer districtId;
    }

    @Data
    class UserFilterRequest {
        String role;
        List<String> usernames;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
        List<String> departmentIds;
        List<String> siteId;
        Integer provinceId;
        Integer districtId;
        Integer communeId;
    }

    @Data
    class UserFilterResponse {
        String username;
        String firstName;
        String lastName;
        String phoneNumber;
        String avatar;
        String email;
        String countryCode;
        UUID departmentId;
        String departmentName;
        UUID siteId;
        String siteName;
        Integer provinceId;
        Integer communeId;
        Integer districtId;
        String provinceName;
        String districtName;
        String communeName;
        Date dateOfBirth;
        String gender;
        List<String> roles;
        Date createdOn;
        Date lastUpdatedOn;
        Boolean enable;
    }


    @Data
    class UpdateState {
        @NotNull
        String username;
        @NotNull
        Boolean enable;
    }

    @Data
    class ChangePasswordUserDto {
        @NotNull
        String oldPassword;
        @NotNull
        String newPassword;
    }

    @Data
    class ImportUserInfo {
        @NotNull
        String username;
        @NotNull
        String password;
        @NotNull
        String firstName;
        @NotNull
        String lastName;
        @NotNull
        String phoneNumber;
        @NotNull
        String email;
        @NotNull
        String departmentCode;
        LocalDate dateOfBirth;
        @NotNull
        Constants.Gender gender;
        @NotNull
        Boolean enable;
    }
}
