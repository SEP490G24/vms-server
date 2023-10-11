package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Organization Service")
@RequestMapping("/api/v1/organization")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface IOrganizationController {

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
    @Operation(summary = "Create new organization")
//    @PreAuthorize("hasRole('r:user:create')")
    ResponseEntity<?> createOrganization(@RequestBody @Valid CreateOrganizationInfo organizationInfo);

    @PatchMapping("/{id}")
    @Operation(summary = "Update organization")
//    @PreAuthorize("hasRole('r:user:create')")
    ResponseEntity<?> updateOrganization(@RequestBody @Valid UpdateOrganizationInfo organizationInfo, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
//    @PreAuthorize("hasRole('r:user:find')")
    ResponseEntity<?> filter(@RequestBody OrganizationFilter organizationFilter);

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
    }

    @Data
    class UpdateOrganizationInfo {
        String name;
        String code;
        String website;
        String representative;
        Boolean enable;
    }

    @Data
    class OrganizationFilter {
        int pageNumber = 0;
        List<String> names;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        String createBy;
        String lastUpdatedBy;
        Boolean enable;
        String keyword;
    }
}
