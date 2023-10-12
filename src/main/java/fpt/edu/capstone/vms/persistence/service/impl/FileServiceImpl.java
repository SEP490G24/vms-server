package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.File;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.service.IFileService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl extends GenericServiceImpl<File, UUID> implements IFileService {

    @Value("${images.folder}")
    private String imagesFolder;

    private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.init(fileRepository);
    }

    @Override
    public File downloadImage(String url) {
        var file = fileRepository.findByName(url);
        if (ObjectUtils.isEmpty(file)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Can't found image");
        return file;
    }

    @Override
    public File uploadImage(MultipartFile file) {
        log.info("Upload image");
        // Get original file name
        String originalFilename = file.getOriginalFilename();
        // Get name extension
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")  + 1);

        // generate url for image
        String relativeFileName = UUID.randomUUID().toString() + "." + extension;
        Path filePath = Paths.get(imagesFolder, relativeFileName);
        try {
            java.io.File originalImage = java.io.File.createTempFile("original", file.getOriginalFilename());
            file.transferTo(originalImage);


            long fileSizeInBytes = originalImage.length();
            long fileSizeInMB = fileSizeInBytes / (1024 * 1024);

            if (fileSizeInMB > 1) {
                Thumbnails.of(originalImage)
                    .size(1920, 1080)
                    .outputQuality(0.8)
                    .toFile(originalImage);
            }

            java.io.File resizedImage = new java.io.File(filePath.toUri());
            try (FileOutputStream fos = new FileOutputStream(resizedImage)) {
                fos.write(org.apache.commons.io.FileUtils.readFileToByteArray(originalImage));
            }

            originalImage.delete();

            File image = new File();
            image.setDescription("Set avatar");
            image.setFileExtension(extension);
            image.setName(relativeFileName);
            image.setUrl(filePath.toString());
            image.setType(Constants.FileType.IMAGE);
            fileRepository.save(image);
            return image;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
