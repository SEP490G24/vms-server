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
@Tag(name = "Department Service")
@RequestMapping("/api/v1/department")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IDepartmentController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id department")
    @PreAuthorize("hasRole('r:department:detail')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a department")
    @PreAuthorize("hasRole('r:department:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all department")
    @PreAuthorize("hasRole('r:department:filter')")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new department")
    @PreAuthorize("hasRole('r:department:create')")
    ResponseEntity<?> createDepartment(@RequestBody @Valid IDepartmentController.CreateDepartmentInfo departmentInfo);

    @PatchMapping("/{id}")
    @Operation(summary = "Update department")
    @PreAuthorize("hasRole('r:department:update')")
    ResponseEntity<?> updateDepartment(@RequestBody UpdateDepartmentInfo updateInfo, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter department")
    @PreAuthorize("hasRole('r:department:filter')")
    ResponseEntity<?> filter(@RequestBody @Valid DepartmentFilter siteFilter, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @PostMapping("/site/{siteId}")
    @Operation(summary = "Find all department by site")
    @PreAuthorize("hasRole('r:department:filter')")
    ResponseEntity<?> findAllBySite(@PathVariable String siteId);


    @Data
    class CreateDepartmentInfo {
        @NotNull
        private String name;
        @NotNull
        private String code;
        @NotNull
        private String siteId;
        private String description;
        @NotNull
        private Boolean enable = true;
    }

    @Data
    class UpdateDepartmentInfo {
        private String name;
        private Boolean enable;
        private String description;
    }

    @Data
    class DepartmentFilter {
        List<String> names;
        List<String> SiteIds;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        String createBy;
        String lastUpdatedBy;
        Boolean enable;
        String keyword;
        UUID siteId;
    }

    @Data
    class DepartmentFilterDTO {
        private UUID id;
        private String name;
        private String code;
        private Boolean enable;
        private String siteId;
        private String siteName;
        private String description;
        private String createdBy;
        private String lastUpdatedBy;
        private LocalDateTime lastUpdatedOn;
        private LocalDateTime createdOn;

    }
}
