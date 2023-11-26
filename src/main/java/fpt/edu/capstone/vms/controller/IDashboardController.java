package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Dashboard Service")
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IDashboardController {

    @PostMapping("/purpose/pie")
    @Operation(summary = "Statistics of meetings by purpose")
    ResponseEntity<?> countTicketsByPurposeWithPie(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/purpose/dual-line")
    @Operation(summary = "Statistics of meetings by purpose")
    ResponseEntity<?> countTicketsByPurposeByWithMultiLine(@RequestBody DashboardDTO dashboardDTO, @QueryParam("limit") String limit);


    @Data
    class DashboardDTO {
        private LocalDateTime fromTime;
        private LocalDateTime toTime;
        private Integer year;
        private Integer month;
        List<String> sites;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class PurposePieResponse {
        private Constants.Purpose type;
        private int value;
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
        private Date startTime;
        private Date endTime;
        private String createBy;
        private Date createdOn;

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

        //access history

        private Date checkInTime;
        private Date checkOutTime;
        private Constants.StatusTicket ticketCustomerStatus;
    }
}
