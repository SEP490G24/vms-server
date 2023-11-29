package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.IFileController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.service.IFileService;
import fpt.edu.capstone.vms.util.ResponseUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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
            return ResponseUtils.getResponseEntityStatus(fileService.uploadImage(file));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
