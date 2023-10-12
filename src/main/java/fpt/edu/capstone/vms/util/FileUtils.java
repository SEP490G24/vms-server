package fpt.edu.capstone.vms.util;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class FileUtils {

    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    private static final Integer maxFileSizeUpload = 104857600;

    public static boolean isValidFileUpload(MultipartFile file, String... extentions) {
        try {
            if (file == null || file.isEmpty()) {
                return false;
            }

            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);
            if (StringUtils.isBlank(fileExtension) || !Arrays.asList(extentions).contains(fileExtension)) {
                return false;
            }

            if (file.getSize() > maxFileSizeUpload) {
                return false;
            }

            if (StringUtils.isBlank(fileName) || fileName.contains("\\") || fileName.contains("/") || fileName.indexOf(".") == 0) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            System.out.print(ex);
            return false;
        }
    }

    public static String getFileExtension(String fileName) {
        return FilenameUtils.getExtension(fileName).toUpperCase();
    }

}
