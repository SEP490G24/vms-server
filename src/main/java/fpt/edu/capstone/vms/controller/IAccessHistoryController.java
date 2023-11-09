package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Access History Service")
@RequestMapping("/api/v1/access-history")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IAccessHistoryController {

    @GetMapping("/{ticketId}/customer/{customerId}")
    @Operation(summary = "Find ticket by qrcode")
        //@PreAuthorize("hasRole('r:ticket:findQRCode')")
    ResponseEntity<?> findByQRCode(@PathVariable UUID ticketId, @PathVariable UUID customerId);

    @PostMapping("")
    @Operation(summary = "Filter access history ")
        //@PreAuthorize("hasRole('r:ticket:findQRCode')")
    ResponseEntity<?> filterAccessHistory(@RequestBody @Valid TicketFilterUser ticketFilterUser, Pageable pageable);

    @Data
    class AccessHistoryFilter {
        private UUID id;
        private String code;
        private String name;
        private String roomName;
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
        List<ICustomerController.CustomerInfo> Customers;
    }

    @Data
    class TicketFilterSite {
        List<String> names;
        String username;
        UUID roomId;
        Constants.StatusTicket status;
        Constants.Purpose purpose;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        LocalDateTime startTimeStart;
        LocalDateTime startTimeEnd;
        LocalDateTime endTimeStart;
        LocalDateTime endTimeEnd;
        String createdBy;
        String lastUpdatedBy;
        String keyword;
    }

    @Data
    class TicketFilterUser {
        List<String> names;
        UUID roomId;
        Constants.StatusTicket status;
        Constants.Purpose purpose;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        LocalDateTime startTimeStart;
        LocalDateTime startTimeEnd;
        LocalDateTime endTimeStart;
        LocalDateTime endTimeEnd;
        String createdBy;
        String lastUpdatedBy;
        Boolean bookmark;
        String keyword;
    }

    @Data
    class CancelTicket {
        private UUID reason;
        private String reasonNote;
        private UUID ticketId;
        private UUID templateId;
    }

    @Data
    class UpdateTicketInfo {
        private UUID id;
        private Constants.Purpose purpose;
        private String purposeNote;
        private String name;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String description;
        private UUID roomId;
        List<ICustomerController.NewCustomers> newCustomers;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class TicketByQRCodeResponseDTO {
        //Ticket Info
        private UUID ticketId;
        private String ticketCode;
        private String ticketName;
        private Constants.Purpose purpose;
        private Constants.StatusTicket ticketStatus;
        private Constants.StatusTicket ticketCustomerStatus;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String createBy;
        private LocalDateTime createdOn;

        //Info Room
        private UUID roomId;
        private String roomName;

        //Info customer
        ICustomerController.CustomerInfo customerInfo;
    }

    @Data
    class UpdateStatusTicketOfCustomer {

        @NotNull
        private UUID ticketId;

        @NotNull
        private UUID customerId;

        @NotNull
        private Constants.StatusTicket status;

        private UUID reasonId;
        private String reasonNote;
    }

}
