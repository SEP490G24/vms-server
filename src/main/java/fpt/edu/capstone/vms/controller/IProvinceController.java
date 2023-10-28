package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Tag(name = "Location Service")
@RequestMapping("/api/v1/location/province")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IProvinceController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id province")
    ResponseEntity<?> findById(@PathVariable Integer id);

    @GetMapping
    @Operation(summary = "Get all province")
    ResponseEntity<List<?>> findAll();

    @Data
    class ProvinceDto {
        private Integer id;
        private String name;
    }


}
