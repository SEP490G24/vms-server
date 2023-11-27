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
@Tag(name = "Permission Service")
@RequestMapping("/api/v1/module")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IPermissionController {

    @GetMapping("")
    @Operation(summary = "Find all module")
    ResponseEntity<?> getAllModule(@QueryParam("fetchPermission") boolean fetchPermission);

    @GetMapping("/{mId}")
    @Operation(summary = "Find all permissions in module")
    ResponseEntity<?> getAllByModuleId(@PathVariable("mId") String mId);

    @GetMapping("/{mId}/permission/{pId}")
    @Operation(summary = "Find permission by id and module id")
    ResponseEntity<?> getByIdAndModuleId(@PathVariable("mId") String mId, @PathVariable("pId") String pId);

    @PostMapping("/permission/filter")
    @Operation(summary = "Filter permission")
    @PreAuthorize("hasRole('r:permission:find')")
    ResponseEntity<?> filter(@RequestBody PermissionFilterPayload filterPayload, Pageable pageable);

    @PostMapping("/{mId}/permission")
    @Operation(summary = "Create permission")
    ResponseEntity<?> create(@PathVariable("mId") String mId, @RequestBody CreatePermissionPayload payload);

    @PutMapping("/{mId}/permission/{pId}")
    @Operation(summary = "Update permission")
    ResponseEntity<?> update(@PathVariable("mId") String mId,
                             @PathVariable("pId") String pId,
                             @RequestBody UpdatePermissionPayload payload) throws NotFoundException;

    @PutMapping("/{mId}/permission/attribute")
    @Operation(summary = "Update attribute permission")
    ResponseEntity<?> updateAttribute(@PathVariable("mId") String mId,
                                      @RequestBody UpdateAttributePermissionPayload payload);

    @DeleteMapping("/{mId}/permission/{pId}")
    @Operation(summary = "Delete permission")
    ResponseEntity<?> delete(@PathVariable("mId") String mId,
                             @PathVariable("pId") String pId);

    @Data
    class PermissionBasePayload {
        private String name;
        private Map<String, List<String>> attributes;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    class CreatePermissionPayload extends PermissionBasePayload {

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    class UpdatePermissionPayload extends PermissionBasePayload {

    }

    @Data
    class UpdateAttributePermissionPayload {
        private Map<String, List<String>> attributes;
        private List<IPermissionResource.PermissionDto> permissionDtos;
    }

    @Data
    class PermissionFilterPayload {
        String name;
    }


}
