package fpt.edu.capstone.vms.persistence.service.sse.checkIn;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class SseCheckInSession {
    private String organizationId;
    private String siteId;
    private String username;
    private UUID sessionId;
}
