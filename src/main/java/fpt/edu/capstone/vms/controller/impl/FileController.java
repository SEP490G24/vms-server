package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IFileController;
import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileController implements IFileController {

    @Value("${images.folder}")
    private String imagesFolder;
    private final ModelMapper mapper;


    public ResponseEntity<?> uploadImage(@RequestBody MultipartFile file) {
        log.info("Upload image");
        // Get original file name
        String originalFilename = file.getOriginalFilename();
        // Get name extension
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // generate url for image
        String relativeFileName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(imagesFolder, relativeFileName);
        try {
            Files.write(filePath, file.getBytes());
            return ResponseEntity.status(HttpStatus.OK).body(relativeFileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> downloadImage(@RequestParam(required = true) String url) {
        // Logic to retrieve the image file based on relative URL
        // For example, if images are stored in a local directory:
        String filePath = imagesFolder + "/" + url;
        String fileFormat = url.substring(url.lastIndexOf(".") + 1);

        try {
        // Set the Content-Type and Content-Disposition headers based on file format
        HttpHeaders headers = new HttpHeaders();
        if ("jpg".equalsIgnoreCase(fileFormat) || "jpeg".equalsIgnoreCase(fileFormat)) {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        } else if ("png".equalsIgnoreCase(fileFormat)) {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        }
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + url);
        Resource resource = new FileSystemResource(filePath);
        // Return the image file as a response with appropriate headers

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
