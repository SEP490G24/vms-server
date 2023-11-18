package fpt.edu.capstone.vms.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Room;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
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
@Tag(name = "Ticket Service")
@RequestMapping("/api/v1/ticket")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface ITicketController {

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

    @PostMapping("/cancel")
    @Operation(summary = "Cancel meeting ticket")
    @PreAuthorize("hasRole('r:ticket:cancel')")
    ResponseEntity<?> cancelMeeting(@RequestBody @Valid CancelTicket cancelTicket);

    @PostMapping("/update")
    @Operation(summary = "Update meeting ticket")
    @PreAuthorize("hasRole('r:ticket:update')")
    ResponseEntity<?> updateMeeting(@RequestBody @Valid UpdateTicketInfo updateTicketInfo);

    @PostMapping("/filter")
    @Operation(summary = "Filter ticket in site for admin")
    ResponseEntity<?> filterAllBySites(@RequestBody @Valid TicketFilter ticketFilterSite, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/check-in/{checkInCode}")
    @Operation(summary = "Find ticket by qrcode")
    @PreAuthorize("hasRole('r:ticket:findQRCode')")
    ResponseEntity<?> findByQRCode(@PathVariable String checkInCode);

    @PutMapping("/check-in")
    @Operation(summary = "Check in customer for ticket")
    @PreAuthorize("hasRole('r:ticket:checkIn')")
    ResponseEntity<?> checkIn(@RequestBody @Valid CheckInPayload checkInPayload);

    @GetMapping("/{ticketId}")
    @Operation(summary = "Find ticket by id for user")
    @PreAuthorize("hasRole('r:ticket:viewTicketDetail')")
    ResponseEntity<?> findByIdForUser(@PathVariable UUID ticketId, @RequestParam(value = "siteId", required = false) String siteId);

    @PostMapping("/customer/filter")
    @Operation(summary = "Filter ticket and customer ")
    @PreAuthorize("hasRole('r:ticket:findQRCode')")
    ResponseEntity<?> filterTicketAndCustomer(@RequestBody @Valid TicketFilter ticketFilter, Pageable pageable);

    @PostMapping("/room")
    @Operation(summary = "Filter ticket by room ")
    @PreAuthorize("hasRole('r:ticket:room')")
    ResponseEntity<?> filterTicketByRoom(@RequestBody @Valid TicketFilter ticketFilter);

    @Data
    class CreateTicketInfo {

        private Constants.Purpose purpose;

        private String purposeNote;

        private String name;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime startTime;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime endTime;

        private String description;

        private UUID roomId;

        private String siteId;

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
        private String name;
        private String roomName;
        private Constants.Purpose purpose;
        private String purposeNote;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime startTime;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime endTime;
        private String comment;
        private Constants.StatusTicket status;
        private String username;
        private UUID roomId;
        private String createdBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime createdOn;
        private String lastUpdatedBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime lastUpdatedOn;
        private String siteId;
        List<ICustomerController.CustomerInfo> Customers;
    }

    @Data
    class TicketFilter {
        List<String> names;
        List<String> sites;
        List<String> usernames;
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
        private UUID reasonId;
        private String reasonNote;
        private UUID ticketId;
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
        private String siteId;
        private String ticketCode;
        private String ticketName;
        private Constants.Purpose purpose;
        private Constants.StatusTicket ticketStatus;
        private Constants.StatusTicket ticketCustomerStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime startTime;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime endTime;
        private String createBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime createdOn;
        private String checkInCode;

        //Info Room
        private UUID roomId;
        private String roomName;

        //Info customer
        ICustomerController.CustomerInfo customerInfo;
    }

    @Data
    class CheckInPayload {

        @NotNull
        private UUID ticketId;

        @NotNull
        private UUID customerId;

        @NotNull
        private String checkInCode;

        @NotNull
        private Constants.StatusTicket status;

        private UUID reasonId;
        private String reasonNote;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class TicketByRoomResponseDTO {
        List<Room> rooms;
        List<TicketFilterDTO> tickets;
    }
}
