package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Card Service")
@RequestMapping("/api/v1/card")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface ICardController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete")
    @PreAuthorize("hasRole('r:card:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new card")
    @PreAuthorize("hasRole('r:card:create')")
    ResponseEntity<?> create(@RequestBody @Valid CardDto cardDto);

    @PutMapping("/{id}")
    @Operation(summary = "Update card")
    @PreAuthorize("hasRole('r:card:update')")
    ResponseEntity<?> update(@RequestBody CardDto cardDto, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
    ResponseEntity<?> filter(@RequestBody @Valid CardFilterDTO cardFilterDTO, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get all card by siteId")
    ResponseEntity<List<?>> findAllBySiteId(@PathVariable UUID siteId);

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class CardDto {
        private UUID id;
        @NotNull
        private UUID cardId;
        @NotNull
        private UUID customerId;
        @NotNull
        private UUID meetingId;
        private LocalDateTime recordingTime;
        private LocalDateTime readingTime;
        @NotNull
        private UUID siteId;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class CardFilterDTO {
        List<String> names;
        LocalDateTime fromDate;
        LocalDateTime toDate;
        String keyword;
        UUID siteId;

    }

    @Data
    class CardFilterResponse {
        private UUID id;
        private UUID cardId;
        private UUID meetingId;
        private String meetingName;
        private UUID customerId;
        private String customerName;
        private UUID siteId;
        private String siteName;
        private LocalDateTime recordingTime;
        private LocalDateTime readingTime;
        private String createdBy;
        private String lastUpdatedBy;
    }
}
