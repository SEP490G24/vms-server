package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Tag(name = "Commune Service")
@RequestMapping("/api/v1/commune")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ICommuneController {

    @GetMapping("/{id}")
    @Operation(summary = "Find by id")
    ResponseEntity<?> findById(@PathVariable Integer id);

    @GetMapping
    @Operation(summary = "Get all")
    ResponseEntity<List<?>> findAll();

    @GetMapping("/district/{districtId}")
    @Operation(summary = "Find all by provinceId")
    ResponseEntity<List<?>> findAllByDistrictId(@PathVariable Integer districtId);

    @Data
    class CommuneDto {
        private Integer id;
        private String name;
    }


}
