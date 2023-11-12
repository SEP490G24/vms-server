package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;


@RestController
@Tag(name = "Access History Service")
@RequestMapping("/api/v1/access-history")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IAccessHistoryController {

    @GetMapping("/{ticketId}/customer/{customerId}")
    @Operation(summary = "View detail access history")
    @PreAuthorize("hasRole('r:access-history:detail')")
    ResponseEntity<?> viewDetailAccessHistory(@PathVariable UUID ticketId, @PathVariable UUID customerId);

    @PostMapping("")
    @Operation(summary = "Filter access history ")
    @PreAuthorize("hasRole('r:access-history:filter')")
    ResponseEntity<?> filterAccessHistory(@RequestBody AccessHistoryFilter ticketFilterUser, Pageable pageable);

    @PostMapping("/export")
    @Operation(summary = "Export access histories")
    @PreAuthorize("hasRole('r:access-history:export')")
    ResponseEntity<?> export(AccessHistoryFilter ticketFilterUser) throws JRException;

    @Data
    class AccessHistoryFilter {
        private String keyword;
        private LocalDateTime formCheckInTime;
        private LocalDateTime toCheckInTime;
        private LocalDateTime formCheckOutTime;
        private LocalDateTime toCheckOutTime;
        private Constants.StatusTicket status;
        String site;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class AccessHistoryResponseDTO {

        private UUID id;

        //Ticket Info
        private UUID ticketId;
        private String ticketCode;
        private String ticketName;
        private Constants.Purpose purpose;
        private Constants.StatusTicket ticketStatus;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String createBy;
        private LocalDateTime createdOn;

        //Info Room
        private UUID roomId;
        private String roomName;

        //Info customer
        ICustomerController.CustomerInfo customerInfo;

        //access history
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private Constants.StatusTicket ticketCustomerStatus;
    }
}
