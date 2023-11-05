package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IFileController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.service.IFileService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController implements IFileController {

    private final IFileService fileService;
    private final ModelMapper mapper;

    public FileController(IFileService fileService, ModelMapper mapper) {
        this.fileService = fileService;
        this.mapper = mapper;
    }


    public ResponseEntity<?> uploadImage(@RequestBody MultipartFile file) {

        try {
            return ResponseEntity.ok(fileService.uploadImage(file));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

}
