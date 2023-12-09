package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.controller.IDashboardController;
import fpt.edu.capstone.vms.controller.ITicketController;
import fpt.edu.capstone.vms.persistence.dto.dashboard.MultiLineResponse;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerTicketMapRepository;
import fpt.edu.capstone.vms.persistence.repository.DashboardRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IDashboardService;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
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
    final CustomerTicketMapRepository customerTicketMapRepository;
    final CustomerRepository customerRepository;
    final ModelMapper mapper;
    final List<String> allPurposes = Arrays.asList("CONFERENCES", "INTERVIEW", "MEETING", "OTHERS", "WORKING");
    final List<Constants.StatusTicket> ticketStatus = List.of(Constants.StatusTicket.COMPLETE, Constants.StatusTicket.CANCEL);
    final List<Constants.StatusCustomerTicket> visitsStatus = List.of(Constants.StatusCustomerTicket.REJECT, Constants.StatusCustomerTicket.CHECK_IN, Constants.StatusCustomerTicket.CHECK_OUT);
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
                lastDay = yearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            } else {
                Year year = Year.of(dashboardDTO.getYear());
                firstDay = year.atDay(1).atStartOfDay();
                lastDay = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            }
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            firstDay = currentYearMonth.atDay(1).atStartOfDay();
            lastDay = currentYearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
        }
        List<String> sites = SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId());
        int totalTicket = dashboardRepository.countTotalTickets(firstDay, lastDay, null, sites);
        return mapToPurposePieResponse(dashboardRepository.countTicketsByPurposeWithPie(firstDay, lastDay, sites),totalTicket);
    }

    @Override
    public List<MultiLineResponse> countTicketsByPurposeByWithMultiLine(IDashboardController.DashboardDTO dashboardDTO) {

        if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() == null) {
            Year year = Year.of(dashboardDTO.getYear());
            LocalDateTime firstDayOfYear = year.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfYear = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            return MultiLineResponse.formatDataWithMonthInYear(convertToMonthlyTicketStats(dashboardRepository.countTicketsByPurposeWithMultiLine(firstDayOfYear, lastDayOfYear, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), allPurposes);
        } else if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() != null) {
            Integer yearFromDTO = dashboardDTO.getYear();
            Integer monthFromDTO = dashboardDTO.getMonth();
            YearMonth yearMonth = YearMonth.of(yearFromDTO, monthFromDTO);
            LocalDateTime firstDayOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = yearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByPurposeWithMultiLine(firstDayOfMonth, lastDayOfMonth, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), dashboardDTO.getYear(), dashboardDTO.getMonth(), allPurposes);
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            LocalDateTime firstDayOfMonth = currentYearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = currentYearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            int currentYear = currentYearMonth.getYear();
            int currentMonth = currentYearMonth.getMonthValue();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByPurposeWithMultiLine(firstDayOfMonth, lastDayOfMonth, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), currentYear, currentMonth, allPurposes);
        }
    }

    @Override
    public IDashboardController.TotalTicketResponse countTicketsByStatus(IDashboardController.DashboardDTO dashboardDTO) {
        LocalDateTime firstDay, lastDay;

        if (dashboardDTO.getYear() != null) {
            if (dashboardDTO.getMonth() != null) {
                YearMonth yearMonth = YearMonth.of(dashboardDTO.getYear(), dashboardDTO.getMonth());
                firstDay = yearMonth.atDay(1).atStartOfDay();
                lastDay = yearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            } else {
                Year year = Year.of(dashboardDTO.getYear());
                firstDay = year.atDay(1).atStartOfDay();
                lastDay = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            }
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            firstDay = currentYearMonth.atDay(1).atStartOfDay();
            lastDay = currentYearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
        }

        List<String> sites = SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId());

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
                lastDay = yearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            } else {
                Year year = Year.of(dashboardDTO.getYear());
                firstDay = year.atDay(1).atStartOfDay();
                lastDay = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            }
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            firstDay = currentYearMonth.atDay(1).atStartOfDay();
            lastDay = currentYearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
        }

        List<String> sites = SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId());

        int totalVisits = dashboardRepository.countTotalVisits(null, null, List.of(Constants.StatusCustomerTicket.CHECK_IN, Constants.StatusCustomerTicket.CHECK_OUT, Constants.StatusCustomerTicket.REJECT), sites);
        int totalVisitsWithCondition = dashboardRepository.countTotalVisits(firstDay, lastDay, List.of(Constants.StatusCustomerTicket.CHECK_IN, Constants.StatusCustomerTicket.CHECK_OUT, Constants.StatusCustomerTicket.REJECT), sites);
        int totalAcceptanceVisits = dashboardRepository.countTotalVisits(null, null, List.of(Constants.StatusCustomerTicket.CHECK_IN, Constants.StatusCustomerTicket.CHECK_OUT), sites);
        int totalAcceptanceVisitsWithCondition = dashboardRepository.countTotalVisits(firstDay, lastDay, List.of(Constants.StatusCustomerTicket.CHECK_IN, Constants.StatusCustomerTicket.CHECK_OUT), sites);
        int totalRejectVisits = dashboardRepository.countTotalVisits(null, null, List.of(Constants.StatusCustomerTicket.REJECT), sites);
        int totalRejectVisitsWithCondition = dashboardRepository.countTotalVisits(firstDay, lastDay, List.of(Constants.StatusCustomerTicket.REJECT), sites);

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
            LocalDateTime lastDayOfYear = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            return MultiLineResponse.formatDataWithMonthInYear(convertToMonthlyTicketStats(dashboardRepository.countTicketsByStatusWithStackedColumn(firstDayOfYear, lastDayOfYear, ticketStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), ticketStatusStrings);
        } else if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() != null) {
            Integer yearFromDTO = dashboardDTO.getYear();
            Integer monthFromDTO = dashboardDTO.getMonth();
            YearMonth yearMonth = YearMonth.of(yearFromDTO, monthFromDTO);
            LocalDateTime firstDayOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = yearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByStatusWithStackedColumn(firstDayOfMonth, lastDayOfMonth, ticketStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), dashboardDTO.getYear(), dashboardDTO.getMonth(), ticketStatusStrings);
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            LocalDateTime firstDayOfMonth = currentYearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = currentYearMonth.atEndOfMonth().atStartOfDay();
            lastDayOfMonth.withHour(23).withMinute(59).withSecond(59);
            int currentYear = currentYearMonth.getYear();
            int currentMonth = currentYearMonth.getMonthValue();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countTicketsByStatusWithStackedColumn(firstDayOfMonth, lastDayOfMonth, ticketStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), currentYear, currentMonth, ticketStatusStrings);
        }
    }

    @Override
    public List<MultiLineResponse> countVisitsByStatusWithStackedColumn(IDashboardController.DashboardDTO dashboardDTO) {
        if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() == null) {
            Year year = Year.of(dashboardDTO.getYear());
            LocalDateTime firstDayOfYear = year.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfYear = year.atDay(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            return MultiLineResponse.formatDataWithMonthInYear(convertToMonthlyTicketStats(dashboardRepository.countVisitsByStatusWithStackedColumn(firstDayOfYear, lastDayOfYear, visitsStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), visitsStatusStrings);
        } else if (dashboardDTO.getYear() != null && dashboardDTO.getMonth() != null) {
            Integer yearFromDTO = dashboardDTO.getYear();
            Integer monthFromDTO = dashboardDTO.getMonth();
            YearMonth yearMonth = YearMonth.of(yearFromDTO, monthFromDTO);
            LocalDateTime firstDayOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = yearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countVisitsByStatusWithStackedColumn(firstDayOfMonth, lastDayOfMonth, visitsStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), dashboardDTO.getYear(), dashboardDTO.getMonth(), visitsStatusStrings);
        } else {
            YearMonth currentYearMonth = YearMonth.now();
            LocalDateTime firstDayOfMonth = currentYearMonth.atDay(1).atStartOfDay();
            LocalDateTime lastDayOfMonth = currentYearMonth.atEndOfMonth().atStartOfDay().withHour(23).withMinute(59).withSecond(59);
            int currentYear = currentYearMonth.getYear();
            int currentMonth = currentYearMonth.getMonthValue();
            return MultiLineResponse.formatDataWithWeekInMonth(convertToMonthlyTicketStats(dashboardRepository.countVisitsByStatusWithStackedColumn(firstDayOfMonth, lastDayOfMonth, visitsStatus, SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId()))), currentYear, currentMonth, visitsStatusStrings);
        }
    }

    @Override
    public IDashboardController.TicketsPeriodResponse countTicketsPeriod(IDashboardController.DashboardDTO dashboardDTO) {

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime timeMinus1Hours = currentTime.minus(1, ChronoUnit.HOURS);
        LocalDateTime timePlus1Hour = currentTime.plus(1, ChronoUnit.HOURS);
        List<String> sites = SecurityUtils.getListSiteToString(siteRepository, dashboardDTO.getSiteId());

        List<Ticket> upcomingMeetings = dashboardRepository.getUpcomingMeetings(currentTime,timePlus1Hour,sites);
        List<ITicketController.TicketFilterDTO> upcomingMeetingsDTO = mapper.map(upcomingMeetings, new TypeToken<List<ITicketController.TicketFilterDTO>>() {
        }.getType());
        if(upcomingMeetingsDTO != null){
            upcomingMeetingsDTO.forEach(o -> {
                setCustomer(o);
            });
        }


        List<Ticket> ongoingMeetings = dashboardRepository.getOngoingMeetings(currentTime,sites, Constants.StatusTicket.PENDING);
        List<ITicketController.TicketFilterDTO> ongoingMeetingsDTO = mapper.map(ongoingMeetings, new TypeToken<List<ITicketController.TicketFilterDTO>>() {
        }.getType());
        if(ongoingMeetingsDTO != null){
            ongoingMeetingsDTO.forEach(o -> {
                setCustomer(o);

            });
        }

        List<Ticket> recentlyFinishedMeetings = dashboardRepository.getRecentlyFinishedMeetings(timeMinus1Hours,currentTime,sites, Constants.StatusTicket.COMPLETE);
        List<ITicketController.TicketFilterDTO> recentlyFinishedMeetingsDTO = mapper.map(recentlyFinishedMeetings, new TypeToken<List<ITicketController.TicketFilterDTO>>() {
        }.getType());
        if(recentlyFinishedMeetingsDTO != null){
            recentlyFinishedMeetingsDTO.forEach(o -> {
                setCustomer(o);

            });
        }

        return IDashboardController.TicketsPeriodResponse.builder()
            .upcomingMeetings(upcomingMeetingsDTO)
            .ongoingMeetings(ongoingMeetingsDTO)
            .recentlyFinishedMeetings(recentlyFinishedMeetingsDTO)
            .build();
    }

    private List<IDashboardController.PurposePieResponse> mapToPurposePieResponse(List<Object[]> result, int total) {
        List<IDashboardController.PurposePieResponse> responseList = new ArrayList<>();

        for (Object[] row : result) {
            Constants.Purpose type = (Constants.Purpose) row[0];
            Long count = (Long) row[1];

            double percentage = (double) count / total;
            long roundedPercentage = Math.round(percentage * 100.0);
            responseList.add(new IDashboardController.PurposePieResponse(type, roundedPercentage));
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
    private void setCustomer(ITicketController.TicketFilterDTO ticketFilterDTO) {
        List<ICustomerController.CustomerInfo> customerInfos = new ArrayList<>();
        customerTicketMapRepository.findAllByCustomerTicketMapPk_TicketId(ticketFilterDTO.getId()).forEach(a -> {
            customerInfos.add(mapper.map(customerRepository.findById(a.getCustomerTicketMapPk().getCustomerId()).orElse(null), ICustomerController.CustomerInfo.class));
        });
        ticketFilterDTO.setCustomers(customerInfos);
    }
}
