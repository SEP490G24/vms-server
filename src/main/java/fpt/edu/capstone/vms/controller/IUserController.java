package fpt.edu.capstone.vms.controller;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Tag(name = "Account Service")
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
////@PreAuthorize("isAuthenticated()")
public interface IUserController {

    @PostMapping("/info/{username}")
    @Operation(summary = "Filter")
//    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> info(@PathVariable String username) throws NotFoundException;

    @PostMapping("/filter")
    @Operation(summary = "Filter")
//    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> filter(@RequestBody UserFilter usernames);

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
    ResponseEntity<?> updateProfile(@RequestBody @Valid CreateUserInfo userInfo) throws NotFoundException;

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
    ResponseEntity<?> export(@RequestBody UserFilter userFilter);

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
        @NotNull
        String phoneNumber;
        @NotNull
        String email;
        @NotNull
        String departmentId;
        LocalDate dateOfBirth;
        @NotNull
        Constants.Gender gender;
        @NotNull
        Boolean enable = true;
    }

    @Data
    class UpdateUserInfo {
        String password;
        @NotNull
        String phoneNumber;
        @NotNull
        String email;
        @NotNull
        Boolean enable;
    }

    @Data
    class UserFilter {
        int pageNumber;
        List<Constants.UserRole> roles;
        List<String> usernames;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
    }

    @Data
    class UpdateState {
        @NotNull
        String username;
        @NotNull
        Boolean enable;
    }
}
