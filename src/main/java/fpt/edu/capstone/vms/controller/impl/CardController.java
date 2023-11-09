package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Card;
import fpt.edu.capstone.vms.persistence.service.ICardService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class CardController implements ICardController {
    private final ICardService cardService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<Card> findById(UUID id) {
        return ResponseEntity.ok(cardService.findById(id));
    }

    @Override
    public ResponseEntity<Card> delete(UUID id) {
        return cardService.delete(id);
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(cardService.findAll());
    }

    @Override
    public ResponseEntity<?> create(CardDto cardDto) {
        try {
            var card = cardService.create(cardDto);
            return ResponseEntity.ok(card);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> update(CardDto cardDto, UUID id) {
        try {
            var card = cardService.update(mapper.map(cardDto, Card.class), id);
            return ResponseEntity.ok(card);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> filter(CardFilterDTO filter, boolean isPageable, Pageable pageable) {
        var cardEntity = cardService.filter(
            filter.getNames(),
            filter.getSiteId(),
            filter.getFromDate(),
            filter.getToDate(),
            filter.getKeyword());

        var cardEntityPageable = cardService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteId(),
            filter.getFromDate(),
            filter.getToDate(),
            filter.getKeyword());

        List<CardFilterResponse> cardDtos = mapper.map(cardEntityPageable.getContent(), new TypeToken<List<CardFilterResponse>>() {
        }.getType());

        return isPageable ? ResponseEntity.ok(new PageImpl(cardDtos, pageable, cardDtos.size()))
            : ResponseEntity.ok(mapper.map(cardEntity, new TypeToken<List<CardFilterResponse>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<List<?>> findAllBySiteId(UUID siteId) {
        return ResponseEntity.ok(cardService.finAllBySiteId(siteId));
    }

}
