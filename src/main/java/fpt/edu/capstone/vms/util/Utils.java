package fpt.edu.capstone.vms.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class Utils {

    public static File getTemplateFile(String path) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = ResourceUtils.getURL(path).openStream();
            File tempFile = File.createTempFile("Temp", "import");
            tempFile.deleteOnExit();
            out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);
            return tempFile;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    log.error( e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    log.error(e.getMessage());
                }
            }
        }
        return null;
    }

    public static boolean isCCCDValid(String cccd) {
        String regex = "\\d{12}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(cccd);
        return matcher.matches();
    }
}
