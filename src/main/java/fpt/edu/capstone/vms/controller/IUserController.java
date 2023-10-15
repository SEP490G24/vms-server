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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@Tag(name = "Account Service")
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
////@PreAuthorize("isAuthenticated()")
public interface IUserController {

    @PostMapping("/filter")
    @Operation(summary = "Filter")
//    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> filter(@RequestBody @Valid UserFilter usernames, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @PostMapping("")
    @Operation(summary = "Create new user")
    //@PreAuthorize("hasRole('r:user:create')")
    ResponseEntity<?> create(@RequestBody @Valid CreateUserInfo userInfo);

    @PutMapping("/{username}")
    @Operation(summary = "Update user")
//    @PreAuthorize("hasRole('r:user:update')")
    ResponseEntity<?> update(@PathVariable("username") String username, @RequestBody @Valid UpdateUserInfo userInfo) throws NotFoundException;

    @PutMapping("/profile")
    @Operation(summary = "Update my profile")
    ResponseEntity<?> updateProfile(@RequestBody @Valid UpdateUserInfo userInfo) throws NotFoundException;

    @PutMapping("/update-state")
    @Operation(summary = "Update my state")
    ResponseEntity<?> updateState(@RequestBody @Valid UpdateState updateState);

    @GetMapping("/handle-auth-success")
    @Operation(summary = "Handle event login success")
    ResponseEntity<?> handleAuthSuccess();

    @GetMapping("/profile")
    @Operation(summary = "View my profile")
    ResponseEntity<?> viewMyProfile();

    @GetMapping("/sync")
    @Operation(summary = "Sync account between keycloak & database")
//    @PreAuthorize("hasRole('r:user:sync')")
    ResponseEntity<?> sync();

    @GetMapping("/export")
    @Operation(summary = "Export list of user to excel")
    ResponseEntity<?> export(UserFilter userFilter);

    @GetMapping("/import")
    @Operation(summary = "download template import user")
    ResponseEntity<ByteArrayResource> downloadExcel() throws IOException;

    @PostMapping("/import")
    @Operation(summary = "Import list of user use excel")
    ResponseEntity<Object> importUser(@RequestBody MultipartFile file);

    @PostMapping("/change-password")
    @Operation(summary = "Change Password")
//    @PreAuthorize("hasRole('r:user:find')")
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
        Boolean enable;
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
        Boolean enable;
    }

    @Data
    class UserFilter {
        List<Constants.UserRole> roles;
        List<String> usernames;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
        String departmentId;

        String username;
        String firstName;
        String lastName;
        String phoneNumber;
        String avatar;
        String email;
        String countryCode;
        String departmentName;
        Date dateOfBirth;
        String gender;
        String roleName;
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
