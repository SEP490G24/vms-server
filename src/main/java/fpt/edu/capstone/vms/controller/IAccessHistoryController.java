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
import org.springframework.web.bind.annotation.CrossOrigin;
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
@Tag(name = "Access History Service")
@RequestMapping("/api/v1/access-history")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IAccessHistoryController {

    @GetMapping("/{checkInCode}")
    @Operation(summary = "View detail access history")
    @PreAuthorize("hasRole('r:access-history:detail')")
    ResponseEntity<?> viewDetailAccessHistory(@PathVariable String checkInCode);

    @PostMapping("")
    @Operation(summary = "Filter access history ")
    @PreAuthorize("hasRole('r:access-history:filter')")
    ResponseEntity<?> filterAccessHistory(@RequestBody AccessHistoryFilter ticketFilterUser, Pageable pageable);

    @PostMapping("/export")
    @Operation(summary = "Export access histories")
    @PreAuthorize("hasRole('r:access-history:export')")
    ResponseEntity<?> export(@RequestBody AccessHistoryFilter ticketFilterUser) throws JRException;

    @Data
    class AccessHistoryFilter {
        private String keyword;
        private LocalDateTime formCheckInTime;
        private LocalDateTime toCheckInTime;
        private LocalDateTime formCheckOutTime;
        private LocalDateTime toCheckOutTime;
        private List<Constants.StatusCustomerTicket> status;
        List<String> sites;
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
        private UUID customerId;
        private String visitorName;
        private String identificationNumber;
        private String email;
        private String phoneNumber;
        private Constants.Gender gender;
        private String description;
        private String cardId;
        private String checkInCode;

        //access history

        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private Constants.StatusCustomerTicket ticketCustomerStatus;

        //site
        private String siteId;
        private String siteName;

    }
}
