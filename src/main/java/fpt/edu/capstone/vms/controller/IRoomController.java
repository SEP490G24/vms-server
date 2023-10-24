package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Room Service")
@RequestMapping("/api/v1/room")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface IRoomController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new agent")
//    @PreAuthorize("hasRole('r:user:create')")
    ResponseEntity<?> create(@RequestBody @Valid RoomDto roomDto);

    @PutMapping("/{id}")
    @Operation(summary = "Update site")
    ResponseEntity<?> update(@RequestBody RoomDto roomDto, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
//    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> filter(@RequestBody @Valid RoomFilterDTO roomFilterDTO, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get all room by siteId")
    ResponseEntity<List<?>> findAllBySiteId(@PathVariable String siteId);

    @Data
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
        private String siteName;

    }

    @Data
    class RoomFilterDTO {
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;


        private String name;
        private String code;
        private UUID siteId;
        private String siteName;
        private String description;
    }
}
