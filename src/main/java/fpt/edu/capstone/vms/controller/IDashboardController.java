package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
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

    @PostMapping("/purpose/pie")
    @Operation(summary = "Statistics of meetings by purpose with pie")
    ResponseEntity<?> countTicketsByPurposeWithPie(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/purpose/multi-line")
    @Operation(summary = "Statistics of meetings by purpose with multi line")
    ResponseEntity<?> countTicketsByPurposeByWithMultiLine(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/ticket")
    @Operation(summary = "Statistics number of ticket by status")
    ResponseEntity<?> countTicketsByStatus(@RequestBody DashboardDTO dashboardDTO);

    @PostMapping("/visits")
    @Operation(summary = "Statistics number of visits to the building")
    ResponseEntity<?> countVisitsByStatus(@RequestBody DashboardDTO dashboardDTO);


    @Data
    class DashboardDTO {
        private LocalDateTime fromTime;
        private LocalDateTime toTime;
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

}
