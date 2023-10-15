package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.persistence.entity.File;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


public interface IFileService extends IGenericService<File, UUID> {

    File uploadImage(MultipartFile file);
    Boolean deleteImage(String oldImage, String newImage);
}
