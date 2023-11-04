package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.persistence.entity.Card;
import fpt.edu.capstone.vms.persistence.repository.CardRepository;
import fpt.edu.capstone.vms.persistence.service.ICardService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CardServiceImpl extends GenericServiceImpl<Card, UUID> implements ICardService {

    private final CardRepository cardRepository;
    private final ModelMapper mapper;


    public CardServiceImpl(CardRepository cardRepository, ModelMapper mapper) {
        this.cardRepository = cardRepository;
        this.mapper = mapper;
        this.init(cardRepository);
    }

    @Override
    public Card update(Card cardInfo, UUID id) {
        var card = cardRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(card))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found card");
        cardRepository.save(card.update(cardInfo));
        return card;
    }

    @Override
    @Transactional
    public Card create(ICardController.CardDto cardDto) {
        if (ObjectUtils.isEmpty(cardDto))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Object is empty");
        if (StringUtils.isEmpty(cardDto.getSiteId().toString()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");
        var card = mapper.map(cardDto, Card.class);
        cardRepository.save(card);
        return card;
    }

    @Override
    public Page<Card> filter(Pageable pageable, List<String> names, UUID siteId, LocalDateTime fromDate, LocalDateTime toDate, String keyword) {
        return cardRepository.filter(
            pageable,
            names,
            siteId,
            fromDate,
            toDate,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Card> filter(List<String> names, UUID siteId, LocalDateTime fromDate, LocalDateTime toDate, String keyword) {
        return cardRepository.filter(
            names,
            siteId,
            fromDate,
            toDate,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Card> finAllBySiteId(UUID siteId) {
        return cardRepository.findAllBySiteId(siteId);
    }
}
