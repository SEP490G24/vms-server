package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.CardCheckInHistory;
import fpt.edu.capstone.vms.persistence.repository.CardCheckInHistoryRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TicketRepository;
import fpt.edu.capstone.vms.persistence.service.ICardCheckInHistoryService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;


@Service
public class CardCheckInHistoryServiceImpl extends GenericServiceImpl<CardCheckInHistory, Integer> implements ICardCheckInHistoryService {

    private final CardCheckInHistoryRepository cardCheckInHistoryRepository;
    private final TicketRepository ticketRepository;
    private final CustomerTicketMapRepository customerTicketMapRepository;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;


    public CardCheckInHistoryServiceImpl(CardCheckInHistoryRepository cardCheckInHistoryRepository, TicketRepository ticketRepository, CustomerTicketMapRepository customerTicketMapRepository, SiteRepository siteRepository, ModelMapper mapper) {
        this.cardCheckInHistoryRepository = cardCheckInHistoryRepository;
        this.ticketRepository = ticketRepository;
        this.customerTicketMapRepository = customerTicketMapRepository;
        this.siteRepository = siteRepository;
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

    @Override
    public Page<ITicketController.CardCheckInHistoryDTO> getAllCardHistoryOfCustomer(Pageable pageable, String checkInCode) {
        var customerTicket = customerTicketMapRepository.findByCheckInCodeIgnoreCase(checkInCode);
        if (customerTicket != null) {
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, customerTicket.getTicketEntity().getSiteId())) {
                throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
            }
            return cardCheckInHistoryRepository.getAllCardHistoryOfCustomer(pageable, checkInCode);
        }
        return null;
    }

    public boolean checkCardCheckInHistory(ICardController.CardCheckDTO cardCheckDTO) {
        if (ObjectUtils.isEmpty(cardCheckDTO) || cardCheckDTO == null) {
            return false;
        }
        var customerTicket = customerTicketMapRepository.findByCardId(cardCheckDTO.getCardId());
        if (customerTicket != null) {
            var ticket = ticketRepository.findById(customerTicket.getCustomerTicketMapPk().getTicketId()).orElse(null);
            LocalDateTime timeNow = LocalDateTime.now();
            if (customerTicket.getStatus().equals(Constants.StatusCustomerTicket.CHECK_IN)) {
                // check in time > time now
                if (customerTicket.getCheckInTime().isAfter(timeNow)) {
                    return false;
                }
            }
            if (customerTicket.getStatus().equals(Constants.StatusCustomerTicket.CHECK_OUT)) {
                // check out time < time now
                if (customerTicket.getCheckOutTime().isBefore(timeNow)) {
                    return false;
                }
            }
            if (ticket != null) {
                if (ticket.getRoom() != null && ticket.getRoom().getDevice() != null) {
                    if (cardCheckDTO.getMacIp() != null && !cardCheckDTO.getMacIp().equals(ticket.getRoom().getDevice().getMacIp())) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
