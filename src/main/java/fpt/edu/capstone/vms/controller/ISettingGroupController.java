package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Tag(name = "Setting Group Service")
@RequestMapping("/api/v1/settingGroup")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface ISettingGroupController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:setting-group:detail')")
    ResponseEntity<?> findById(@PathVariable Long id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete")
    @PreAuthorize("hasRole('r:setting-group:delete')")
    ResponseEntity<?> delete(@PathVariable Long id);

    @PutMapping ("/{id}")
    @Operation(summary = "Update setting group")
    @PreAuthorize("hasRole('r:setting-group:update')")
    ResponseEntity<?> updateSettingGroup(@PathVariable Long id, @RequestBody @Valid UpdateSettingGroupInfo settingGroupInfo);

    @GetMapping
    @Operation(summary = "Get all")
    @PreAuthorize("hasRole('r:setting-group:filter')")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new agent")
    @PreAuthorize("hasRole('r:setting-group:create')")
    ResponseEntity<?> createSettingGroup(@RequestBody @Valid CreateSettingGroupInfo settingGroupInfo);

    @Data
    class CreateSettingGroupInfo {
        @NotNull
        private String name;
    }

    @Data
    class UpdateSettingGroupInfo {
        String name;
    }
}
