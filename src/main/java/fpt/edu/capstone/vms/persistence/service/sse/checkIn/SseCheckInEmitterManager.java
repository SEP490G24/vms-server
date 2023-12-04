package fpt.edu.capstone.vms.persistence.service.sse.checkIn;

import fpt.edu.capstone.vms.controller.ICardController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.util.JacksonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class SseCheckInEmitterManager {
    public static final String CHECK_IN_EVENT = "CHECK_IN_EVENT";
    public static final String SCAN_CARD_EVENT = "SCAN_CARD_EVENT";

    private final SiteRepository siteRepository;

    private final Map<SseCheckInSession, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    @Autowired
    public SseCheckInEmitterManager(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public void addSubscribeEmitter(SseCheckInSession sseCheckInSession, SseEmitter emitter) {
        emitterMap.put(sseCheckInSession, emitter);
    }

    public void removeEmitter(SseCheckInSession sseCheckInSession) {
        emitterMap.remove(sseCheckInSession);
    }

    public void sendSseToClient(SseCheckInSession sseCheckInSession, ITicketController.TicketByQRCodeResponseDTO ticket) {
        SseEmitter sseEmitter = emitterMap.get(sseCheckInSession);
        sendSseToClient(sseEmitter, ticket);
    }

    public void sendSseToClient(SseEmitter sseEmitter, ITicketController.TicketByQRCodeResponseDTO ticket) {
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event()
                    .data(JacksonUtils.toJson(ticket))
                    .name(CHECK_IN_EVENT));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void broadcast(String siteId, ITicketController.TicketByQRCodeResponseDTO ticket) {
        var organizationId = siteRepository.findOrganizationIdBySiteId(UUID.fromString(siteId)).orElse(null);
        List<SseEmitter> emitters = emitterMap.entrySet().stream().filter((entry) -> {
            if (entry.getKey().getOrganizationId() != null && entry.getKey().getOrganizationId().equals(organizationId)) return true;
            else return entry.getKey().getSiteId() != null && entry.getKey().getSiteId().equals(siteId);
        }).map(Map.Entry::getValue).toList();
        emitters.forEach(sseEmitter -> sendSseToClient(sseEmitter, ticket));
    }

    public void sendSseToClient(SseEmitter sseEmitter, ICardController.CardCheckDTO cardCheckDTO) {
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event()
                    .data(JacksonUtils.toJson(cardCheckDTO))
                    .name(SCAN_CARD_EVENT));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void broadcast(String siteId, ICardController.CardCheckDTO cardCheckDTO) {
        List<SseEmitter> emitters = emitterMap.entrySet().stream().filter((entry) -> siteId.equals(entry.getKey().getSiteId())).map(Map.Entry::getValue).toList();
        emitters.forEach(sseEmitter -> sendSseToClient(sseEmitter, cardCheckDTO));
    }
}
