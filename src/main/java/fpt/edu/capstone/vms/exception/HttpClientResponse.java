package fpt.edu.capstone.vms.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HttpClientResponse {
    private LocalDateTime timestamp;
    private String message;

    public HttpClientResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }
}
