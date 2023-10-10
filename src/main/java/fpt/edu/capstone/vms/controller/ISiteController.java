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
@Tag(name = "Site Service")
@RequestMapping("/api/v1/site")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ISiteController {

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
    ResponseEntity<?> createSite(@RequestBody @Valid createSiteInfo siteInfo);

    @Data
    class createSiteInfo {
        private String name;
        private String code;
        private UUID organizationId;
        private Boolean mandatoryHealth;
        private Boolean mandatoryTripCode;
        private Boolean nucleicAcidTestReport;
        private String phoneNumber;
        private String province;
        private String district;
        private String ward;
        private String address;
        private String taxCode;
        private String description;
        private Boolean enable;
    }
}
