package fpt.edu.capstone.vms.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Room;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
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
import lombok.experimental.Accessors;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Date;
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
    @PreAuthorize("hasRole('r:ticket:delete')")
    ResponseEntity<?> delete(@PathVariable String id);

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

    @PutMapping("/update")
    @Operation(summary = "Update meeting ticket")
    @PreAuthorize("hasRole('r:ticket:update')")
    ResponseEntity<?> updateMeeting(@RequestBody @Valid UpdateTicketInfo updateTicketInfo);

    @PostMapping("/filter")
    @Operation(summary = "Filter all ticket")
    @PreAuthorize("hasRole('r:ticket:filter')")
    ResponseEntity<?> filterAllTicket(@RequestBody @Valid TicketFilter ticketFilterSite, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/check-in/{checkInCode}")
    @Operation(summary = "Find ticket by qrcode")
    @PreAuthorize("hasRole('r:ticket:findQRCode')")
    ResponseEntity<?> findByQRCode(@PathVariable String checkInCode);

    @GetMapping(value = "/subscribe/check-in", consumes = MediaType.ALL_VALUE)
    SseEmitter subscribeCheckIn();

    @PutMapping("/update-status")
    @Operation(summary = "Update status customer for ticket")
    @PreAuthorize("hasRole('r:ticket:updateStatusCustomer')")
    ResponseEntity<?> updateStatusCustomerOfTicket(@RequestBody @Valid CheckInPayload checkInPayload);

    @GetMapping("/view-detail/{ticketId}")
    @Operation(summary = "View Detail ticket by id")
    @PreAuthorize("hasRole('r:ticket:detail')")
    ResponseEntity<?> viewDetailTicket(@PathVariable UUID ticketId);

    @PostMapping("/check-in/filter")
    @Operation(summary = "Filter ticket and customer ")
    @PreAuthorize("hasRole('r:ticket:findQRCode')")
    ResponseEntity<?> filterTicketAndCustomer(@RequestBody @Valid TicketFilter ticketFilter, Pageable pageable);

    @PostMapping("/room")
    @Operation(summary = "Filter ticket by room ")
    @PreAuthorize("hasRole('r:ticket:room')")
    ResponseEntity<?> filterTicketByRoom(@RequestBody @Valid TicketFilter ticketFilter);

    @PostMapping("/customer/card")
    @Operation(summary = "add card to customer ")
    @PreAuthorize("hasRole('r:ticket:add-card')")
    ResponseEntity<?> addCardToCustomerTicket(@RequestBody @Valid CustomerTicketCardDTO customerTicketCardDTO);

    @GetMapping("/history/{checkInCode}")
    @Operation(summary = "Find all card history of customer")
    @PreAuthorize("hasRole('r:ticket:viewCardCheckInHistory')")
    ResponseEntity<?> getAllCardHistoryOfCustomer(@PathVariable String checkInCode, Pageable pageable);

    @GetMapping("/check-room")
    @Operation(summary = "Check room when create ticket")
    ResponseEntity<?> checkRoom(@RequestBody CheckRoom checkRoom);

    @Data
    class CheckRoom {
        private String roomId;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime startTime;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime endTime;
    }

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

        List<ICustomerController.CustomerInfo> oldCustomers;

        @NotNull
        private boolean draft = true;

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
        private Boolean isBookmark;
        private Integer customerCount;
        List<ICustomerController.CustomerInfo> Customers;
    }

    @Data
    class TicketFilter {
        List<String> names;
        List<String> siteId;
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
    class CancelTicket {
        private Integer reasonId;
        private String reasonNote;
        private UUID ticketId;
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
        private String roomId;
        List<ICustomerController.NewCustomers> newCustomers;
        List<ICustomerController.CustomerInfo> oldCustomers;
        private boolean draft = true;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    class TicketByQRCodeResponseDTO {
        //Ticket Info
        private UUID ticketId;
        private String siteId;
        private String ticketCode;
        private String ticketName;
        private Constants.Purpose purpose;
        private Constants.StatusTicket ticketStatus;
        private Constants.StatusCustomerTicket ticketCustomerStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime startTime;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime endTime;
        private String createBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime createdOn;
        private String lastUpdatedBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
        private LocalDateTime lastUpdatedOn;
        private String checkInCode;

        //Info Room
        private UUID roomId;
        private String roomName;
        private boolean isSecurity;

        //Info customer
        ICustomerController.CustomerInfo customerInfo;
    }

    @Data
    class CheckInPayload {

        @NotNull
        private String checkInCode;

        @NotNull
        private Constants.StatusCustomerTicket status;

        private Integer reasonId;
        private String reasonNote;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class TicketByRoomResponseDTO {
        List<Room> rooms;
        List<Ticket> tickets;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class TicketByRoomResponse {
        List<Room> rooms;
        List<TicketFilterDTO> tickets;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class CustomerTicketCardDTO {
        @NotNull
        String checkInCode;
        @NotNull
        String cardId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class CardCheckInHistoryDTO {
        private Integer id;
        private String checkInCode;
        private String cardId;
        private String macIp;
        private String roomName;
        private String status;
        private Date createdOn;
    }
}
