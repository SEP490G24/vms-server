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
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
    @PreAuthorize("hasRole('r:customer:detail')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    @PreAuthorize("hasRole('r:customer:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter customer")
    @PreAuthorize("hasRole('r:customer:filter')")
    ResponseEntity<?> filter(@RequestBody @Valid CustomerFilter filter, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @PostMapping("/available")
    @Operation(summary = "Find all by organization id")
    @PreAuthorize("hasRole('r:customer:filter')")
    ResponseEntity<?> findByOrganizationId(@RequestBody CustomerAvailablePayload customerAvailablePayload);

    @PostMapping("/check")
    @Operation(summary = "check exist customer")
    ResponseEntity<?> checkCustomerExist(@RequestBody CustomerCheckExist customerCheckExist);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class NewCustomers {

        @NotNull(message = "The name of the visitor cannot be null")
        @NotEmpty(message = "The name of the visitor cannot be empty")
        @Size(max = 50, message = "The visitor's name must not exceed 50 characters")
        private String visitorName;

        @NotEmpty(message = "The identification number of the visitor cannot be empty")
        @Pattern(regexp = "\\d{12}", message = "The phone number is not in the correct format")
        private String identificationNumber;

        @NotNull(message = "Email cannot be null")
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Email is not in the correct format")
        private String email;

        @NotEmpty(message = "The phone number cannot be empty")
        @Pattern(regexp = "^(0[2356789]\\d{10})$", message = "The phone number is not in the correct format")
        private String phoneNumber;

        @NotEmpty(message = "The gender cannot be empty")
        private Constants.Gender gender;

        private String description;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    class CustomerInfo {
        private UUID id;
        private String visitorName;
        private String identificationNumber;
        private String email;
        private String organizationId;
        private String phoneNumber;
        private Constants.Gender gender;
        private String description;

        private String createdBy;

        private String lastUpdatedBy;

        private LocalDateTime lastUpdatedOn;

        private LocalDateTime createdOn;
    }

    @Data
    class CustomerFilter {
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        String createBy;
        String lastUpdatedBy;
        String keyword;
        String identificationNumber;
    }

    @Data
    @Builder
    class CustomerAvailablePayload {
        LocalDateTime startTime;
        LocalDateTime endTime;
    }

    @Data
    class CustomerCheckExist {
        String value;
        Constants.CustomerCheckType type;
    }
}
