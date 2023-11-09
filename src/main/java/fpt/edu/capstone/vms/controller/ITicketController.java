package fpt.edu.capstone.vms.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import fpt.edu.capstone.vms.constants.Constants;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    ResponseEntity<?> cancelMeeting(@RequestBody @Valid CancelTicket cancelTicket);

    @PostMapping("/update")
    @Operation(summary = "Update meeting ticket")
    ResponseEntity<?> updateMeeting(@RequestBody @Valid UpdateTicketInfo updateTicketInfo);

    @PostMapping("/filter")
    @Operation(summary = "Filter ticket")
    ResponseEntity<?> filter(@RequestBody @Valid TicketFilterUser ticketFilterUser, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @PostMapping("/filter/site")
    @Operation(summary = "Filter ticket in site for admin")
    @PreAuthorize("hasRole('r:ticket:find')")
    ResponseEntity<?> filterAllBySites(@RequestBody @Valid TicketFilterSite ticketFilterSite, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/{ticketId}/customer/{customerId}")
    @Operation(summary = "Find ticket by qrcode")
        //@PreAuthorize("hasRole('r:ticket:findQRCode')")
    ResponseEntity<?> findByQRCode(@PathVariable UUID ticketId, @PathVariable UUID customerId);

    @PutMapping("/update-status")
    @Operation(summary = "Update status of ticket")
    ResponseEntity<?> updateState(@RequestBody @Valid UpdateStatusTicketOfCustomer updateStatusTicketOfCustomer);

    @GetMapping("/{ticketId}")
    @Operation(summary = "Find ticket by id for user")
    ResponseEntity<?> findByIdForUser(@PathVariable UUID ticketId);

    @GetMapping("/admin/{ticketId}")
    @Operation(summary = "Find ticket by id for admin")
    @PreAuthorize("hasRole('r:ticket:viewTicketDetail')")
    ResponseEntity<?> findByIdForAdmin(@PathVariable UUID ticketId);

    @PostMapping("/customer/filter")
    @Operation(summary = "Filter ticket and customer ")
        //@PreAuthorize("hasRole('r:ticket:findQRCode')")
    ResponseEntity<?> filterTicketAndCustomer(@RequestBody @Valid TicketFilterUser ticketFilterUser, Pageable pageable);

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

        private UUID templateId;

        private String siteId;

        List<ICustomerController.NewCustomers> newCustomers;

        List<String> oldCustomers;

        @NotNull
        private boolean draft;

        @NotNull
        private boolean isPassGuard;

        @NotNull
        private boolean isPassReceptionist;
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
        private String siteId;
        List<ICustomerController.CustomerInfo> Customers;
    }

    @Data
    class TicketFilterSite {
        List<String> names;
        String username;
        UUID roomId;
        Constants.StatusTicket status;
        Constants.Purpose purpose;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime createdOnStart;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime createdOnEnd;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime startTimeStart;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime startTimeEnd;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime endTimeStart;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime createdOnStart;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime createdOnEnd;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime startTimeStart;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime startTimeEnd;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        LocalDateTime endTimeStart;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime startTime;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime startTime;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime endTime;
        private String createBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
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
