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
    public ResponseEntity<?> countTicketsByPurposeByWithMultiLine(DashboardDTO dashboardDTO) {
        return ResponseEntity.ok(dashboardService.countTicketsByPurposeByWithMultiLine(dashboardDTO));
    }

    @Override
    public ResponseEntity<?> countTicketsByStatus(DashboardDTO dashboardDTO) {
        return ResponseEntity.ok(dashboardService.countTicketsByStatus(dashboardDTO));
    }

    @Override
    public ResponseEntity<?> countTicketsByStatusWithStackedColumn(DashboardDTO dashboardDTO) {
        return ResponseEntity.ok(dashboardService.countTicketsByStatusWithStackedColumn(dashboardDTO));
    }

    @Override
    public ResponseEntity<?> countVisitsByStatus(DashboardDTO dashboardDTO) {
        return ResponseEntity.ok(dashboardService.countVisitsByStatus(dashboardDTO));
    }

    @Override
    public ResponseEntity<?> countVisitsByStatusWithStackedColumn(DashboardDTO dashboardDTO) {
        return ResponseEntity.ok(dashboardService.countVisitsByStatusWithStackedColumn(dashboardDTO));
    }

    @Override
    public ResponseEntity<?> countTicketsPeriod(DashboardDTO dashboardDTO) {
        return ResponseEntity.ok(dashboardService.countTicketsPeriod(dashboardDTO));
    }
}
