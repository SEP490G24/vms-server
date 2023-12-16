package fpt.edu.capstone.vms.controller;


import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Roles Service")
@RequestMapping("/api/v1/role")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IRoleController {

    @GetMapping("")
    @Operation(summary = "Find all roles")
    @PreAuthorize("hasRole('r:role:filter')")
    ResponseEntity<?> getAll(@RequestParam(value = "siteId", required = false) String siteId);

    @GetMapping("/{id}")
    @Operation(summary = "Find role by id")
    @PreAuthorize("hasRole('r:role:detail')")
    ResponseEntity<?> getById(@PathVariable("id") String id);

    @PostMapping("/filter")
    @Operation(summary = "Filter role")
    @PreAuthorize("hasRole('r:role:filter')")
    ResponseEntity<?> filter(@RequestBody RoleFilterPayload filterPayload, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

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
    @PreAuthorize("hasRole('r:role:premission:update')")
    ResponseEntity<?> updatePermission(@PathVariable("id") String id,
                                       @RequestBody UpdateRolePermissionPayload payload);

    @PutMapping("/{id}/permissions")
    @Operation(summary = "Update permissions")
    @PreAuthorize("hasRole('r:role:premission:update')")
    ResponseEntity<?> updatePermissions(@PathVariable("id") String id,
                                       @RequestBody UpdateRolePermissionsPayload payload);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
    @PreAuthorize("hasRole('r:role:delete')")
    ResponseEntity<?> delete(@PathVariable("id") String id);

    @Data
    class RoleBasePayload {
        private String code;
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
    }

    @Data
    class UpdateRolePermissionPayload {
        private IPermissionResource.PermissionDto permissionDto;
        private boolean state;
    }

    @Data
    class UpdateRolePermissionsPayload {
        private List<IPermissionResource.PermissionDto> permissionsDto;
        private boolean state;
    }

    @Data
    class RoleFilterPayload extends RoleBasePayload {

    }

}
