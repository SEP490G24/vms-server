package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.persistence.dto.dashboard.MultiLineResponse;

import java.util.List;

public interface IDashboardService {
    List<IDashboardController.PurposePieResponse> countTicketsByPurposeWithPie(IDashboardController.DashboardDTO dashboardDTO);

    List<MultiLineResponse> countTicketsByPurposeByWithMultiLine(IDashboardController.DashboardDTO dashboardDTO);

    IDashboardController.TotalTicketResponse countTicketsByStatus(IDashboardController.DashboardDTO dashboardDTO);

    IDashboardController.TotalVisitsResponse countVisitsByStatus(IDashboardController.DashboardDTO dashboardDTO);

    List<MultiLineResponse> countTicketsByStatusWithStackedColumn(IDashboardController.DashboardDTO dashboardDTO);

    List<MultiLineResponse> countVisitsByStatusWithStackedColumn(IDashboardController.DashboardDTO dashboardDTO);

    IDashboardController.TicketsPeriodResponse countTicketsPeriod(IDashboardController.DashboardDTO dashboardDTO);
}
