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
@Tag(name = "Template Service")
@RequestMapping("/api/v1/template")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ITemplateController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:template:detail')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete")
    @PreAuthorize("hasRole('r:template:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @PostMapping()
    @PreAuthorize("hasRole('r:template:create')")
    ResponseEntity<?> create(@RequestBody @Valid TemplateDto templateDto);

    @PutMapping("/{id}")
    @Operation(summary = "Update site")
    @PreAuthorize("hasRole('r:template:update')")
    ResponseEntity<?> update(@RequestBody UpdateTemplateDto templateDto, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
    @PreAuthorize("hasRole('r:template:filter')")
    ResponseEntity<?> filter(@RequestBody @Valid TemplateFilterDTO templateFilterDTO, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get all template by siteId")
    @PreAuthorize("hasRole('r:template:filter')")
    ResponseEntity<?> findAllBySiteId(@PathVariable String siteId);

    @GetMapping("/site/{siteId}/{type}")
    @Operation(summary = "Get all template by siteId and type")
    @PreAuthorize("hasRole('r:template:filter')")
    ResponseEntity<?> findAllBySiteIdAndType(@PathVariable String siteId, @PathVariable Constants.TemplateType type);

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class TemplateDto {
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

        private UUID siteId;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class UpdateTemplateDto {
        private String name;
        private String subject;
        private String body;
        Constants.TemplateType type;
        private Boolean enable;
        private String description;
    }

    @Data
    class TemplateFilterDTO {
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
        Constants.TemplateType type;
        List<String> siteId;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class TemplateFilter {
        private UUID id;
        private String code;
        private String name;
        private String subject;
        private String body;
        Constants.TemplateType type;
        private String description;
        private Boolean enable;
        private UUID siteId;
        private String siteName;

    }
}
