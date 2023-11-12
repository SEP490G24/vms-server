package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@Tag(name = "Setting Site Value Service")
@RequestMapping("/api/v1/settingSiteMap")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface ISettingSiteMapController {

    @GetMapping("/{siteId}/{settingId}")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:setting-site:find')")
    ResponseEntity<?> findById(@PathVariable String siteId, @PathVariable Long settingId);

    @DeleteMapping("/{siteId}/{settingId}")
    @Operation(summary = "Delete")
    @PreAuthorize("hasRole('r:setting-site:delete')")
    ResponseEntity<?> delete(@PathVariable String siteId, @PathVariable Long settingId);

    @PostMapping()
    @Operation(summary = "Create or Update setting site")
    @PreAuthorize("hasRole('r:setting-site:create')")
    ResponseEntity<?> createOrUpdateSettingSiteMap(@RequestBody @Valid SettingSiteInfo updateSettingSiteInfo);

    @GetMapping
    @Operation(summary = "Get all")
    @PreAuthorize("hasRole('r:setting-site:find')")
    ResponseEntity<List<?>> findAll();

    @GetMapping("/{siteId}")
    @Operation(summary = "Find All by site id")
    @PreAuthorize("hasRole('r:setting-site:find')")
    ResponseEntity<List<?>> findAllBySiteId(@PathVariable String siteId);

    @GetMapping("/site/{siteId}/group/{settingGroupId}")
    @Operation(summary = "Find All by site id and group id")
    @PreAuthorize("hasRole('r:setting-site:find')")
    ResponseEntity<?> findAllBySiteIdAndGroupId(@PathVariable String siteId, @PathVariable Integer settingGroupId);

    @GetMapping("/set-default/{siteId}")
    @Operation(summary = "Set default setting for site")
    @PreAuthorize("hasRole('r:setting-site:delete')")
    ResponseEntity<?> setDefault(@PathVariable String siteId);

    @Data
    @Builder
    class SettingSiteInfo {
        @NotNull
        private String siteId;
        @NotNull
        private Integer settingId;
        private String description;
        @NotNull
        private String value;
    }

    @Data
    class SettingSite {
        private Long settingId;
        private UUID siteId;
        private String code;
        private Boolean status;
        private String propertyValue;
        private String defaultPropertyValue;
        private String description;
        private Long settingGroupId;
        private String settingGroupName;
        private String settingName;
        private String type;
        private String valueList;
        private String createdBy;
        private String lastUpdatedBy;
        private Date lastUpdatedOn;
        private Date createdOn;
    }

    @Data
    class SettingSiteDTO {
        private Long settingGroupId;
        private String siteId;
        private Map<String, String> settings;
    }

}
