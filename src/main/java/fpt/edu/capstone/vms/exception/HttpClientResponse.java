package fpt.edu.capstone.vms.exception;

import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HttpClientResponse {
    private LocalDateTime timestamp;
    private int statusCode;
    private String message;

    public HttpClientResponse(HttpStatusCode statusCode, String message) {
        this.timestamp = LocalDateTime.now();
        this.statusCode = statusCode.value();
        this.message = message;
    }
}
