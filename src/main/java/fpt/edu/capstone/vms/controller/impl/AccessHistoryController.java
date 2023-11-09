package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AccessHistoryController implements IAccessHistoryController {

    @Override
    public ResponseEntity<?> findByQRCode(UUID ticketId, UUID customerId) {
        return null;
    }

    @Override
    public ResponseEntity<?> filterAccessHistory(TicketFilterUser ticketFilterUser, Pageable pageable) {
        return null;
    }
}
