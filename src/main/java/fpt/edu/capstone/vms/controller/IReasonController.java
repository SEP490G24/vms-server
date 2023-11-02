package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
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
@Tag(name = "Reason Service")
@RequestMapping("/api/v1/reason")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IReasonController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete")
    @PreAuthorize("hasRole('r:reason:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new reason")
    @PreAuthorize("hasRole('r:reason:create')")
    ResponseEntity<?> create(@RequestBody @Valid ReasonDto reasonDto);

    @PutMapping("/{id}")
    @Operation(summary = "Update reason")
    @PreAuthorize("hasRole('r:reason:update')")
    ResponseEntity<?> update(@RequestBody ReasonDto reasonDto, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
    ResponseEntity<?> filter(@RequestBody @Valid ReasonFilterDTO reasonFilterDTO, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get all reason by siteId")
    ResponseEntity<List<?>> findAllBySiteId(@PathVariable UUID siteId);

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class ReasonDto {
        private UUID id;
        @NotNull
        private String code;
        @NotNull
        private String name;
        @NotNull
        Constants.Reason type;
        private String description;
        @NotNull
        private Boolean enable;
        @NotNull
        private UUID siteId;
        private String siteName;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class ReasonFilterDTO {
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
        UUID siteId;

    }

    @Data
    class ReasonFilterResponse {
        private UUID id;
        private String name;
        private String code;
        private UUID siteId;
        private String siteName;
        private String description;
        private String createdBy;
        private String lastUpdatedBy;
        private LocalDateTime lastUpdatedOn;
        private LocalDateTime createdOn;
    }
}
