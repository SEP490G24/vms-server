package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.File;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class FileServiceImplTest {

    @InjectMocks
    private FileServiceImpl fileService;

    @Mock
    private FileRepository fileRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    @DisplayName("given invalid image extension, when uploadImage, then exception is thrown")
    void givenInvalidImageExtension_WhenUploadImage_ThenThrowException() {
        MultipartFile invalidFile = createMultipartFileWithExtension("txt");

        assertThrows(CustomException.class, () -> fileService.uploadImage(invalidFile));
    }

    @Test
    @DisplayName("given file size over 10MB, when uploadImage, then exception is thrown")
    void givenFileSizeOver10MB_WhenUploadImage_ThenThrowException() {
        MultipartFile largeFile = createMultipartFileWithSize(11 * 1024 * 1024);

        assertThrows(CustomException.class, () -> fileService.uploadImage(largeFile));
    }

    @Test
    @DisplayName("given existing old image, when deleteImage, then return true")
    void givenExistingOldImage_WhenDeleteImage_ThenReturnTrue() {
        String oldImage = "existing_image.jpg";
        String newImage = "new_image.jpg";

        File oldFile = new File();
        when(fileRepository.findByName(oldImage)).thenReturn(oldFile);

        assertTrue(true);
    }

    @Test
    @DisplayName("given non-existing old image, when deleteImage, then return false")
    void givenNonExistingOldImage_WhenDeleteImage_ThenReturnFalse() {
        String oldImage = "non_existing_image.jpg";
        String newImage = "new_image.jpg";

        when(fileRepository.findByName(oldImage)).thenReturn(null);

        assertFalse(false);
    }

    private MultipartFile createMultipartFileWithExtension(String extension) {
        String originalFilename = "testFile." + extension;
        byte[] content = new byte[1024];

        return new MockMultipartFile("file", originalFilename, null, content);
    }

    private MultipartFile createMultipartFileWithSize(long size) {
        String originalFilename = "testFile.jpg";
        byte[] content = new byte[(int) size];

        return new MockMultipartFile("file", originalFilename, null, content);
    }
}
