package fpt.edu.capstone.vms.persistence.service.impl;

import com.azure.storage.common.StorageSharedKeyCredential;
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
import com.azure.storage.blob.*;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl extends GenericServiceImpl<File, UUID> implements IFileService {

    @Value("${azure.account.name}")
    private String accountName;

    @Value("${azure.account.key}")
    private String accountKey;

    @Value("${azure.container.name}")
    private String containerName;

    private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.init(fileRepository);
    }

    @Override
    public Boolean deleteImage(String oldImage, String newImage) {
        var oldFile = fileRepository.findByName(oldImage);
        var newFile = fileRepository.findByName(newImage);

        if (ObjectUtils.isEmpty(newFile))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found image in file");
        BlobClient blobClient = getBlobClient(oldImage);
        blobClient.deleteIfExists();
        if (!ObjectUtils.isEmpty(oldFile)) {
            fileRepository.delete(oldFile);
            return true;
        }
        return true;
    }

    @Override
    public File uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "File empty");
        }
        log.info("Upload image");
        // Get original file name
        String originalFilename = file.getOriginalFilename();
        // Get name extension
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        // generate url for image
        String relativeFileName = UUID.randomUUID().toString() + "." + extension;
        try {
            long fileSizeInMB = file.getSize() / (1024 * 1024);

            ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
            if (fileSizeInMB > 1) {
                Thumbnails.of(file.getInputStream())
                    .size(1920, 1080)
                    .outputQuality(0.9)
                    .toOutputStream(thumbnailOutputStream);
            } else {
                Thumbnails.of(file.getInputStream())
                    .size(1920, 1080)
                    .toOutputStream(thumbnailOutputStream);
            }

            String blobEndpoint = String.format("https://%s.blob.core.windows.net", accountName);
            String blobUri = String.format("%s/%s/%s", blobEndpoint, containerName, relativeFileName);
            BlobClient blobClient = getBlobClient(relativeFileName);
            try (InputStream thumbnailImageStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray())) {
                blobClient.upload(thumbnailImageStream, thumbnailOutputStream.size());
            }

            File image = new File();
            image.setDescription("Set avatar");
            image.setFileExtension(extension);
            image.setName(relativeFileName);
            image.setStatus(true);
            image.setUrl(blobUri);
            image.setType(Constants.FileType.IMAGE);
            fileRepository.save(image);
            return image;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public BlobClient getBlobClient(String fileName) {
        StorageSharedKeyCredential storageCredentials =
            new StorageSharedKeyCredential(accountName, accountKey);
        String blobEndpoint = String.format("https://%s.blob.core.windows.net", accountName);
        // Create the BlobServiceClient
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().endpoint(blobEndpoint).credential(storageCredentials).buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        return containerClient.getBlobClient(fileName);
    }
}
