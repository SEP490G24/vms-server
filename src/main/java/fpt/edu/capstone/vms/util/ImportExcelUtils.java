package fpt.edu.capstone.vms.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class ImportExcelUtils {

//    private static boolean validateHeaderCellInExcelFile(Row rowHeader, Integer lastColumnIndex, Map<Integer, String> mapHeader) {
//        for (Cell cell : rowHeader) {
//            String cellValue = cell.getStringCellValue().trim();
//            if (cell.getColumnIndex() >= lastColumnIndex) {
//                return true;
//            }
//            if (!mapHeader.get(cell.getColumnIndex()).trim().equalsIgnoreCase(cellValue.trim())) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private static boolean validateEmptyCell(String cellName, String cellValue) {
//        cellValue = cellValue.trim();
//        if (cellValue.isEmpty()) {
//            setCommentAndColorError(cellName + " không đươc bỏ trống");
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    private boolean validateMaxLength(String cellName, String cellValue, int maxlength) {
//        cellValue = cellValue.trim();
//        if (cellValue.length() > maxlength) {
//            setCommentAndColorError("Độ dài của " + cellName + " vượt quá " + maxlength);
//            return false;
//        }
//        return true;
//    }
//
//    private static void setErrorNotExist(String cellName) {
//        setCommentAndColorError(cellName + " không tồn tại");
//    }
//
//    private static Boolean validateBeforeCurrentDate(String dateRequest, String cellName) {
//        Date date = Date.from(Objects.requireNonNull(formatDate(dateRequest)).atStartOfDay(ZoneId.systemDefault()).toInstant());
//        if (date == null) {
//            setCommentAndColorError(cellName + " không đúng định dạng yyyy-MM-dd");
//            return false;
//        }
//        if (date.before(formatDate2(new Date()))) {
//            return true;
//        } else {
//            setCommentAndColorError(cellName + " phải nhỏ hơn hoặc bằng ngày hiện tại");
//            return false;
//        }
//    }
//
//    private static void setCommentAndColorError(String messageIsError) {
//        mapError.computeIfAbsent(currentRowIndex, k -> new ArrayList<>());
//        mapError.get(currentRowIndex).add(messageIsError);
//    }
//
//    private  static LocalDate formatDate(String strDate) {
//        if ("".equals(strDate.trim())) {
//            return null;
//        } else {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            dateFormat.setLenient(false);
//            try {
//                Date date = dateFormat.parse(strDate);
//                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            } catch (ParseException e) {
//                return null;
//            }
//        }
//    }
//
//    private static Date formatDate2(Date date) {
//        if (date == null) {
//            return null;
//        }
//        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//            date = dateFormat.parse(dateFormat.format(date));
//            return date;
//        } catch (ParseException e) {
//            return null;
//        }
//    }
//
//    static boolean checkRegex(String value, String regex, String messageIsError, boolean isRequired) {
//        if (!isRequired && StringUtils.isBlank(value)) {
//            return true;
//        }
//        if (value.matches(regex)) {
//            return true;
//        } else {
//            setCommentAndColorError(messageIsError);
//            return false;
//        }
//    }
}
