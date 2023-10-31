package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    ResponseEntity<?> delete(@PathVariable String id);

    @GetMapping
    @Operation(summary = "Get all ticket")
    @PreAuthorize("hasRole('r:ticket:find')")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new ticket")
    @PreAuthorize("hasRole('r:ticket:create')")
    ResponseEntity<?> create(@RequestBody @Valid CreateTicketInfo ticketInfo);

    @PostMapping("/bookmark")
    @Operation(summary = "Set bookmark ticket")
    ResponseEntity<?> updateBookmark(@RequestBody @Valid TicketBookmark ticketBookmark);

    @Data
    class CreateTicketInfo {

        private Constants.Purpose purpose;

        private String purposeNote;

        private String name;

        private LocalDateTime startTime;

        private LocalDateTime endTime;

        private String description;

        private UUID roomId;

        private UUID templateId;

        List<ICustomerController.NewCustomers> newCustomers;

        List<String> oldCustomers;

        @NotNull
        private boolean draft;
    }

    @Data
    class TicketBookmark {

        @NotNull
        @NotEmpty
        private String ticketId;

        @NotNull
        @NotEmpty
        private boolean bookmark;
    }

    @Data
    class TicketFilterDTO {
        private UUID id;
        private String code;
        private Constants.Purpose purpose;
        private String purposeNote;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String comment;
        private Constants.StatusTicket status;
        private String username;
        private UUID roomId;
        private String createdBy;
        private LocalDateTime createdOn;
        private String lastUpdatedBy;
        private LocalDateTime lastUpdatedOn;
    }
}
