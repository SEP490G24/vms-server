package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.service.IAccessHistoryService;
import net.sf.jasperreports.engine.JRException;
import org.apache.http.HttpStatus;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    public ResponseEntity<?> viewDetailAccessHistory(UUID ticketId, UUID customerId) {
        return ResponseEntity.ok(accessHistoryService.viewAccessHistoryDetail(ticketId, customerId));
    }

    @Override
    public ResponseEntity<?> filterAccessHistory(AccessHistoryFilter accessHistoryFilter, Pageable pageable) {
        return ResponseEntity.ok(accessHistoryService.accessHistory(pageable, accessHistoryFilter.getKeyword(), accessHistoryFilter.getStatus(),
            accessHistoryFilter.getFormCheckInTime(), accessHistoryFilter.getToCheckInTime(), accessHistoryFilter.getFormCheckOutTime(),
            accessHistoryFilter.getToCheckOutTime(), accessHistoryFilter.getSite()));
    }

    @Override
    public ResponseEntity<?> export(AccessHistoryFilter ticketFilterUser) throws JRException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "access_history.xlsx");
        return ResponseEntity.status(HttpStatus.SC_OK).headers(headers).body(accessHistoryService.export(ticketFilterUser));
    }
}
