package fpt.edu.capstone.vms.util;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;

import static fpt.edu.capstone.vms.constants.Constants.SettingCode.MAIL_HOST;
import static fpt.edu.capstone.vms.constants.Constants.SettingCode.MAIL_PASSWORD;
import static fpt.edu.capstone.vms.constants.Constants.SettingCode.MAIL_PORT;
import static fpt.edu.capstone.vms.constants.Constants.SettingCode.MAIL_SMTP_AUTH;
import static fpt.edu.capstone.vms.constants.Constants.SettingCode.MAIL_SMTP_DISPLAY_NAME;
import static fpt.edu.capstone.vms.constants.Constants.SettingCode.MAIL_SMTP_STARTTLS_ENABLE;
import static fpt.edu.capstone.vms.constants.Constants.SettingCode.MAIL_USERNAME;

@Service
@AllArgsConstructor
@Slf4j
public class EmailUtils {

    private final SettingUtils settingUtils;

    private JavaMailSenderImpl configEmail(String siteId) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        settingUtils.loadSettingsSite(siteId);
        mailSender.setHost(settingUtils.getOrDefault(MAIL_HOST));
        mailSender.setPort(settingUtils.getInteger(MAIL_PORT, null));
        mailSender.setUsername(settingUtils.getOrDefault(MAIL_USERNAME));
        mailSender.setPassword(settingUtils.getOrDefault(MAIL_PASSWORD));

        Properties properties = mailSender.getJavaMailProperties();
        properties.put(MAIL_SMTP_AUTH, settingUtils.getBoolean(MAIL_SMTP_AUTH));
        properties.put(MAIL_SMTP_STARTTLS_ENABLE, settingUtils.getBoolean(MAIL_SMTP_STARTTLS_ENABLE));

        return mailSender;
    }

    @Async
    public void sendMailWithQRCode(String to, String subject, String body, byte[] qrCodeData, String siteId) {
        try {


            MimeMessagePreparator messagePreparatory = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                messageHelper.setFrom(settingUtils.getOrDefault(MAIL_USERNAME), settingUtils.getOrDefault(MAIL_SMTP_DISPLAY_NAME));
                messageHelper.setTo(to);
                messageHelper.setSubject(subject);
                messageHelper.setText(body, true);
                if (qrCodeData != null) {
                    ByteArrayResource qrCodeAttachment = new ByteArrayResource(qrCodeData);
                    messageHelper.addAttachment("qrcode.png", qrCodeAttachment);
                }
            };


            configEmail(siteId).send(messagePreparatory);
            log.info("Activation email sent!!");
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException(e);
        }

    }

    public String replaceEmailParameters(String emailTemplate, Map<String, String> parameterMap) {
        String replacedTemplate = emailTemplate;

        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            String parameter = "@{" + entry.getKey() + "}";
            String value = entry.getValue();
            replacedTemplate = replacedTemplate.replace(parameter, value);
        }

        return replacedTemplate;
    }
}
