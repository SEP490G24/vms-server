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
            if (emitterFuture.isDone()) {
                emitterFuture.thenAccept(emitter -> {
                    try {
                        // Log some information to check if this part is reached
                        System.out.println("Sending SSE event to client");

                        // Make sure 'ticket' and 'checkInPayload' are not null
                        if (ticket != null && checkInPayload != null) {
                            emitter.send(SseEmitter.event()
                                .data(ticket)
                                .name("CHECK_IN_EVENT"));
                        } else {
                            System.out.println("ticket or checkInPayload is null");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("CompletableFuture not yet completed");
            }
        } else {
            System.out.println("No CompletableFuture found for checkInPayload");
        }
    }

}
