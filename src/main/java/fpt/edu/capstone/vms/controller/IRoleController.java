package fpt.edu.capstone.vms.controller;


import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Tag(name = "Roles Service")
@RequestMapping("/api/v1/role")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IRoleController {

    @GetMapping("")
    @Operation(summary = "Find all roles")
    @PreAuthorize("hasRole('r:role:find')")
    ResponseEntity<?> getAll();

    @GetMapping("/{id}")
    @Operation(summary = "Find role by id")
    @PreAuthorize("hasRole('r:role:find')")
    ResponseEntity<?> getById(@PathVariable("id") String id);

    @PostMapping("/filter")
    @Operation(summary = "Filter role")
        @PreAuthorize("hasRole('r:role:find')")
    ResponseEntity<?> filter(@RequestBody RoleFilterPayload filterPayload);

    @PostMapping("")
    @Operation(summary = "Create role")
        @PreAuthorize("hasRole('r:role:create')")
    ResponseEntity<?> create(@RequestBody CreateRolePayload payload);

    @PutMapping("/{id}")
    @Operation(summary = "Update role")
        @PreAuthorize("hasRole('r:role:update')")
    ResponseEntity<?> update(@PathVariable("id") String id,
                             @RequestBody UpdateRolePayload payload) throws NotFoundException;

    @PutMapping("/{id}/permission")
    @Operation(summary = "Update permission")
    @PreAuthorize("hasRole('r:role:update')")
    ResponseEntity<?> updatePermission(@PathVariable("id") String id,
                                       @RequestBody UpdateRolePermissionPayload payload);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
        @PreAuthorize("hasRole('r:role:delete')")
    ResponseEntity<?> delete(@PathVariable("id") String id);

    @PostMapping("/site")
    @Operation(summary = "Get role by sites")
        @PreAuthorize("hasRole('r:role:find')")
    ResponseEntity<?> getBySites(@RequestBody List<String> sites);

    @Data
    class RoleBasePayload {
        private String siteId;
        private String name;
        private Map<String, List<String>> attributes;
        private String description;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    class CreateRolePayload extends RoleBasePayload {

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    class UpdateRolePayload extends RoleBasePayload {
        private Set<IPermissionResource.PermissionDto> permissionDtos;
    }

    @Data
    class UpdateRolePermissionPayload {
        private IPermissionResource.PermissionDto permissionDto;
        private boolean state;
    }

    @Data
    class RoleFilterPayload extends RoleBasePayload {

    }

}
