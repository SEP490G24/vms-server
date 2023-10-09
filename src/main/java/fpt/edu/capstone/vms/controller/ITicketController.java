package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Ticket Service")
@RequestMapping("/api/v1/ticket")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ITicketController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new agent")
//    @PreAuthorize("hasRole('r:user:create')")
    ResponseEntity<?> createTicket(@RequestBody @Valid createTicketInfo ticketInfo);

    @Data
    class createTicketInfo {
        private String visitor_name;
        private String code;
        private Integer identificationNumber;
        private String licensePlateNumber;
        private String email;
        private String phoneNumber;
        private Boolean gender;
        private String purpose;
        private String purposeOther;
        private LocalDateTime expectedDate;
        private String expectedTime;
        private LocalDateTime endTime;
        private String comment;
        private Boolean promise;
        private Boolean privacy;
        private String isBookmark;
        private String username;
    }
}
