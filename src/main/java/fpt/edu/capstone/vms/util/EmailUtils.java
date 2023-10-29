package fpt.edu.capstone.vms.util;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.exception.CustomException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


public class EmailUtils {

    private static JavaMailSender javaMailSender;

    private static final String sender = "xuantrinhxq2@gmail.com";

    public static void sendMailWithQRCode(String to, String subject, String text, byte[] qrCodeData) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setText(text);
            mimeMessageHelper.setSubject(subject);

            ByteArrayResource qrCodeAttachment = new ByteArrayResource(qrCodeData);
            mimeMessageHelper.addAttachment("qrcode.png", qrCodeAttachment);


            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new CustomException(ErrorApp.EMAIL_SEND_FAILED);
        }
    }
}
