package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "Card Service")
@RequestMapping("/api/v1/card")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ICardController {

    @PostMapping()
    @Operation(summary = "Check card")
    ResponseEntity<?> checkCard(@RequestBody @Valid CardCheckDTO cardDto);

    @PostMapping("/scan")
    @Operation(summary = "Scan card")
    ResponseEntity<?> scanCard(@RequestBody @Valid CardCheckDTO cardDto);

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class CardCheckDTO {
        private String cardId;
        private String macIp;
    }
}
