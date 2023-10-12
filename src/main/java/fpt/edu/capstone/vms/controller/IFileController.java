package fpt.edu.capstone.vms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Roles Service")
@RequestMapping("/api/v1/file")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@PreAuthorize("isAuthenticated()")
public interface IFileController {

    @PostMapping("/uploadImage")
    @Operation(summary = "Upload Image")
    ResponseEntity<?> uploadImage(@RequestBody MultipartFile file);

    @GetMapping("/downloadAvatar")
    @Operation(summary = "Download Image")
    ResponseEntity<?> downloadImage(@RequestParam(required = true) String url);
}
