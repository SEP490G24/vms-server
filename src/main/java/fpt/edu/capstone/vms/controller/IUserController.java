package fpt.edu.capstone.vms.controller;



import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Tag(name = "Account Service")
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
//@PreAuthorize("isAuthenticated()")
public interface IUserController {

    @PostMapping("/info/{username}")
    @Operation(summary = "Filter")
    @PreAuthorize("hasRole('r:user:find')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> info(@PathVariable String username) throws NotFoundException;

    @PostMapping("/filter")
    @Operation(summary = "Filter")
    @PreAuthorize("hasRole('r:user:find')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> filter(@RequestBody UserFilter usernames);

    @PostMapping("")
    @Operation(summary = "Create new agent")
    //@PreAuthorize("hasRole('r:user:create')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> create(@RequestBody @Valid CreateUserInfo userInfo);

    @PutMapping("/{username}")
    @Operation(summary = "Update agent")
    @PreAuthorize("hasRole('r:user:update')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> update(@PathVariable("username") String username, @RequestBody @Valid UpdateUserInfo userInfo) throws NotFoundException;

    @PutMapping("/profile")
    @Operation(summary = "Update my profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> updateProfile(@RequestBody @Valid CreateUserInfo userInfo) throws NotFoundException;

    @PutMapping("/update-state")
    @Operation(summary = "Update my state")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> updateState(@QueryParam("state") Constants.UserState state);

    @GetMapping("/handle-auth-success")
    @Operation(summary = "Handle event login success")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> handleAuthSuccess();

    @GetMapping("/profile")
    @Operation(summary = "View my profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> viewMyProfile();

    @GetMapping("/sync")
    @Operation(summary = "Sync account between keycloak & database")
    @PreAuthorize("hasRole('r:user:sync')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<?> sync();



    @Data
    class CreateUserInfo {
        @NotNull
        String username;
        @NotNull
        String password;
        @NotNull
        String firstName;
        @NotNull
        String lastName;
        @NotNull
        String phoneNumber;
        @NotNull
        String email;
        @NotNull
        boolean isEnable;
    }

    @Data
    class UpdateUserInfo {
        String password;
        @NotNull
        String phoneNumber;
        @NotNull
        String email;
        @NotNull
        boolean isEnable;
    }

    @Data
    class UserFilter {
        int pageNumber = 0;
        List<Constants.UserRole> roles;
        List<String> usernames;
        LocalDateTime createdOnStart;
        LocalDateTime createdOnEnd;
        Constants.UserState state;
    }
}
