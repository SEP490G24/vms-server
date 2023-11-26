package fpt.edu.capstone.vms.util;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public final class SseUtils {
    public static void sendInitEvent(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event().name("INIT"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
