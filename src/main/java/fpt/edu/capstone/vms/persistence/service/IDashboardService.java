package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.persistence.dto.dashboard.MonthlyMultiLineResponse;

import java.util.List;

public interface IDashboardService {
    List<IDashboardController.PurposePieResponse> countTicketsByPurposeWithPie(IDashboardController.DashboardDTO dashboardDTO);

    List<MonthlyMultiLineResponse> countTicketsByPurposeByWithMultiLine(IDashboardController.DashboardDTO dashboardDTO, String limit);
}
