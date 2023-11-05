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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Template Service")
@RequestMapping("/api/v1/template")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ITemplateController {

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
    ResponseEntity<?> create(@RequestBody @Valid TemplateDto templateDto);

    @PutMapping("/{id}")
    @Operation(summary = "Update site")
    ResponseEntity<?> update(@RequestBody TemplateDto templateDto, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
//    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> filter(@RequestBody @Valid TemplateFilterDTO templateFilterDTO, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get all room by siteId")
    ResponseEntity<List<?>> findAllBySiteId(@PathVariable UUID siteId);

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class TemplateDto {
        private UUID id;
        @NotNull
        private String code;
        @NotNull
        private String name;
        @NotNull
        private String subject;
        @NotNull
        private String body;
        @NotNull
        Constants.TemplateType type;
        private String description;
        @NotNull
        private Boolean enable;
        @NotNull
        private UUID siteId;
        private String siteName;

    }

    @Data
    class TemplateFilterDTO {
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;

        private String name;
        private String code;
        private String subject;
        private String body;
        private String description;
        Constants.TemplateType type;
        private UUID siteId;
        private String siteName;
    }
}
