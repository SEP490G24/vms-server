package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface ITicketService extends IGenericService<Ticket, UUID> {
    Ticket create(ITicketController.CreateTicketInfo ticketInfo);

    Boolean updateBookMark(ITicketController.TicketBookmark ticketBookmark);

    Boolean deleteTicket(String ticketId);

    Boolean cancelTicket(ITicketController.CancelTicket cancelTicket);

    Ticket updateTicket(ITicketController.UpdateTicketInfo ticketInfo);

    Page<Ticket> getAllTicketPageableByUsername(Pageable pageable,
                                                List<String> names,
                                                UUID roomId,
                                                Constants.StatusTicket status,
                                                Constants.Purpose purpose,
                                                LocalDateTime createdOnStart,
                                                LocalDateTime createdOnEnd,
                                                LocalDateTime startTimeStart,
                                                LocalDateTime startTimeEnd,
                                                LocalDateTime endTimeStart,
                                                LocalDateTime endTimeEnd,
                                                String createdBy,
                                                String lastUpdatedBy,
                                                Boolean bookmark,
                                                String keyword);

    Page<Ticket> filterAllBySite(Pageable pageable,
                                 List<String> names,
                                 List<String> sites,
                                 List<String> usernames,
                                 UUID roomId,
                                 Constants.StatusTicket status,
                                 Constants.Purpose purpose,
                                 LocalDateTime createdOnStart,
                                 LocalDateTime createdOnEnd,
                                 LocalDateTime startTimeStart,
                                 LocalDateTime startTimeEnd,
                                 LocalDateTime endTimeStart,
                                 LocalDateTime endTimeEnd,
                                 String createdBy,
                                 String lastUpdatedBy,
                                 Boolean bookmark,
                                 String keyword);

    List<Ticket> getAllTicketByUsername(List<String> names,
                                        UUID roomId,
                                        Constants.StatusTicket status,
                                        Constants.Purpose purpose,
                                        LocalDateTime createdOnStart,
                                        LocalDateTime createdOnEnd,
                                        LocalDateTime startTimeStart,
                                        LocalDateTime startTimeEnd,
                                        LocalDateTime endTimeStart,
                                        LocalDateTime endTimeEnd,
                                        String createdBy,
                                        String lastUpdatedBy,
                                        Boolean bookmark,
                                        String keyword);

    List<Ticket> filterAllBySite(List<String> names,
                                 List<String> sites,
                                 List<String> usernames,
                                 UUID roomId,
                                 Constants.StatusTicket status,
                                 Constants.Purpose purpose,
                                 LocalDateTime createdOnStart,
                                 LocalDateTime createdOnEnd,
                                 LocalDateTime startTimeStart,
                                 LocalDateTime startTimeEnd,
                                 LocalDateTime endTimeStart,
                                 LocalDateTime endTimeEnd,
                                 String createdBy,
                                 String lastUpdatedBy,
                                 Boolean bookmark,
                                 String keyword);

    ITicketController.TicketByQRCodeResponseDTO findByQRCode(String checkInCode);

    ITicketController.TicketByQRCodeResponseDTO updateStatusCustomerOfTicket(ITicketController.CheckInPayload checkInPayload);

    ITicketController.TicketFilterDTO findByTicket(UUID ticketId);

    Page<CustomerTicketMap> filterTicketAndCustomer(
        Pageable pageable,
        List<String> sites,
        List<String> names,
        UUID roomId,
        Constants.Purpose purpose,
        LocalDateTime createdOnStart,
        LocalDateTime createdOnEnd,
        LocalDateTime startTimeStart,
        LocalDateTime startTimeEnd,
        LocalDateTime endTimeStart,
        LocalDateTime endTimeEnd,
        String createdBy,
        String lastUpdatedBy,
        Boolean bookmark,
        String keyword
    );

    boolean addCardCustomerTicket(ITicketController.CustomerTicketCardDTO customerTicketCardDTO);

    ITicketController.TicketByRoomResponseDTO filterTicketByRoom(List<String> names, List<String> sites, List<String> usernames, UUID roomId, Constants.StatusTicket status, Constants.Purpose purpose, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, LocalDateTime startTimeStart, LocalDateTime startTimeEnd, LocalDateTime endTimeStart, LocalDateTime endTimeEnd, String createdBy, String lastUpdatedBy, String keyword);

    boolean isRoomBooked(UUID roomId, LocalDateTime startTime, LocalDateTime endTime);
}
