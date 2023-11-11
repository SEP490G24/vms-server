package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.service.IAccessHistoryService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AccessHistoryController implements IAccessHistoryController {

    private final IAccessHistoryService accessHistoryService;
    private final ModelMapper mapper;

    public AccessHistoryController(IAccessHistoryService accessHistoryService, ModelMapper mapper) {
        this.accessHistoryService = accessHistoryService;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<?> findByQRCode(UUID ticketId, UUID customerId) {
        return null;
    }

    @Override
    public ResponseEntity<?> filterAccessHistory(AccessHistoryFilter accessHistoryFilter, Pageable pageable) {
        return ResponseEntity.ok(accessHistoryService.accessHistory(pageable, accessHistoryFilter.getKeyword(), accessHistoryFilter.getStatus(),
            accessHistoryFilter.getFormCheckInTime(), accessHistoryFilter.getToCheckInTime(), accessHistoryFilter.getFormCheckOutTime(),
            accessHistoryFilter.getToCheckOutTime(), accessHistoryFilter.getSite()));
    }
}
