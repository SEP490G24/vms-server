package fpt.edu.capstone.vms.persistence.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MultiLineResponse {

    private String time;
    private String type;
    private int value;

    public static List<MultiLineResponse> formatDataWithMonthInYear(List<MultiLineResponse> dailyCounts, List<String> allPurposes) {
        List<MultiLineResponse> monthlyCounts = new ArrayList<>();

        // Tạo danh sách tất cả các tháng trong năm và mục đích
        List<String> allMonths = getAllMonthsInYear(); // Hàm này cần được triển khai để trả về danh sách tất cả các tháng trong năm

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

    public static List<MultiLineResponse> formatDataWithMonthInYear1(List<MultiLineResponse> dailyCounts, List<String> allPurposes) {
        List<MultiLineResponse> monthlyCounts = new ArrayList<>();
        dailyCounts = mergeCheckInAndCheckOut(dailyCounts);
        // Tạo danh sách tất cả các tháng trong năm và mục đích
        List<String> allMonths = getAllMonthsInYear(); // Hàm này cần được triển khai để trả về danh sách tất cả các tháng trong năm

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

        for (MultiLineResponse record : dailyCounts) {
            String day = record.getTime();
            String status = record.getType();

            if (status.equals("CHECK_IN") || status.equals("CHECK_OUT")) {
                // Gộp thành trạng thái APPROVE
                mergedCounts.merge(day, record.getValue(), Integer::sum);
            } else {
                // Giữ nguyên các trạng thái khác
                mergedCounts.put(day, record.getValue());
            }
        }

        // Chuyển đổi Map thành List<MultiLineResponse>
        return mergedCounts.entrySet().stream()
            .map(entry -> new MultiLineResponse(entry.getKey(), "APPROVE", entry.getValue()))
            .collect(Collectors.toList());
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

        // Tạo danh sách tất cả các tuần trong tháng và mục đích
        List<String> allWeeks = getAllWeeksInMonth(year, month);

        // Khởi tạo weeklyCounts với giá trị mặc định
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
                        .filter(dailyCount -> week.equals(getWeekOfYear(dailyCount.getTime())) && purpose.equals(dailyCount.getType()))
                        .mapToInt(MultiLineResponse::getValue)
                        .sum();

                    // Tìm phần tử tương ứng trong weeklyCounts để cập nhật số lượng
                    weeklyCounts.stream()
                        .filter(response -> week.equals(response.getTime()) && purpose.equals(response.getType()))
                        .findFirst().ifPresent(matchingResponse -> matchingResponse.setValue(count));

                }
            }
        }

        return weeklyCounts;
    }

    private static List<String> getAllWeeksInMonth(int year, int month) {
        List<String> allWeeks = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

        LocalDate startDate = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        while (startDate.isBefore(lastDayOfMonth) || startDate.isEqual(lastDayOfMonth)) {
            LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            endDate = endDate.isAfter(lastDayOfMonth) ? lastDayOfMonth : endDate;
            allWeeks.add(String.format("(%s to %s)", startDate, endDate));
            startDate = endDate.plusDays(1);
        }

        return allWeeks;
    }

    private static String getWeekOfYear(String date) {
        LocalDate startOfWeek = LocalDate.parse(date).with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return String.format("(%s to %s)",
            startOfWeek,
            endOfWeek);
    }
}
