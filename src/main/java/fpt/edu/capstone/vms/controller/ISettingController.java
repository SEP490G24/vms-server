package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.constants.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Tag(name = "Setting Site Service")
@RequestMapping("/api/v1/setting")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface ISettingController {

    @PutMapping("/{id}")
    @Operation(summary = "Update setting site")
    ResponseEntity<?> updateSettingGroup(@PathVariable Long id, @RequestBody @Valid UpdateSettingInfo settingInfo);

    @GetMapping
    @Operation(summary = "Get all")
    @PreAuthorize("hasRole('r:setting-site:filter')")
    ResponseEntity<List<?>> findAll(@RequestParam(value = "groupId", required = false) Integer groupId, @RequestParam(value = "siteId", required = false) String siteId);

    @PostMapping()
    ResponseEntity<?> createSetting(@RequestBody @Valid CreateSettingInfo settingInfo);

    @Data
    class CreateSettingInfo {
        @NotNull
        private String code;
        @NotNull
        private String name;
        private String description;
        @NotNull
        private Constants.SettingType type;
        private String defaultValue;
        @NotNull
        private Boolean enable;
        @NotNull
        private Long groupId;
    }

    @Data
    class UpdateSettingInfo {
        private String code;
        private String name;
        private String description;
        private Constants.SettingType type;
        private String defaultValue;
        private Boolean enable;
        private Long groupId;
    }

    @Data
    class GroupIdDto {
        private Long groupId;
    }

}
