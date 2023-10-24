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
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('r:department:find')")
    ResponseEntity<?> findById(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a department")
    @PreAuthorize("hasRole('r:department:delete')")
    ResponseEntity<?> delete(@PathVariable UUID id);

    @GetMapping
    @Operation(summary = "Get all department")
    @PreAuthorize("hasRole('r:department:find')")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new department")
    @PreAuthorize("hasRole('r:department:create')")
    ResponseEntity<?> createDepartment(@RequestBody @Valid createDepartmentInfo departmentInfo);

    @PatchMapping("/{id}")
    @Operation(summary = "Update department")
    @PreAuthorize("hasRole('r:department:update')")
    ResponseEntity<?> updateDepartment(@RequestBody updateDepartmentInfo updateInfo, @PathVariable UUID id);

    @PostMapping("/filter")
    @Operation(summary = "Filter department")
    @PreAuthorize("hasRole('r:department:find')")
    ResponseEntity<?> filter(@RequestBody @Valid DepartmentFilter siteFilter, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @Data
    class createDepartmentInfo {
        @NotNull
        private String name;
        @NotNull
        private String code;
        @NotNull
        private String siteId;
        private String description;
    }

    @Data
    class updateDepartmentInfo {
        private String name;
        private String code;
        private String enable;
        private String description;
    }

    @Data
    class DepartmentFilter {
        List<String> names;
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
        private String name;
        private String code;
        private String enable;
        private String siteId;
        private String siteName;
        private String description;
        private String createdBy;
        private String lastUpdatedBy;
        private LocalDateTime lastUpdatedOn;
        private LocalDateTime createdOn;

    }
}
