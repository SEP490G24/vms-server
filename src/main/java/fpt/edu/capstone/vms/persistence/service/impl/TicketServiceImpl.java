package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.ITicketService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.EmailUtils;
import fpt.edu.capstone.vms.util.QRcodeUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketServiceImpl extends GenericServiceImpl<Ticket, UUID> implements ITicketService {

    final ModelMapper mapper;
    final TicketRepository ticketRepository;
    final TemplateRepository templateRepository;
    final CustomerRepository customerRepository;


    private static String daysEarlier = "";
    private static int number = 0;

    public TicketServiceImpl(TicketRepository ticketRepository, CustomerRepository customerRepository,
                             TemplateRepository templateRepository, ModelMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.templateRepository = templateRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.init(ticketRepository);
    }


    @Override
    public Ticket create(ITicketController.CreateTicketInfo ticketInfo) {
        //Tạo meeting
        var ticketDto = mapper.map(ticketInfo, Ticket.class);
        ticketDto.setCode(generateMeetingCode(ticketInfo.getPurpose()));

        //Check trùng phòng chưa
        if (isRoomBooked(ticketInfo.getRoomId(), ticketInfo.getStartTime(), ticketInfo.getEndTime())) {
            throw new CustomException(ErrorApp.ROOM_IN_USE);
        }

        Ticket ticket = ticketRepository.save(ticketDto);


        try {
            //Check customer đã tồn tại hay chưa, nếu chưa thì add ,
            //nếu id khác null thì tồn tại r, nếu == null thì tạo mới
            String meetingUrl;
            Template template = templateRepository.findById(ticketInfo.getTemplateId()).orElse(null);
            List<ICustomerController.CreateCustomerDto> createCustomerDtos = ticketInfo.getCreateCustomerDtos();
            for (ICustomerController.CreateCustomerDto customerDto : createCustomerDtos
            ) {
                if (customerDto.getId() == null) {
                    Customer customer = customerRepository.save(mapper.map(customerDto, Customer.class));
                    meetingUrl = "https://web-vms.azurewebsites.net/ticket/" + ticket.getId().toString() + "/customer/" + customer.getId().toString();
                } else {
                    meetingUrl = "https://web-vms.azurewebsites.net/ticket/" + ticket.getId().toString() + "/customer/" + customerDto.getId();
                }

                // Tạo mã QR code
                byte[] qrCodeData = QRcodeUtils.getQRCodeImage(meetingUrl, 400, 400);

                // Gửi email với mã QR code đính kèm

                assert template != null;
                EmailUtils.sendMailWithQRCode(customerDto.getEmail(), template.getSubject(), template.getBody(), qrCodeData);
            }

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
        switch (purpose) {
            case "CONFERENCES" -> purpose = "C";
            case "INTERVIEW" -> purpose = "I";
            case "MEETING" -> purpose = "M";
            case "OTHERS" -> purpose = "O";
            case "WORKING" -> purpose = "W";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String dateCreated = dateFormat.format(new Date());
        if (!dateCreated.equals(daysEarlier)) {
            number = 0;
            daysEarlier = dateCreated;
        }
        number++;
        return purpose + dateCreated + String.format("%04d", number);
    }
}
