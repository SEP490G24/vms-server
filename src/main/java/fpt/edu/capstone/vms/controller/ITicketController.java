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
@Tag(name = "Ticket Service")
@RequestMapping("/api/v1/ticket")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface ITicketController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id ticket")
    @PreAuthorize("hasRole('r:ticket:find')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket")
    @PreAuthorize("hasRole('r:ticket:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all ticket")
    @PreAuthorize("hasRole('r:ticket:find')")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new ticket")
    @PreAuthorize("hasRole('r:ticket:create')")
    ResponseEntity<?> create(@RequestBody @Valid CreateTicketInfo ticketInfo);

    @Data
    class CreateTicketInfo {

        @NotNull(message = "The name of the visitor cannot be null")
        @NotEmpty(message = "The name of the visitor cannot be empty")
        @Size(max = 50, message = "The visitor's name must not exceed 50 characters")
        private String visitor_name;

        @NotNull(message = "Meeting code cannot be null")
        @NotEmpty(message = "Meeting code cannot be empty")
        @Size(max = 15, message = "Meeting code has a maximum length of 15 characters")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Meeting code must contain only alphanumeric characters")
        private String code;

        @NotEmpty(message = "The identification number of the visitor cannot be empty")
        private Integer identificationNumber;

        @NotEmpty(message = "The license plate number of the visitor cannot be empty")
        private String licensePlateNumber;

        @NotNull(message = "Email cannot be null")
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Email is not in the correct format")
        private String email;

        @NotEmpty(message = "The phone number cannot be empty")
        @Pattern(regexp = "^(0[2356789]\\d{8})$", message = "The phone number is not in the correct format")
        private String phoneNumber;

        @NotEmpty(message = "The gender cannot be empty")
        private Constants.Gender gender;

        @NotEmpty
        private String purpose;

        @NotEmpty
        private String purposeOther;

        @NotNull(message = "Start time cannot be null")
        private LocalDateTime startTime;

        @NotNull(message = "End time cannot be null")
        private LocalDateTime endTime;

        private String comment;

        private String isBookmark;

        @NotNull(message = "Room cannot be null")
        @NotEmpty(message = "Room cannot be empty")
        private UUID roomId;

        @NotEmpty(message = "Template cannot be empty")
        private UUID templateId;

        @NotNull
        private Constants.StatusTicket status;
    }
}
