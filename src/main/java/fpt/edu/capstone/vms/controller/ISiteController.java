package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Column;
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
@Tag(name = "Site Service")
@RequestMapping("/api/v1/site")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ISiteController {

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
    ResponseEntity<?> createSite(@RequestBody @Valid CreateSiteInfo siteInfo);

    @PutMapping("/{id}")
    @Operation(summary = "Update site")
    ResponseEntity<?> updateSite(@RequestBody UpdateSiteInfo updateSiteInfo, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
//    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> filter(@RequestBody @Valid SiteFilter siteFilter, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get all site by organizationId")
    ResponseEntity<List<?>> findAllByOrganizationId(@PathVariable String organizationId);
    @Data
    class CreateSiteInfo {
        @NotNull
        private String name;
        @NotNull
        private String phoneNumber;
        @NotNull
        private String province;
        @NotNull
        private String district;
        @NotNull
        private String ward;
        @NotNull
        private String address;
        @NotNull
        private String taxCode;
        private String description;
    }

    @Data
    class UpdateSiteInfo {
        private String name;
        private String code;
        private String phoneNumber;
        private String province;
        private String district;
        private String ward;
        private String address;
        private String taxCode;
        private String description;
        private boolean enable;
    }

    @Data
    class SiteFilter {
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        String createBy;
        String lastUpdatedBy;
        Boolean enable;
        String keyword;
        UUID organizationId;
    }

    @Data
    class SiteFilterDTO {
        private String name;
        private String code;
        private String enable;
        private String organizationId;
        private String organizationName;
        private String phoneNumber;
        private String province;
        private String district;
        private String ward;
        private String address;
        private String taxCode;
        private String description;
        private String createdBy;
        private String lastUpdatedBy;
        private LocalDateTime lastUpdatedOn;
        private LocalDateTime createdOn;
    }
}
