package fpt.edu.capstone.vms.persistence.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Data
@AllArgsConstructor
public class MultiLineResponse {

    private String time;
    private String type;
    private int value;

    public static List<MultiLineResponse> formatDataWithMonthInYear(List<MultiLineResponse> dailyCounts, List<String> allPurposes) {
        List<MultiLineResponse> monthlyCounts = new ArrayList<>();
        dailyCounts = mergeCheckInAndCheckOut(dailyCounts);
        List<String> allMonths = getAllMonthsInYear();

        for (String month : allMonths) {
            for (String purpose : allPurposes) {
                int count = dailyCounts.stream()
                    .filter(dailyCount -> month.equals(dailyCount.getTime().substring(5, 7)) && purpose.equals(dailyCount.getType()))
                    .mapToInt(MultiLineResponse::getValue)
                    .sum();

                monthlyCounts.add(new MultiLineResponse(month, purpose, count));
            }
        }

        return monthlyCounts;
    }


    private static List<MultiLineResponse> mergeCheckInAndCheckOut(List<MultiLineResponse> dailyCounts) {
        Map<String, Integer> mergedCounts = new HashMap<>();

        List<MultiLineResponse> result = new ArrayList<>();

        for (MultiLineResponse record : dailyCounts) {
            String day = record.getTime();
            String status = record.getType();
            int value = record.getValue();

            if (status.equals("CHECK_IN") || status.equals("CHECK_OUT")) {
                // Gộp thành trạng thái APPROVE
                mergedCounts.merge(day, value, Integer::sum);
            } else {
                // Giữ nguyên các trạng thái khác và thêm vào danh sách kết quả
                result.add(new MultiLineResponse(day, status, value));
            }
        }

        // Thêm các phần tử có status là "APPROVE"
        for (Map.Entry<String, Integer> entry : mergedCounts.entrySet()) {
            result.add(new MultiLineResponse(entry.getKey(), "APPROVE", entry.getValue()));
        }

        return result;
    }




    private static List<String> getAllMonthsInYear() {
        List<String> allMonths = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i); // Đổi năm tùy theo nhu cầu
            allMonths.add(month);
        }
        return allMonths;
    }


    public static List<MultiLineResponse> formatDataWithWeekInMonth(List<MultiLineResponse> dailyCounts, int year, int month, List<String> allPurposes) {
        List<MultiLineResponse> weeklyCounts = new ArrayList<>();
        dailyCounts = mergeCheckInAndCheckOut(dailyCounts);
        List<String> allWeeks = getAllIntervalsInMonth(year, month);

        for (String week : allWeeks) {
            for (String purpose : allPurposes) {
                weeklyCounts.add(new MultiLineResponse(week, purpose, 0));
            }
        }

        // Nếu dailyCounts không rỗng, thì đổ dữ liệu từ dailyCounts vào weeklyCounts
        if (!dailyCounts.isEmpty()) {
            for (String week : allWeeks) {
                for (String purpose : allPurposes) {
                    int count = dailyCounts.stream()
                        .filter(dailyCount -> isDateInWeek(dailyCount.getTime(), week) && purpose.equals(dailyCount.getType()))
                        .mapToInt(MultiLineResponse::getValue)
                        .sum();

                    // Tìm phần tử tương ứng trong weeklyCounts để cập nhật số lượng
                    weeklyCounts.stream()
                        .filter(response -> week.equals(response.getTime()) && purpose.equals(response.getType()))
                        .findFirst().ifPresent(matchingResponse -> matchingResponse.setValue(count));

                }
            }
        }
        convertDateFormat(weeklyCounts);
        return weeklyCounts;
    }

    private static List<String> getAllIntervalsInMonth(int year, int month) {
        List<String> allIntervals = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

        int daysInMonth = yearMonth.lengthOfMonth();

        // Khoảng thời gian giữa các khoảng (3 khoảng đầu cách nhau 7 ngày)
        int intervalGap = 7;

        // Tính số ngày của khoảng thứ 4 (ngày 25 đến hết tháng)
        int daysInFourthInterval = daysInMonth - (3 * intervalGap);

        // Bắt đầu từ ngày đầu tháng
        LocalDate startDate = firstDayOfMonth;

        // Tạo 3 khoảng đầu tiên
        for (int i = 0; i < 3; i++) {
            LocalDate endDate = startDate.plusDays(intervalGap);
            endDate = endDate.isAfter(lastDayOfMonth) ? lastDayOfMonth : endDate;
            allIntervals.add(formatInterval(startDate, endDate));
            startDate = endDate.plusDays(1);
        }

        // Tạo khoảng thứ 4 từ ngày 25 đến hết tháng
        LocalDate day25 = firstDayOfMonth.withDayOfMonth(25);
        if (!day25.isAfter(lastDayOfMonth)) {
            allIntervals.add(formatInterval(day25, lastDayOfMonth));
        }

        return allIntervals;
    }

    private static String formatInterval(LocalDate startDate, LocalDate endDate) {
        return String.format("%s -> %s",
            startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private static boolean isDateInWeek(String date, String week) {
        // Tìm vị trí của dấu "->"
        int arrowIndex = week.indexOf("->");

        // Nếu không tìm thấy dấu "->" hoặc vị trí không hợp lệ, trả về false
        if (arrowIndex == -1 || arrowIndex + 3 >= week.length()) {
            return false;
        }

        // Lấy phần của ngày và tháng từ chuỗi
        String startDateString = week.substring(0, arrowIndex).trim();
        String endDateString = week.substring(arrowIndex + 3).trim();

        LocalDate startDate, endDate, currentDate;

        try {
            startDate = LocalDate.parse(startDateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            endDate = LocalDate.parse(endDateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            currentDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Kiểm tra xem ngày có nằm trong khoảng thời gian của tuần không
            return !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void convertDateFormat(List<MultiLineResponse> data) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM");

        for (MultiLineResponse item : data) {
            String[] dateRange = item.getTime().split(" -> ");
            try {
                Date startDate = inputFormat.parse(dateRange[0]);
                Date endDate = inputFormat.parse(dateRange[1]);

                String formattedStartDate = outputFormat.format(startDate);
                String formattedEndDate = outputFormat.format(endDate);

                String formattedDateRange = formattedStartDate + " -> " + formattedEndDate;
                item.setTime(formattedDateRange);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

}
