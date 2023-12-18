package fpt.edu.capstone.vms.persistence.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.common.StorageSharedKeyCredential;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.File;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.service.IFileService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
            throw new CustomException(ErrorApp.FILE_NOT_FOUND);
        if (oldImage != null) {
            BlobClient blobClient = getBlobClient(oldImage);
            blobClient.deleteIfExists();
        }
        if (!ObjectUtils.isEmpty(oldFile)) {
            fileRepository.delete(oldFile);
            return true;
        }
        return true;
    }

    @Override
    public File uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorApp.FILE_EMPTY);
        }
        log.info("Upload image");
        // Get original file name
        String originalFilename = file.getOriginalFilename();
        // Get name extension
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        if (!isValidImageExtension(extension)) {
            throw new CustomException(ErrorApp.FILE_INVALID_IMAGE_EXTENSION);
        }

        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

        // generate url for image
        String relativeFileName = timestamp + "_" + UUID.randomUUID().toString() + "." + extension;
        try {
            long fileSizeInMB = file.getSize() / (1024 * 1024);

            if (fileSizeInMB > 3) {
                throw new CustomException(ErrorApp.FILE_OVER_SIZE);
            }

            String blobEndpoint = String.format("https://%s.blob.core.windows.net", accountName);
            String blobUri = String.format("%s/%s/%s", blobEndpoint, containerName, relativeFileName);
            BlobClient blobClient = getBlobClient(relativeFileName);
            try (InputStream imageStream = file.getInputStream()) {
                blobClient.upload(imageStream, file.getSize());
            }
            File image = new File();
            image.setDescription("Set avatar");
            image.setFileExtension(extension);
            image.setName(relativeFileName);
            image.setCode("user.avatar");
            image.setStatus(true);
            image.setUrl(blobUri);
            image.setType(Constants.FileType.IMAGE_AVATAR);
            fileRepository.save(image);
            return image;
        } catch (IOException e) {
            BlobClient blobClient = getBlobClient(relativeFileName);
            blobClient.deleteIfExists();
            e.printStackTrace();
            throw new CustomException(ErrorApp.FILE_UPLOAD_FAILED);
        }
    }

    public List<String> getBlobFileNameList() {
        List<String> fileNames = new ArrayList<>();
        StorageSharedKeyCredential storageCredentials =
            new StorageSharedKeyCredential(accountName, accountKey);
        String blobEndpoint = String.format("https://%s.blob.core.windows.net", accountName);
        // Create the BlobServiceClient
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().endpoint(blobEndpoint).credential(storageCredentials).buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Lặp qua các tệp trong container và lấy danh sách tên tệp
        for (BlobItem blobItem : containerClient.listBlobs()) {
            fileNames.add(blobItem.getName());
        }

        return fileNames;
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

    public boolean isValidImageExtension(String extension) {
        List<String> validExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");
        return validExtensions.contains(extension.toLowerCase());
    }
}
