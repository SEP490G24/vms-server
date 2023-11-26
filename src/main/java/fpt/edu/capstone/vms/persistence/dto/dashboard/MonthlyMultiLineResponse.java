package fpt.edu.capstone.vms.persistence.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class MonthlyMultiLineResponse {

    private String month;
    private String type;
    private int number;

    public static List<MonthlyMultiLineResponse> fillMissingData(List<MonthlyMultiLineResponse> existingData) {
        List<MonthlyMultiLineResponse> resultData = new ArrayList<>();

        // Generate all combinations of months and types
        for (int month = 1; month <= 12; month++) {
            for (String type : getAllTypes(existingData)) {
                MonthlyMultiLineResponse record = getRecord(existingData, month, type);
                if (record == null) {
                    // If the record doesn't exist in the existing data, create it with number 0
                    record = new MonthlyMultiLineResponse(String.format("%02d", month), type, 0);
                }
                resultData.add(record);
            }
        }

        return resultData;
    }

    private static List<String> getAllTypes(List<MonthlyMultiLineResponse> data) {
        List<String> types = new ArrayList<>();
        for (MonthlyMultiLineResponse record : data) {
            if (!types.contains(record.getType())) {
                types.add(record.getType());
            }
        }
        return types;
    }

    private static MonthlyMultiLineResponse getRecord(List<MonthlyMultiLineResponse> data, int month, String type) {
        for (MonthlyMultiLineResponse record : data) {
            if (record.getMonth().equals(String.format("%02d", month)) && record.getType().equals(type)) {
                return record;
            }
        }
        return null;
    }
}
