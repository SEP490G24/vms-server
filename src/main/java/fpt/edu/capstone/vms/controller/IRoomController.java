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
@Tag(name = "Room Service")
@RequestMapping("/api/v1/room")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IRoomController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:room:find')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete")
    @PreAuthorize("hasRole('r:room:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all")
    @PreAuthorize("hasRole('r:room:find')")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new agent")
    @PreAuthorize("hasRole('r:room:create')")
    ResponseEntity<?> create(@RequestBody @Valid RoomDto roomDto);

    @PutMapping("/{id}")
    @Operation(summary = "Update room")
    @PreAuthorize("hasRole('r:room:update')")
    ResponseEntity<?> update(@RequestBody UpdateRoomDto roomDto, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
    @PreAuthorize("hasRole('r:room:find')")
    ResponseEntity<?> filter(@RequestBody @Valid RoomFilterDTO roomFilterDTO, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get all room by siteId")
    @PreAuthorize("hasRole('r:room:find')")
    ResponseEntity<List<?>> findAllBySiteId(@PathVariable String siteId);

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class RoomDto {
        @NotNull
        private String code;
        @NotNull
        private String name;
        private String description;
        @NotNull
        private Boolean enable;
        @NotNull
        private UUID siteId;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class UpdateRoomDto {
        private String code;
        private String name;
        private String description;
        private Boolean enable;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class RoomFilterDTO {
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
        String createBy;
        List<String> siteId;

    }

    @Data
    class RoomFilterResponse {
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
        private Boolean enable;
    }
}
