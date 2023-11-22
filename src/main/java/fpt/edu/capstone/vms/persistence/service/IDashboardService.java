package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.IDashboardController;

import java.util.List;

public interface IDashboardService {
    List<IDashboardController.DashboardResponse> getTicketStatsByPurpose(IDashboardController.DashboardDTO dashboardDTO, String limit);
}
