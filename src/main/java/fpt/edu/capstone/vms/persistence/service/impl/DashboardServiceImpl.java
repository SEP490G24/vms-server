package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.persistence.dto.dashboard.MultiLineResponse;
import fpt.edu.capstone.vms.persistence.repository.DashboardRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IDashboardService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    final DashboardRepository dashboardRepository;
    final SiteRepository siteRepository;
    final List<String> allPurposes = Arrays.asList("CONFERENCES", "INTERVIEW", "MEETING", "OTHERS", "WORKING");
    final List<Constants.StatusTicket> ticketStatus = List.of(Constants.StatusTicket.COMPLETE, Constants.StatusTicket.CANCEL);
    final List<Constants.StatusTicket> visitsStatus = List.of(Constants.StatusTicket.REJECT, Constants.StatusTicket.CHECK_IN, Constants.StatusTicket.CHECK_OUT);
    final List<String> ticketStatusStrings = ticketStatus.stream()
        .map(Constants.StatusTicket::name)
        .collect(Collectors.toList());
    final List<String> visitsStatusStrings = Arrays.asList("REJECT", "APPROVE");


    @Override
    public List<IDashboardController.PurposePieResponse> countTicketsByPurposeWithPie(IDashboardController.DashboardDTO dashboardDTO) {

        LocalDateTime firstDay, lastDay;

        if (dashboardDTO.getYear() != null) {
            if (dashboardDTO.getMonth() != null) {
                YearMonth yearMonth = YearMonth.of(dashboardDTO.getYear(), dashboardDTO.getMonth());
                firstDay = yearMonth.atDay(1).atStartOfDay();
                lastDay = yearMonth.atEndOfMonth().atStartOfDay();
            } else {
                Year year = Year.of(dashboardDTO.getYear());
                firstDay = year.atDay(1).atStartOfDay();
                lastDay = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay();
            }
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            firstDay = currentYearMonth.atDay(1).atStartOfDay();
            lastDay = currentYearMonth.atEndOfMonth().atStartOfDay();
        }

        List<String> sites = SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites());
        return mapToPurposePieResponse(dashboardRepository.countTicketsByPurposeWithPie(firstDay, lastDay, sites));
    }

    @Override
    public List<MultiLineResponse> countTicketsByPurposeByWithMultiLine(IDashboardController.DashboardDTO dashboardDTO) {

        if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() == null) {
            Year year = Year.of(dashboardDTO.getYear());
            LocalDateTime firstDayOfYear = year.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfYear = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay();
            return MultiLineResponse.formatDataWithMonthInYear(convertToMonthlyTicketStats(dashboardRepository.countTicketsByPurposeWithMultiLine(firstDayOfYear, lastDayOfYear, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), allPurposes);
        } else if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() != null) {
            Integer yearFromDTO = dashboardDTO.getYear();
            Integer monthFromDTO = dashboardDTO.getMonth();
            YearMonth yearMonth = YearMonth.of(yearFromDTO, monthFromDTO);
            LocalDateTime firstDayOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = yearMonth.atEndOfMonth().atStartOfDay();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByPurposeWithMultiLine(firstDayOfMonth, lastDayOfMonth, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), dashboardDTO.getYear(), dashboardDTO.getMonth(), allPurposes);
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            LocalDateTime firstDayOfMonth = currentYearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = currentYearMonth.atEndOfMonth().atStartOfDay();
            int currentYear = currentYearMonth.getYear();
            int currentMonth = currentYearMonth.getMonthValue();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByPurposeWithMultiLine(firstDayOfMonth, lastDayOfMonth, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), currentYear, currentMonth, allPurposes);
        }
    }

    @Override
    public IDashboardController.TotalTicketResponse countTicketsByStatus(IDashboardController.DashboardDTO dashboardDTO) {
        LocalDateTime firstDay, lastDay;

        if (dashboardDTO.getYear() != null) {
            if (dashboardDTO.getMonth() != null) {
                YearMonth yearMonth = YearMonth.of(dashboardDTO.getYear(), dashboardDTO.getMonth());
                firstDay = yearMonth.atDay(1).atStartOfDay();
                lastDay = yearMonth.atEndOfMonth().atStartOfDay();
            } else {
                Year year = Year.of(dashboardDTO.getYear());
                firstDay = year.atDay(1).atStartOfDay();
                lastDay = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay();
            }
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            firstDay = currentYearMonth.atDay(1).atStartOfDay();
            lastDay = currentYearMonth.atEndOfMonth().atStartOfDay();
        }

        List<String> sites = SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites());

        int totalTicket = dashboardRepository.countTotalTickets(null, null, null, sites);
        int totalTicketWithCondition = dashboardRepository.countTotalTickets(firstDay, lastDay, null, sites);
        int totalCompletedTicket = dashboardRepository.countTotalTickets(null, null, List.of(Constants.StatusTicket.COMPLETE), sites);
        int totalCompletedTicketWithCondition = dashboardRepository.countTotalTickets(firstDay, lastDay, List.of(Constants.StatusTicket.COMPLETE), sites);
        int totalCancelTicket = dashboardRepository.countTotalTickets(null, null, List.of(Constants.StatusTicket.CANCEL), sites);
        int totalCancelTicketWithCondition = dashboardRepository.countTotalTickets(firstDay, lastDay, List.of(Constants.StatusTicket.CANCEL), sites);

        return IDashboardController.TotalTicketResponse.builder()
            .totalTicket(totalTicket)
            .totalTicketWithCondition(totalTicketWithCondition)
            .totalCompletedTicket(totalCompletedTicket)
            .totalCompletedTicketWithCondition(totalCompletedTicketWithCondition)
            .totalCancelTicket(totalCancelTicket)
            .totalCancelTicketWithCondition(totalCancelTicketWithCondition)
            .build();
    }

    @Override
    public IDashboardController.TotalVisitsResponse countVisitsByStatus(IDashboardController.DashboardDTO dashboardDTO) {
        LocalDateTime firstDay, lastDay;

        if (dashboardDTO.getYear() != null) {
            if (dashboardDTO.getMonth() != null) {
                YearMonth yearMonth = YearMonth.of(dashboardDTO.getYear(), dashboardDTO.getMonth());
                firstDay = yearMonth.atDay(1).atStartOfDay();
                lastDay = yearMonth.atEndOfMonth().atStartOfDay();
            } else {
                Year year = Year.of(dashboardDTO.getYear());
                firstDay = year.atDay(1).atStartOfDay();
                lastDay = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay();
            }
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            firstDay = currentYearMonth.atDay(1).atStartOfDay();
            lastDay = currentYearMonth.atEndOfMonth().atStartOfDay();
        }

        List<String> sites = SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites());

        int totalVisits = dashboardRepository.countTotalVisits(null, null, List.of(Constants.StatusTicket.CHECK_IN, Constants.StatusTicket.CHECK_OUT, Constants.StatusTicket.REJECT), sites);
        int totalVisitsWithCondition = dashboardRepository.countTotalVisits(firstDay, lastDay, List.of(Constants.StatusTicket.CHECK_IN, Constants.StatusTicket.CHECK_OUT, Constants.StatusTicket.REJECT), sites);
        int totalAcceptanceVisits = dashboardRepository.countTotalVisits(null, null, List.of(Constants.StatusTicket.CHECK_IN, Constants.StatusTicket.CHECK_OUT), sites);
        int totalAcceptanceVisitsWithCondition = dashboardRepository.countTotalVisits(firstDay, lastDay, List.of(Constants.StatusTicket.CHECK_IN, Constants.StatusTicket.CHECK_OUT), sites);
        int totalRejectVisits = dashboardRepository.countTotalVisits(null, null, List.of(Constants.StatusTicket.REJECT), sites);
        int totalRejectVisitsWithCondition = dashboardRepository.countTotalVisits(firstDay, lastDay, List.of(Constants.StatusTicket.REJECT), sites);

        return IDashboardController.TotalVisitsResponse.builder()
            .totalVisits(totalVisits)
            .totalVisitsWithCondition(totalVisitsWithCondition)
            .totalAcceptanceVisits(totalAcceptanceVisits)
            .totalAcceptanceVisitsWithCondition(totalAcceptanceVisitsWithCondition)
            .totalRejectVisits(totalRejectVisits)
            .totalRejectVisitsWithCondition(totalRejectVisitsWithCondition)
            .build();
    }

    @Override
    public List<MultiLineResponse> countTicketsByStatusWithStackedColumn(IDashboardController.DashboardDTO dashboardDTO) {
        if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() == null) {
            Year year = Year.of(dashboardDTO.getYear());
            LocalDateTime firstDayOfYear = year.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfYear = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay();
            return MultiLineResponse.formatDataWithMonthInYear(convertToMonthlyTicketStats(dashboardRepository.countTicketsByStatusWithStackedColumn(firstDayOfYear, lastDayOfYear, ticketStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), ticketStatusStrings);
        } else if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() != null) {
            Integer yearFromDTO = dashboardDTO.getYear();
            Integer monthFromDTO = dashboardDTO.getMonth();
            YearMonth yearMonth = YearMonth.of(yearFromDTO, monthFromDTO);
            LocalDateTime firstDayOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = yearMonth.atEndOfMonth().atStartOfDay();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByStatusWithStackedColumn(firstDayOfMonth, lastDayOfMonth, ticketStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), dashboardDTO.getYear(), dashboardDTO.getMonth(), ticketStatusStrings);
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            LocalDateTime firstDayOfMonth = currentYearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = currentYearMonth.atEndOfMonth().atStartOfDay();
            int currentYear = currentYearMonth.getYear();
            int currentMonth = currentYearMonth.getMonthValue();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByStatusWithStackedColumn(firstDayOfMonth, lastDayOfMonth, ticketStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), currentYear, currentMonth, ticketStatusStrings);
        }
    }

    @Override
    public List<MultiLineResponse> countVisitsByStatusWithStackedColumn(IDashboardController.DashboardDTO dashboardDTO) {
        if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() == null) {
            Year year = Year.of(dashboardDTO.getYear());
            LocalDateTime firstDayOfYear = year.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfYear = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay();
            return MultiLineResponse.formatDataWithMonthInYear1(convertToMonthlyTicketStats(dashboardRepository.countVisitsByStatusWithStackedColumn(firstDayOfYear, lastDayOfYear, visitsStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), visitsStatusStrings);
        } else if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() != null) {
            Integer yearFromDTO = dashboardDTO.getYear();
            Integer monthFromDTO = dashboardDTO.getMonth();
            YearMonth yearMonth = YearMonth.of(yearFromDTO, monthFromDTO);
            LocalDateTime firstDayOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = yearMonth.atEndOfMonth().atStartOfDay();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByStatusWithStackedColumn(firstDayOfMonth, lastDayOfMonth, visitsStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), dashboardDTO.getYear(), dashboardDTO.getMonth(), visitsStatusStrings);
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            LocalDateTime firstDayOfMonth = currentYearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = currentYearMonth.atEndOfMonth().atStartOfDay();
            int currentYear = currentYearMonth.getYear();
            int currentMonth = currentYearMonth.getMonthValue();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByStatusWithStackedColumn(firstDayOfMonth, lastDayOfMonth, visitsStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSites()))), currentYear, currentMonth, visitsStatusStrings);
        }
    }

    private List<IDashboardController.PurposePieResponse> mapToPurposePieResponse(List<Object[]> result) {
        List<IDashboardController.PurposePieResponse> responseList = new ArrayList<>();

        for (Object[] row : result) {
            Constants.Purpose type = (Constants.Purpose) row[0];
            Long count = (Long) row[1];

            responseList.add(new IDashboardController.PurposePieResponse(type, count.intValue()));
        }

        for (Constants.Purpose purpose : Constants.Purpose.values()) {
            boolean exists = responseList.stream().anyMatch(response -> response.getType() == purpose);
            if (!exists) {
                responseList.add(new IDashboardController.PurposePieResponse(purpose, 0));
            }
        }

        return responseList;
    }

    private List<MultiLineResponse> convertToMonthlyTicketStats(List<Object[]> result) {
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

        List<MultiLineResponse> responseList = new ArrayList<>();
        monthTypeCounts.forEach((formattedMonth, purposeCounts) ->
            purposeCounts.forEach((purpose, count) ->
                responseList.add(new MultiLineResponse(formattedMonth, purpose, count))
            )
        );

        return responseList;
    }
}
