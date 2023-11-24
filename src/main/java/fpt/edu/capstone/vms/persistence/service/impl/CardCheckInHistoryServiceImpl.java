package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.persistence.entity.CardCheckInHistory;
import fpt.edu.capstone.vms.persistence.repository.CardCheckInHistoryRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.ICardCheckInHistoryService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;


@Service
public class CardCheckInHistoryServiceImpl extends GenericServiceImpl<CardCheckInHistory, Integer> implements ICardCheckInHistoryService {

    private final CardCheckInHistoryRepository cardCheckInHistoryRepository;
    private final TicketRepository ticketRepository;
    private final CustomerTicketMapRepository customerTicketMapRepository;
    private final ModelMapper mapper;


    public CardCheckInHistoryServiceImpl(CardCheckInHistoryRepository cardCheckInHistoryRepository, TicketRepository ticketRepository, CustomerTicketMapRepository customerTicketMapRepository, ModelMapper mapper) {
        this.cardCheckInHistoryRepository = cardCheckInHistoryRepository;
        this.ticketRepository = ticketRepository;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.mapper = mapper;
        this.init(cardCheckInHistoryRepository);
    }


    @Override
    public boolean checkCard(ICardController.CardCheckDTO cardCheckDTO) {
        var check = checkCardCheckInHistory(cardCheckDTO);
        var cardCheckInHistory = new CardCheckInHistory();
        var customerTicket = customerTicketMapRepository.findByCardId(cardCheckDTO.getCardId());
        if (customerTicket == null) {
            return false;
        }
        cardCheckInHistory.setCheckInCode(customerTicket.getCheckInCode());
        cardCheckInHistory.setMacIp(cardCheckDTO.getMacIp());
        if (check) {
            cardCheckInHistory.setStatus(Constants.StatusCheckInCard.APPROVED);
        } else {
            cardCheckInHistory.setStatus(Constants.StatusCheckInCard.DENIED);
        }
        cardCheckInHistoryRepository.save(cardCheckInHistory);
        return check;
    }

    private boolean checkCardCheckInHistory(ICardController.CardCheckDTO cardCheckDTO) {
        if (ObjectUtils.isEmpty(cardCheckDTO)) {
            return false;
        }
        var customerTicket = customerTicketMapRepository.findByCardId(cardCheckDTO.getCardId());
        if (customerTicket != null) {
            var ticket = ticketRepository.findById(customerTicket.getCustomerTicketMapPk().getTicketId()).orElse(null);
            LocalDateTime timeNow = LocalDateTime.now();
            if (customerTicket.getStatus().equals(Constants.StatusTicket.CHECK_IN)) {
                if (customerTicket.getCheckInTime().isAfter(timeNow)) {
                    return false;
                }
            }
            if (customerTicket.getStatus().equals(Constants.StatusTicket.CHECK_OUT)) {
                if (customerTicket.getCheckOutTime().isBefore(timeNow)) {
                    return false;
                }
            }
            if (ticket != null) {
                if (ticket.getRoom().getMacIp() != null) {
                    if (cardCheckDTO.getMacIp() != null && !cardCheckDTO.getMacIp().equals(ticket.getRoom().getMacIp())) {
                        return false;
                    }
                }
                return true;
            }

        }
        return false;
    }
}
