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

import java.util.Properties;

@Service
@AllArgsConstructor
@Slf4j
public class EmailUtils {

//    private final JavaMailSender javaMailSender;

    private static final String sender = "xuantrinhxq2@gmail.com";

    @Async
    public void sendMailWithQRCode(String to, String subject, String body, byte[] qrCodeData) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost("smtp.1blu.de");
//        mailSender.setPort(465);
//        mailSender.setUsername("z323749_0-bh-etk-test");
//        mailSender.setPassword("iW@cbuYuX)MO)5U");
//        mailSender.setProtocol("smtps");
//
//        Properties properties = mailSender.getJavaMailProperties();
//        properties.put("mail.smtp.auth", true);
//        properties.put("mail.smtp.starttls.enable", true);

        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("xuantrinhxq2@gmail.com");
        mailSender.setPassword("ovawvgfjvnaouxkw");

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);

        MimeMessagePreparator messagePreparatory = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(sender);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(body, true);
            if (qrCodeData != null) {
                ByteArrayResource qrCodeAttachment = new ByteArrayResource(qrCodeData);
                messageHelper.addAttachment("qrcode.png", qrCodeAttachment);
            }
        };

        try {
            mailSender.send(messagePreparatory);
            log.info("Activation email sent!!");
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException(e);
        }

    }
}
