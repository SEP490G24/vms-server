package fpt.edu.capstone.vms.component;

import com.azure.storage.blob.BlobClient;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.File;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.impl.FileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
@Slf4j
public class ImageCleanJob {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FileServiceImpl fileService;

    public ImageCleanJob(UserRepository userRepository, FileRepository fileRepository, FileServiceImpl fileService) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }

    @Scheduled(cron = "${schedule.cleaningImage.time}")
    public void scheduleCleanAvatarTrash() {
        log.info("Start clean image!!!");
        List<File> files = fileRepository.findAllByType(Constants.FileType.IMAGE_AVATAR);
        List<User> users = userRepository.findAllByAvatarIsNotNull();
        if (users.isEmpty()) {
            log.info("Cleaning {} images in database and blob", files.size());
            files.forEach(o -> {
                deleteImage(o);
            });
        }
        files.forEach(o -> {
            var check = users.stream().anyMatch(m -> m.getAvatar().equals(o.getName()));
            if (!check) {
                deleteImage(o);
                log.info("Cleaning {} images in database and blob", o.getName());
            }
        });

        log.info("Done clean image!!!");
    }

    @Scheduled(cron = "${schedule.cleaningImage.time}")
    public void scheduleCleanFileTrash() {
        log.info("Start clean image!!!");
        List<String> fileNames = fileService.getBlobFileNameList();
        for (String name: fileNames) {
            var check = fileRepository.existsByName(name);
            if (!check) {
                BlobClient blobClient = fileService.getBlobClient(name);
                blobClient.deleteIfExists();
            }
        }
        log.info("Done clean image!!!");
    }

    public void deleteImage(File file) {
        BlobClient blobClient = fileService.getBlobClient(file.getName());
        blobClient.deleteIfExists();
        if (!ObjectUtils.isEmpty(file)) {
            fileRepository.delete(file);
        }
    }
}
