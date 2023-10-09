package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Site Department Map Service")
@RequestMapping("/api/v1/siteDepartmentMap")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ISiteDepartmentMapController {

    @GetMapping("/{departmentId}/{siteId}")
    @Operation(summary = "Find by id")
    ResponseEntity<?> findById(@PathVariable UUID departmentId, @PathVariable UUID siteId);

    @DeleteMapping("/{departmentId}/{siteId}")
    @Operation(summary = "Delete")
    ResponseEntity<?> delete(@PathVariable UUID departmentId, @PathVariable UUID siteId);

    @GetMapping
    @Operation(summary = "Get all")
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new agent")
//    @PreAuthorize("hasRole('r:user:create')")
    ResponseEntity<?> createTicket(@RequestBody @Valid createSiteDepartmentMapInfo siteDepartmentMapInfo);

    @Data
    class createSiteDepartmentMapInfo {
        private UUID departmentId;
        private UUID siteId;
    }
}
