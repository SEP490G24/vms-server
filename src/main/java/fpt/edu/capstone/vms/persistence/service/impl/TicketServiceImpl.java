package fpt.edu.capstone.vms.persistence.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TicketServiceImpl extends GenericServiceImpl<Ticket, UUID> implements ITicketService {

    public final ModelMapper mapper;
    public final JavaMailSender javaMailSender;
    public final TicketRepository ticketRepository;
    public final TemplateRepository templateRepository;

    @Value("${spring.mail.username}")
    public String sender;

    public byte[] getQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public void sendMailWithAttachment(Ticket ticket, Template template, byte[] qrCodeData) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(ticket.getEmail());
            mimeMessageHelper.setText(template.getBody());
            mimeMessageHelper.setSubject(template.getSubject());

            // Đính kèm mã QR code vào email
            ByteArrayResource qrCodeAttachment = new ByteArrayResource(qrCodeData);
            mimeMessageHelper.addAttachment("qrcode.png", qrCodeAttachment);


            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new CustomException("Không gửi được mail");
        }
    }


    @Override
    public Ticket create(ITicketController.CreateTicketInfo ticketInfo) {
        //Tạo meeting
        var ticketDto = mapper.map(ticketInfo, Ticket.class);

        //Check trùng phòng chưa
        Ticket ticket = ticketRepository.save(ticketDto);

        // Tạo URL chi tiết cuộc họp
        String meetingUrl = "https://web-vms.azurewebsites.net/ticket/" + ticket.getId().toString();

        try {
            // Tạo mã QR code
            byte[] qrCodeData = getQRCodeImage(meetingUrl, 400, 400);
            Template template = templateRepository.findById(ticketInfo.getTemplateId()).orElse(null);
            // Gửi email với mã QR code đính kèm
            sendMailWithAttachment(ticket, template, qrCodeData);
        } catch (Exception e) {
            throw new CustomException("Lỗi trong quá trình tạo meeting");
        }
        return ticket;
    }
}
