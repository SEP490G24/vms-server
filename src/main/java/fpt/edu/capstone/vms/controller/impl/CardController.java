package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.service.ICardCheckInHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@AllArgsConstructor
public class CardController implements ICardController {
    private final ICardCheckInHistoryService cardCheckInHistoryService;

    @Override
    public ResponseEntity<?> checkCard(CardCheckDTO cardCheckDTO) {
        try {
            return ResponseEntity.ok(cardCheckInHistoryService.checkCard(cardCheckDTO));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

}
