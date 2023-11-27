package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.repository.DeviceRepository;
import fpt.edu.capstone.vms.persistence.service.ICardCheckInHistoryService;
import fpt.edu.capstone.vms.persistence.service.sse.checkIn.SseCheckInEmitterManager;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@AllArgsConstructor
public class CardController implements ICardController {
    private final ICardCheckInHistoryService cardCheckInHistoryService;
    private final SseCheckInEmitterManager sseCheckInEmitterManager;
    private final DeviceRepository deviceRepository;

    @Override
    public ResponseEntity<?> checkCard(CardCheckDTO cardCheckDTO) {
        try {
            return ResponseEntity.ok(cardCheckInHistoryService.checkCard(cardCheckDTO));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> scanCard(CardCheckDTO cardDto) {
        if (cardDto == null) {
            return ResponseEntity.badRequest().body(new HttpClientResponse("Card is required"));
        }
        if (cardDto.getMacIp() == null || cardDto.getMacIp().isEmpty()) {
            return ResponseEntity.badRequest().body(new HttpClientResponse("MacIp is required"));
        }
        if (cardDto.getCardId() == null || cardDto.getCardId().isEmpty()) {
            return ResponseEntity.badRequest().body(new HttpClientResponse("CardId is required"));
        }
        var device = deviceRepository.findByMacIp(cardDto.getMacIp());
        if (device == null) {
            return ResponseEntity.badRequest().body(new HttpClientResponse("Device not found"));
        }
        sseCheckInEmitterManager.broadcast(device.getSiteId().toString(), cardDto);
        return ResponseEntity.ok().body(new HttpClientResponse("Success"));
    }

}
