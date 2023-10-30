package fpt.edu.capstone.vms.controller;


import fpt.edu.capstone.vms.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Account Service")
@RequestMapping("/api/v1/customer")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface ICustomerController {


    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:customer:find')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @PostMapping("")
    @Operation(summary = "Create new customer")
    @PreAuthorize("hasRole('r:customer:create')")
    ResponseEntity<?> create(@RequestBody @Valid NewCustomers createCustomerDto);

    @Data
    class NewCustomers {

        @NotNull(message = "The name of the visitor cannot be null")
        @NotEmpty(message = "The name of the visitor cannot be empty")
        @Size(max = 50, message = "The visitor's name must not exceed 50 characters")
        private String visitor_name;

        @NotEmpty(message = "The identification number of the visitor cannot be empty")
        private Integer identificationNumber;

        @NotNull(message = "Email cannot be null")
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Email is not in the correct format")
        private String email;

        @NotEmpty(message = "The phone number cannot be empty")
        @Pattern(regexp = "^(0[2356789]\\d{8})$", message = "The phone number is not in the correct format")
        private String phoneNumber;

        @NotEmpty(message = "The gender cannot be empty")
        private Constants.Gender gender;

        private String description;

        private Integer provinceId;

        private Integer districtId;

        private Integer communeId;
    }

    @Data
    class CustomerInfo {

        private UUID id;

        private String visitor_name;

        private Integer identificationNumber;

        private String email;

        private String phoneNumber;

        private Constants.Gender gender;

        private String description;

        private Integer provinceId;

        private Integer districtId;

        private Integer communeId;
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
        List<String> roles;
    }

    @Data
    class UserFilterRequest {
        String role;
        List<String> usernames;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
        String departmentId;
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
        String departmentId;
        String departmentName;
        String siteId;
        String siteName;
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
