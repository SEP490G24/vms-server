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
import org.springframework.web.bind.annotation.*;
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
    @PreAuthorize("hasRole('r:user:filter')")
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

    @GetMapping("/profile")
    @Operation(summary = "View my profile")
    ResponseEntity<?> viewMyProfile();

    @PostMapping("/export")
    @Operation(summary = "Export list of user to excel")
    @PreAuthorize("hasRole('r:user:export')")
    ResponseEntity<?> export(@RequestBody UserFilterRequest userFilterRequest);

    @GetMapping("/import")
    @Operation(summary = "download template import user")
    @PreAuthorize("hasRole('r:user:download-template')")
    ResponseEntity<ByteArrayResource> downloadExcel() throws IOException;

    @PostMapping("/import")
    @Operation(summary = "Import list of user use excel")
    @PreAuthorize("hasRole('r:user:import')")
    ResponseEntity<Object> importUser(@RequestParam(value = "siteId", required = false) String siteId, @RequestBody MultipartFile file);

    @PostMapping("/change-password")
    @Operation(summary = "Change Password")
    ResponseEntity<?> changePassword(@RequestBody ChangePasswordUserDto changePasswordUserDto);


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
        LocalDate dateOfBirth;
        Constants.Gender gender;
        Integer provinceId;
        Integer communeId;
        Integer districtId;
        String address;
    }

    @Data
    class UserFilterRequest {
        String role;
        List<String> usernames;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
        List<String> departmentId;
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
        String roleName;
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

    @Data
    class ProfileUser {

        //personal info
        String username;
        String firstName;
        String lastName;
        Constants.Gender gender;
        //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDate dateOfBirth;

        //contact info
        String phoneNumber;
        String email;

        //address info
        Integer provinceId;
        Integer communeId;
        Integer districtId;
        String address;
        String role;

        //more info
        String avatar;
        UUID siteId;
        String siteName;
        UUID departmentId;
        String departmentName;
    }
}
