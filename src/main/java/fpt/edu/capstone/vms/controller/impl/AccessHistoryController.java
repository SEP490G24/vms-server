package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAccessHistoryController;
import fpt.edu.capstone.vms.persistence.entity.CustomerTicketMap;
import fpt.edu.capstone.vms.persistence.service.IAccessHistoryService;
import fpt.edu.capstone.vms.persistence.service.excel.ExportAccessHistory;
import net.sf.jasperreports.engine.JRException;
import org.apache.http.HttpStatus;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccessHistoryController implements IAccessHistoryController {

    private final IAccessHistoryService accessHistoryService;
    private final ExportAccessHistory exportAccessHistory;
    private final ModelMapper mapper;

    public AccessHistoryController(IAccessHistoryService accessHistoryService, ExportAccessHistory exportAccessHistory, ModelMapper mapper) {
        this.accessHistoryService = accessHistoryService;
        this.exportAccessHistory = exportAccessHistory;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<?> viewDetailAccessHistory(String checkInCode) {
        return ResponseEntity.ok(accessHistoryService.viewAccessHistoryDetail(checkInCode));
    }

    @Override
    public ResponseEntity<?> filterAccessHistory(AccessHistoryFilter accessHistoryFilter, Pageable pageable) {
        if (CollectionUtils.isEmpty(accessHistoryFilter.getStatus())) {
            accessHistoryFilter.setStatus(List.of(Constants.StatusCustomerTicket.CHECK_IN, Constants.StatusCustomerTicket.CHECK_OUT));
            Page<CustomerTicketMap> customerTicketMapPage = accessHistoryService.accessHistory(pageable, accessHistoryFilter.getKeyword(), accessHistoryFilter.getStatus(),
                accessHistoryFilter.getFormCheckInTime(), accessHistoryFilter.getToCheckInTime(), accessHistoryFilter.getFormCheckOutTime(),
                accessHistoryFilter.getToCheckOutTime(), accessHistoryFilter.getSites());
            List<AccessHistoryResponseDTO> accessHistoryResponseDTOS = mapper.map(customerTicketMapPage.getContent(), new TypeToken<List<AccessHistoryResponseDTO>>() {
            }.getType());
            Page<IAccessHistoryController.AccessHistoryResponseDTO> listData = new PageImpl<>(accessHistoryResponseDTOS, pageable, customerTicketMapPage.getTotalElements());
            return ResponseEntity.ok(listData);
        } else {
            if (accessHistoryFilter.getStatus().contains(Constants.StatusCustomerTicket.CHECK_IN) || accessHistoryFilter.getStatus().contains(Constants.StatusCustomerTicket.CHECK_OUT)
            ) {
                Page<CustomerTicketMap> customerTicketMapPage = accessHistoryService.accessHistory(pageable, accessHistoryFilter.getKeyword(), accessHistoryFilter.getStatus(),
                    accessHistoryFilter.getFormCheckInTime(), accessHistoryFilter.getToCheckInTime(), accessHistoryFilter.getFormCheckOutTime(),
                    accessHistoryFilter.getToCheckOutTime(), accessHistoryFilter.getSites());
                List<AccessHistoryResponseDTO> accessHistoryResponseDTOS = mapper.map(customerTicketMapPage.getContent(), new TypeToken<List<AccessHistoryResponseDTO>>() {
                }.getType());
                Page<IAccessHistoryController.AccessHistoryResponseDTO> listData = new PageImpl<>(accessHistoryResponseDTOS, pageable, customerTicketMapPage.getTotalElements());
                return ResponseEntity.ok(listData);
            } else {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @Override
    public ResponseEntity<?> export(AccessHistoryFilter ticketFilterUser) throws JRException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "access_history.xlsx");
        return ResponseEntity.status(HttpStatus.SC_OK).headers(headers).body(exportAccessHistory.export(ticketFilterUser));
    }
}
