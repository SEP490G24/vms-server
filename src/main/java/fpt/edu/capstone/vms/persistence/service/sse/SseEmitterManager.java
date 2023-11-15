package fpt.edu.capstone.vms.persistence.service.sse;

import fpt.edu.capstone.vms.controller.ITicketController;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterManager {
    private final Map<ITicketController.CheckInPayload, CompletableFuture<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void addEmitter(ITicketController.CheckInPayload checkInPayload, SseEmitter emitter) {
        CompletableFuture<SseEmitter> future = new CompletableFuture<>();
        emitters.put(checkInPayload, future);
    }

    public void removeEmitter(ITicketController.CheckInPayload checkInPayload, SseEmitter emitter) {
        emitters.computeIfPresent(checkInPayload, (k, v) -> {
            v.completeExceptionally(new IOException("Emitter removed"));
            return null;
        });
        emitters.remove(checkInPayload);
    }

    public void sendSseToClient(ITicketController.CheckInPayload checkInPayload, ITicketController.TicketByQRCodeResponseDTO ticket) {
        CompletableFuture<SseEmitter> emitterFuture = emitters.get(checkInPayload);
        if (emitterFuture != null) {
            emitterFuture.thenAccept(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                        .data(ticket)
                        .name("CHECK_IN_EVENT"));
                } catch (IOException e) {
                    // Handle exception
                }
            });
        }
    }
}
