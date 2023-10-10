package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Department Service")
@RequestMapping("/api/v1/department")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface IDepartmentController {

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
    ResponseEntity<?> createDepartment(@RequestBody @Valid createDepartmentInfo departmentInfo);

    @PatchMapping("/{id}")
    @Operation(summary = "Update department")
    ResponseEntity<?> updateDepartment(@RequestBody updateDepartmentInfo updateInfo, @PathVariable UUID id);

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
        private String description;
    }
}
