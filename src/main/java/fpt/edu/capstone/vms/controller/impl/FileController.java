package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IFileController;
import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.service.IFileService;
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
import org.springframework.web.client.HttpClientErrorException;
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
public class FileController implements IFileController {

    @Value("${images.folder}")
    private String imagesFolder;
    private final IFileService fileService;
    private final ModelMapper mapper;


    public ResponseEntity<?> uploadImage(@RequestBody MultipartFile file) {

        try {
            return ResponseEntity.ok(fileService.uploadImage(file));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

}
