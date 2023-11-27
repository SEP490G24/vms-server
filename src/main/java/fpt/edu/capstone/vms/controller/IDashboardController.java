package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@Tag(name = "Dashboard Service")
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IDashboardController {

    @PostMapping("/ticket/purpose/pie")
    @Operation(summary = "Statistics of meetings by purpose with pie")
    ResponseEntity<?> countTicketsByPurposeWithPie(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/ticket/purpose/multi-line")
    @Operation(summary = "Statistics of meetings by purpose with multi line")
    ResponseEntity<?> countTicketsByPurposeByWithMultiLine(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/ticket/status")
    @Operation(summary = "Statistics number of ticket by status")
    ResponseEntity<?> countTicketsByStatus(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/ticket/status/stacked-column")
    @Operation(summary = "Statistics number of ticket by status with stacked column")
    ResponseEntity<?> countTicketsByStatusWithStackedColumn(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/visits/status")
    @Operation(summary = "Statistics number of visits to the building")
    ResponseEntity<?> countVisitsByStatus(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/visits/status/stacked-column")
    @Operation(summary = "Statistics number of visits by status with stacked column")
    ResponseEntity<?> countVisitsByStatusWithStackedColumn(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/tickets/period")
    @Operation(summary = "Statistics number of ticket in period")
    ResponseEntity<?> countTicketsPeriod(@RequestBody DashboardDTO dashboardDTO);

    @Data
    class DashboardDTO {
        private Integer year;
        private Integer month;
        private List<Constants.StatusTicket> status;
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
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class TotalTicketResponse {
        private int totalTicket;
        private int totalTicketWithCondition;
        private int totalCompletedTicket;
        private int totalCompletedTicketWithCondition;
        private int totalCancelTicket;
        private int totalCancelTicketWithCondition;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class TotalVisitsResponse {
        private int totalVisits;
        private int totalVisitsWithCondition;
        private int totalAcceptanceVisits;
        private int totalAcceptanceVisitsWithCondition;
        private int totalRejectVisits;
        private int totalRejectVisitsWithCondition;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class TicketsPeriodResponse {
        private List<Ticket> upcomingMeetings;
        private List<Ticket> ongoingMeetings;
        private List<Ticket> recentlyFinishedMeetings;
    }

}
