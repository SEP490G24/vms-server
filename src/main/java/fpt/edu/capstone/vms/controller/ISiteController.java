package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Site Service")
@RequestMapping("/api/v1/site")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface ISiteController {

    @GetMapping("/view")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:site:detail')")
    ResponseEntity<?> findById(@RequestParam(value = "siteId", required = false) String siteId);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete site")
    @PreAuthorize("hasRole('r:site:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all")
    @PreAuthorize("hasRole('r:site:filter')")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new site")
    @PreAuthorize("hasRole('r:site:create')")
    ResponseEntity<?> createSite(@RequestBody @Valid CreateSiteInfo siteInfo);

    @PutMapping("/{id}")
    @Operation(summary = "Update site")
    @PreAuthorize("hasRole('r:site:update')")
    ResponseEntity<?> updateSite(@RequestBody UpdateSiteInfo updateSiteInfo, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter site")
    @PreAuthorize("hasRole('r:site:filter')")
    ResponseEntity<?> filter(@RequestBody @Valid SiteFilter siteFilter, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @GetMapping("/organization")
    @Operation(summary = "Get all site by organizationId")
    @PreAuthorize("hasRole('r:site:filter')")
    ResponseEntity<List<?>> findAllByOrganization();

    @Data
    class CreateSiteInfo {
        @NotNull
        private String name;
        @NotNull
        private String code;
        @NotNull
        private String phoneNumber;
        @NotNull
        private Integer provinceId;
        @NotNull
        private Integer districtId;
        @NotNull
        private Integer communeId;
        @NotNull
        private String address;
        @NotNull
        private String taxCode;
        private String description;
        @NotNull
        private boolean enable;
    }

    @Data
    class UpdateSiteInfo {
        private String name;
        private String phoneNumber;
        private Integer provinceId;
        private Integer districtId;
        private Integer communeId;
        private String address;
        private String taxCode;
        private String description;
        private Boolean enable;
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
        Integer provinceId;
        Integer districtId;
        Integer communeId;
    }

    @Data
    class SiteFilterDTO {
        private UUID id;
        private String name;
        private String code;
        private Boolean enable;
        private String organizationId;
        private String organizationName;
        private String phoneNumber;
        private Integer provinceId;
        private Integer communeId;
        private Integer districtId;
        private String provinceName;
        private String districtName;
        private String communeName;
        private String address;
        private String taxCode;
        private String description;
        private String createdBy;
        private String lastUpdatedBy;
        private LocalDateTime lastUpdatedOn;
        private LocalDateTime createdOn;
    }
}
