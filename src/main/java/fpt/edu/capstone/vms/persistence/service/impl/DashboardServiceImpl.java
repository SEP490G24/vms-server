package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.persistence.dto.dashboard.MonthlyMultiLineResponse;
import fpt.edu.capstone.vms.persistence.repository.DashboardRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IDashboardService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    final DashboardRepository dashboardRepository;
    final SiteRepository siteRepository;

    @Override
    public List<IDashboardController.PurposePieResponse> countTicketsByPurposeWithPie(IDashboardController.DashboardDTO dashboardDTO) {
        return convertToTicketStats(dashboardRepository.countTicketsByPurposeWithPie(dashboardDTO.getFromTime(), dashboardDTO.getToTime(), dashboardDTO.getYear(), dashboardDTO.getMonth(), SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites())));
    }

    @Override
    public List<MonthlyMultiLineResponse> countTicketsByPurposeByWithMultiLine(IDashboardController.DashboardDTO dashboardDTO, String limit) {
        return MonthlyMultiLineResponse.fillMissingData(convertToMonthlyTicketStats(dashboardRepository.countTicketsByPurposeByWithMultiLine(dashboardDTO.getYear(), SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))));
    }

    private List<IDashboardController.PurposePieResponse> convertToTicketStats(List<Object[]> result) {
        return result.stream()
            .map(row -> new IDashboardController.PurposePieResponse((Constants.Purpose) row[0], ((Number) row[1]).intValue()))
            .collect(Collectors.toList());
    }

    private List<MonthlyMultiLineResponse> convertToMonthlyTicketStats(List<Object[]> result) {
        Map<String, Map<String, Integer>> monthTypeCounts = new HashMap<>();

        for (Object[] row : result) {
            if (row[0] != null && row[1] != null && row[2] != null) {
                String formattedMonth = (String) row[0];
                String purpose = row[1].toString();
                int count = ((Number) row[2]).intValue();

                monthTypeCounts
                    .computeIfAbsent(formattedMonth, k -> new HashMap<>())
                    .put(purpose, count);
            }
        }

        List<MonthlyMultiLineResponse> responseList = new ArrayList<>();
        monthTypeCounts.forEach((formattedMonth, purposeCounts) ->
            purposeCounts.forEach((purpose, count) ->
                responseList.add(new MonthlyMultiLineResponse(formattedMonth, purpose, count))
            )
        );

        return responseList;
    }
}
