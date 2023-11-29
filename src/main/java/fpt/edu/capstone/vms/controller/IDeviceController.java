package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@Tag(name = "Device Service")
@RequestMapping("/api/v1/device")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IDeviceController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    @PreAuthorize("hasRole('r:device:detail')")
    ResponseEntity<?> findById(@PathVariable Integer id);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete")
    @PreAuthorize("hasRole('r:device:delete')")
    ResponseEntity<?> delete(@PathVariable Integer id);

    @PostMapping()
    @Operation(summary = "Create new device")
    @PreAuthorize("hasRole('r:device:create')")
    ResponseEntity<?> create(@RequestBody @Valid DeviceDto deviceDto);

    @PutMapping("/{id}")
    @Operation(summary = "Update device")
    @PreAuthorize("hasRole('r:device:update')")
    ResponseEntity<?> update(@RequestBody UpdateDeviceDto deviceDto, @PathVariable Integer id);

    @PostMapping("/filter")
    @Operation(summary = "Filter")
    @PreAuthorize("hasRole('r:device:find')")
    ResponseEntity<?> filter(@RequestBody @Valid DeviceFilterDTO deviceFilterDTO, @QueryParam("isPageable") boolean isPageable, Pageable pageable);

    @PostMapping("/not-use")
    @Operation(summary = "Find all device with not use in site")
    @PreAuthorize("hasRole('r:device:find')")
    ResponseEntity<?> findAllWithNotUseInSite(@RequestParam(value = "siteId", required = false) String siteId);


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class DeviceDto {
        @NotNull
        private String code;
        @NotNull
        private String name;
        @NotNull
        private String macIp;
        @NotNull
        private Constants.DeviceType deviceType;
        private String description;
        @NotNull
        private Boolean enable;
        @NotNull
        private UUID siteId;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class UpdateDeviceDto {
        private String code;
        private String name;
        private String macIp;
        private Constants.DeviceType deviceType;
        private String description;
        private Boolean enable;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class DeviceFilterDTO {
        List<String> names;
        Constants.DeviceType deviceType;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Boolean enable;
        String keyword;
        String createBy;
        List<String> siteId;

    }

    @Data
    class DeviceFilterResponse {
        private Integer id;
        private String name;
        private String code;
        private UUID siteId;
        private String siteName;
        private Constants.DeviceType deviceType;
        private String description;
        private String macIp;
        private String createdBy;
        private String lastUpdatedBy;
        private LocalDateTime lastUpdatedOn;
        private LocalDateTime createdOn;
        private Boolean enable;
    }
}
