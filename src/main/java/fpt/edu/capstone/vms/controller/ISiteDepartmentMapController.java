package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Site Department Map Service")
@RequestMapping("/api/v1/siteDepartmentMap")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ISiteDepartmentMapController {

    @GetMapping("/{departmentId}/{siteId}")
    @Operation(summary = "Find by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> findById(@PathVariable UUID departmentId, @PathVariable UUID siteId);

    @DeleteMapping("/{departmentId}/{siteId}")
    @Operation(summary = "Delete")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> delete(@PathVariable UUID departmentId, @PathVariable UUID siteId);

    @GetMapping
    @Operation(summary = "Get all")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<List<?>> findAll();

    @PostMapping()
    @Operation(summary = "Create new agent")
//    @PreAuthorize("hasRole('r:user:create')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> createTicket(@RequestBody @Valid createSiteDepartmentMapInfo siteDepartmentMapInfo);

    @Data
    class createSiteDepartmentMapInfo {
        private UUID departmentId;
        private UUID siteId;
    }
}
