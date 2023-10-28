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
import fpt.edu.capstone.vms.persistence.repository.RoomRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketServiceImpl extends GenericServiceImpl<Ticket, UUID> implements ITicketService {

    final ModelMapper mapper;
    //    final JavaMailSender javaMailSender;
    final TicketRepository ticketRepository;
    final TemplateRepository templateRepository;
    final RoomRepository roomRepository;


    private static String daysEarlier = "";
    private static int number = 0;

    public TicketServiceImpl(TicketRepository ticketRepository
        , TemplateRepository templateRepository
        , RoomRepository roomRepository
//        , JavaMailSender javaMailSender
        , ModelMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.roomRepository = roomRepository;
        this.templateRepository = templateRepository;
//        this.javaMailSender = javaMailSender;
        this.mapper = mapper;
        this.init(ticketRepository);
    }

//    @Value("${spring.mail.username}")
//    String sender;

    public byte[] getQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public void sendMailWithAttachment(Ticket ticket, Template template, byte[] qrCodeData) {
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        MimeMessageHelper mimeMessageHelper;
//
//        try {
//            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
//            mimeMessageHelper.setFrom(sender);
//            mimeMessageHelper.setTo("");
//            mimeMessageHelper.setText(template.getBody());
//            mimeMessageHelper.setSubject(template.getSubject());
//
//            // Đính kèm mã QR code vào email
//            ByteArrayResource qrCodeAttachment = new ByteArrayResource(qrCodeData);
//            mimeMessageHelper.addAttachment("qrcode.png", qrCodeAttachment);
//
//
//            javaMailSender.send(mimeMessage);
//        } catch (MessagingException e) {
//            throw new CustomException("Không gửi được mail");
//        }
    }


    @Override
    public Ticket create(ITicketController.CreateTicketInfo ticketInfo) {
        //Tạo meeting
        var ticketDto = mapper.map(ticketInfo, Ticket.class);
        ticketDto.setCode(generateMeetingCode(ticketInfo.getPurpose()));

        //Check trùng phòng chưa
        if (isRoomBooked(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
            throw new CustomException("Phòng đang được sử dụng, vui lòng chọn phòng khác");
        }

        Ticket ticket = ticketRepository.save(ticketDto);

        // Tạo URL chi tiết cuộc họp
        String meetingUrl = "https://web-vms.azurewebsites.net/ticket/" + ticket.getId().toString();

        try {
            // Tạo mã QR code
            byte[] qrCodeData = getQRCodeImage(meetingUrl, 400, 400);
            Template template = templateRepository.findById(ticketInfo.getTemplateId()).orElse(null);
            // Gửi email với mã QR code đính kèm
            assert template != null;
            sendMailWithAttachment(ticket, template, qrCodeData);
        } catch (Exception e) {
            throw new CustomException("Lỗi trong quá trình tạo meeting");
        }
        return ticket;
    }

    public boolean isRoomBooked(UUID roomId, LocalDateTime startTime, LocalDateTime endTime) {
        int count = ticketRepository.countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqual(roomId, startTime, endTime);
        return count > 0;
    }

    public static String generateMeetingCode(String purpose) {
        if (purpose.equals("CONFERENCES")) {
            purpose = "C";
        } else if (purpose.equals("INTERVIEW")) {
            purpose = "I";
        } else if (purpose.equals("MEETING")) {
            purpose = "M";
        } else if (purpose.equals("OTHERS")) {
            purpose = "O";
        } else if (purpose.equals("WORKING")) {
            purpose = "W";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String dateCreated = dateFormat.format(new Date());
        if (!dateCreated.equals(daysEarlier)) {
            number = 0;
            daysEarlier = dateCreated;
        }
        number++;
        String soPhieuBaoHanh = purpose + dateCreated + String.format("%04d", number);
        return soPhieuBaoHanh;
    }
}
