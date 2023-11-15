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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Organization Service")
@RequestMapping("/api/v1/organization")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IOrganizationController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:organization:find')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organization")
    @PreAuthorize("hasRole('r:organization:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all organization")
    @PreAuthorize("hasRole('r:organization:find')")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new organization")
    @PreAuthorize("hasRole('r:organization:create')")
    ResponseEntity<?> createOrganization(@RequestBody @Valid CreateOrganizationInfo organizationInfo);

    @PatchMapping("/{id}")
    @Operation(summary = "Update organization")
    @PreAuthorize("hasRole('r:organization:update')")
    ResponseEntity<?> updateOrganization(@RequestBody @Valid UpdateOrganizationInfo organizationInfo, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter organization")
    @PreAuthorize("hasRole('r:organization:find')")
    ResponseEntity<?> filter(@RequestBody OrganizationFilter organizationFilter, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @Data
    class CreateOrganizationInfo {
        @NotNull
        String name;
        @NotNull
        String code;
        @NotNull
        String website;
        @NotNull
        String representative;
        @NotNull
        String logo;
        @NotNull
        String contactInfo;
        @NotNull
        String contactPhoneNumber;
    }

    @Data
    class UpdateOrganizationInfo {
        String name;
        String code;
        String website;
        String representative;
        String logo;
        String contactInfo;
        String contactPhoneNumber;
    }

    @Data
    class OrganizationFilter {
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        String createBy;
        String lastUpdatedBy;
        Boolean enable;
        String keyword;
    }
}
