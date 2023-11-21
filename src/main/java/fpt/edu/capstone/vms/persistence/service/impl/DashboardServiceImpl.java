package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.persistence.repository.DashboardRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IDashboardService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    final DashboardRepository dashboardRepository;
    final SiteRepository siteRepository;

    @Override
    public List<IDashboardController.DashboardResponse> getTicketStatsByPurpose(IDashboardController.DashboardDTO dashboardDTO, String limit) {
        LocalDate currentTime = LocalDate.now();
        if ("date".equals(limit)) {
            return convertToTicketStats(dashboardRepository.countTicketsByPurposeAndDate(java.sql.Date.valueOf(currentTime), SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites())));
        } else if ("month".equals(limit)) {
            return convertToTicketStats(dashboardRepository.countTicketsByPurposeAndMonth(currentTime.getMonthValue(), SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites())));
        } else if ("year".equals(limit)) {
            return convertToTicketStats(dashboardRepository.countTicketsByPurposeAndYear(currentTime.getYear(), SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites())));
        }
        return null;
    }

    private List<IDashboardController.DashboardResponse> convertToTicketStats(List<Object[]> result) {
        return result.stream()
            .map(row -> new IDashboardController.DashboardResponse((Constants.Purpose) row[0], ((Number) row[1]).intValue()))
            .collect(Collectors.toList());
    }
}
