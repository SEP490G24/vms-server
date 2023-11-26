package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.persistence.service.IDashboardService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardController implements IDashboardController {

    final IDashboardService dashboardService;

    @Override
    public ResponseEntity<?> countTicketsByPurposeWithPie(DashboardDTO dashboardDTO) {
        return ResponseEntity.ok(dashboardService.countTicketsByPurposeWithPie(dashboardDTO));
    }

    @Override
    public ResponseEntity<?> countTicketsByPurposeByWithMultiLine(DashboardDTO dashboardDTO, String limit) {
        return ResponseEntity.ok(dashboardService.countTicketsByPurposeByWithMultiLine(dashboardDTO, limit));
    }
}
