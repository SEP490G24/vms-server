package fpt.edu.capstone.vms.persistence.service.excel;

import com.monitorjbl.xlsx.StreamingReader;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class ImportUser {
    final DepartmentRepository departmentRepository;
    final UserRepository userRepository;
    private final IUserResource userResource;
    final ModelMapper mapper;


    Integer currentRowIndex;

    enum UserIndexColumn {
        INDEX(0),
        USERNAME(1),
        FIRST_NAME(2),
        LAST_NAME(3),
        PHONE_NUMBER(4),
        EMAIL(5),
        GENDER(6),
        DATA_OF_BIRTH(7),
        DEPARTMENT_CODE(8),
        ENABLE(9);
        final int value;

        UserIndexColumn(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    static final Integer LAST_COLUMN_INDEX = 9;
    static final Map<Integer, String> HEADER_EXCEL_FILE = new HashMap<>();

    public static final String STATION_CODE_ALREADY_EXISTS_MESSAGE = "Username đã tồn tại";
    public static final String INVALID_STATION_CODE_FORMAT_MESSAGE = "Mã trạm BHUQ không được chứa ký tự đặc biệt và tiếng việt có dấu";
    public static final String INVALID_PHONE_NUMBER_FORMAT_MESSAGE = "Số điện thoại không đúng định dạng";
    public static final String INVALID_EMAIL_FORMAT_MESSAGE = "Email không đúng định dạng";
    public static final String MALE = "MALE";
    public static final String FEMALE = "FEMALE";
    public static final String OTHER = "OTHER";

    public static final String PHONE_NUMBER_REGEX = "^(0[2356789]\\d{8})$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String SPECIAL_CHARACTERS_REGEX = "^[a-zA-Z0-9]*$";

    Map<Integer, List<String>> mapError;

    static {
        HEADER_EXCEL_FILE.put(0, "Index");
        HEADER_EXCEL_FILE.put(1, "Username");
        HEADER_EXCEL_FILE.put(2, "FirstName");
        HEADER_EXCEL_FILE.put(3, "LastName");
        HEADER_EXCEL_FILE.put(4, "PhoneNumber");
        HEADER_EXCEL_FILE.put(5, "Email");
        HEADER_EXCEL_FILE.put(6, "Gender");
        HEADER_EXCEL_FILE.put(7, "DateOfBirth");
        HEADER_EXCEL_FILE.put(8, "DepartmentCode");
        HEADER_EXCEL_FILE.put(9, "Enable");
    }

    @Transactional
    public User saveImportExcel(IUserController.CreateUserInfo dto) {
        User userEntity = null;
        IUserResource.UserDto userDto = mapper.map(dto, IUserResource.UserDto.class);
        // (1) Create user on Keycloak
        String kcUserId = userResource.create(userDto);

        try {
            if (!StringUtils.isEmpty(kcUserId)) {
                userEntity = mapper.map(userDto, User.class).setOpenid(kcUserId);
                if (userEntity.getDepartmentId() == null)
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "SiteId not null");
                userRepository.save(userEntity);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (null != kcUserId) {
                userResource.delete(kcUserId);
            }
        }
        return userEntity;
    }

    public Workbook importExcel(MultipartFile file) {
        try {
            this.mapError = new HashMap<>();

            //read data file
            InputStream inputStreamRead = file.getInputStream();
            Workbook workbookRead = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(inputStreamRead);
            Sheet currentSheetRead = workbookRead.getSheetAt(0);
            Map<Integer, Map<Integer, String>> listRowExcel = new HashMap<>(); //rowIndex - map<cellIndex-value>
            List<String> listUsernameValid = new ArrayList<>();
            boolean isAllRowBlank = true;
            for (Row row : currentSheetRead) {
                //validate header
                if (row.getRowNum() == 0) {
                    if (validateHeaderCellInExcelFile(row, LAST_COLUMN_INDEX, HEADER_EXCEL_FILE)) {
                        continue;
                    } else {
                        //throw new CustomException(ErrorApp.FILE_HEADER_FORMAT);
                    }
                }

                Map<Integer, String> dataRow = new HashMap<>();
                boolean checkAllBlank = true;
                for (int i = 0; i < LAST_COLUMN_INDEX + 1; i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null) {
                        dataRow.put(i, "");
                    } else {
                        String cellValue = row.getCell(i).getStringCellValue().trim();
                        dataRow.put(cell.getColumnIndex(), cellValue);
                        if (!StringUtils.isBlank(cellValue)) {
                            checkAllBlank = false;
                        }
                    }
                }
                if (!checkAllBlank) {
                    listRowExcel.put(row.getRowNum(), dataRow);
                    isAllRowBlank = false;
                } else {
                    //trường hợp all cell blank
                    listRowExcel.put(row.getRowNum(), new HashMap<>());
                }
            }
            workbookRead.close();

            if (isAllRowBlank) {
                //throw new CustomException(ErrorApp.FILE_EMPTY);
            }


            //get list combobox
            List<User> users = userRepository.findAllByEnableIsTrue();
            String username = SecurityUtils.getUserDetails().getName();
            List<Department> departments = departmentRepository.findAllByEnableIsTrue();

            for (Map.Entry<Integer, Map<Integer, String>> entryRow : listRowExcel.entrySet()) {
                this.currentRowIndex = entryRow.getKey();
                Map<Integer, String> dataRowCurrent = entryRow.getValue();
                if (dataRowCurrent.isEmpty()) {
                    //bỏ qua dòng trắng
                    continue;
                }

                IUserController.CreateUserInfo dto = new IUserController.CreateUserInfo();

                for (Map.Entry<Integer, String> entry : dataRowCurrent.entrySet()) {
                    int cellIndex = entry.getKey();
                    String cellValue = entry.getValue();

                    //Username
                    if (cellIndex == UserIndexColumn.USERNAME.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(1), cellValue, 15) && validateEmptyCell(HEADER_EXCEL_FILE.get(1), cellValue) && checkRegex(cellValue, SPECIAL_CHARACTERS_REGEX, INVALID_STATION_CODE_FORMAT_MESSAGE, true)) {
                            Optional<User> user = users.stream()
                                .filter(x -> cellValue.equalsIgnoreCase(x.getUsername()))
                                .findFirst();
                            if (user.isEmpty() && !listUsernameValid.contains(cellValue)) {
                                dto.setUsername(cellValue);
                            } else {
                                setCommentAndColorError(STATION_CODE_ALREADY_EXISTS_MESSAGE);
                            }
                        }
                        continue;
                    }

                    //First Name
                    if (cellIndex == UserIndexColumn.FIRST_NAME.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(2), cellValue, 100) && validateEmptyCell(HEADER_EXCEL_FILE.get(2), cellValue)) {
                            dto.setFirstName(cellValue);
                        }
                        continue;
                    }

                    //Last Name
                    if (cellIndex == UserIndexColumn.LAST_NAME.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(3), cellValue, 100) && validateEmptyCell(HEADER_EXCEL_FILE.get(3), cellValue)) {
                            dto.setLastName(cellValue);
                        }
                        continue;
                    }

                    //Phone Number
                    if (cellIndex == UserIndexColumn.PHONE_NUMBER.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(4), cellValue, 10) && validateEmptyCell(HEADER_EXCEL_FILE.get(4), cellValue) && checkRegex(cellValue, PHONE_NUMBER_REGEX, INVALID_PHONE_NUMBER_FORMAT_MESSAGE, false)) {
                            dto.setPhoneNumber(cellValue);
                        }
                        continue;
                    }

                    //Email
                    if (cellIndex == UserIndexColumn.EMAIL.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(5), cellValue) && validateEmptyCell(HEADER_EXCEL_FILE.get(5), cellValue) && checkRegex(cellValue, EMAIL_REGEX, INVALID_EMAIL_FORMAT_MESSAGE, true)) {
                            dto.setEmail(cellValue);
                        }
                        continue;
                    }

                    //gender
                    if (cellIndex == UserIndexColumn.GENDER.getValue() && validateEmptyCell(HEADER_EXCEL_FILE.get(5), cellValue)) {
                        Constants.Gender gender = switch (cellValue) {
                            case MALE -> Constants.Gender.MALE;
                            case FEMALE -> Constants.Gender.FEMALE;
                            case OTHER -> Constants.Gender.OTHER;
                            default -> null;
                        };
                        dto.setGender(gender);
                        continue;
                    }

                    //dateOfBirth
                    if (cellIndex == UserIndexColumn.DATA_OF_BIRTH.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(7), cellValue) && validateEmptyCell(HEADER_EXCEL_FILE.get(7), cellValue) && validateBeforeCurrentDate(cellValue, HEADER_EXCEL_FILE.get(20))) {
                            dto.setDateOfBirth(formatDate(cellValue));
                        }
                        continue;
                    }


                    //Department code
                    if (cellIndex == UserIndexColumn.DEPARTMENT_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(8), cellValue, 50) && validateEmptyCell(HEADER_EXCEL_FILE.get(8), cellValue)) {
                            Optional<Department> department = departments.stream()
                                .filter(x -> cellValue.equalsIgnoreCase(x.getCode()))
                                .findFirst();
                            if (department.isPresent()) {
                                dto.setDepartmentId(department.get().getId());
                            } else {
                                setErrorNotExist(HEADER_EXCEL_FILE.get(8));
                            }
                        }
                        continue;
                    }

                    //Enable
                    if (cellIndex == UserIndexColumn.ENABLE.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(9), cellValue)) {
                            dto.setEnable(BooleanUtils.toBoolean(Integer.parseInt(cellValue)));
                        }
                    }
                }

                //check error by current row
                if (mapError.get(this.currentRowIndex) == null) {
                    User entity = saveImportExcel(dto);
                    listUsernameValid.add(entity.getUsername());
                    //delete message error if exist
                    if (!CollectionUtils.isEmpty(this.mapError.get(currentRowIndex))) {
                        mapError.remove(currentRowIndex);
                    }
                }
            }

            //case all row valid
            if (mapError.isEmpty()) {
                //all row validate
                return null;
            }

            //Write file error
            InputStream inputStreamWrite = file.getInputStream();
            Workbook workbookWrite = WorkbookFactory.create(inputStreamWrite);
            Sheet workbookSheetWrite = workbookWrite.getSheetAt(0);

            // Tạo một CellStyle mới
            CellStyle headerCellStyle = workbookWrite.createCellStyle();
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            // Đặt màu nền Light Green
            headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            // Đặt font cho header cột
            Font font = workbookWrite.createFont();
            font.setFontName("Times New Roman");
            font.setBold(true);
            font.setFontHeightInPoints((short) 11);
            headerCellStyle.setFont(font);
            font.setColor(IndexedColors.RED.getIndex());

            //Tạo cell header
            int columnIndexForError = LAST_COLUMN_INDEX + 1;
            workbookSheetWrite.getRow(0).createCell(columnIndexForError).setCellValue("Chi tiết lỗi");
            workbookSheetWrite.getRow(0).getCell(columnIndexForError).setCellStyle(headerCellStyle);
            workbookSheetWrite.setColumnWidth(columnIndexForError, 50 * 256);
            for (Row row : workbookSheetWrite) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                String rowMessageError = "";
                if (mapError.containsKey(row.getRowNum())) {
                    List<String> listError = mapError.get(row.getRowNum());
                    if (!CollectionUtils.isEmpty(listError)) {
                        rowMessageError = String.join("; ", listError);
                    }
                }

                Cell errorCell = workbookSheetWrite.getRow(row.getRowNum()).createCell(columnIndexForError);
                Font errorFont = workbookWrite.createFont();
                errorFont.setColor(IndexedColors.RED.getIndex());
                CellStyle errorCellStyle = workbookWrite.createCellStyle();
                errorCellStyle.setFont(errorFont);
                errorCell.setCellStyle(errorCellStyle);
                errorCell.setCellValue(rowMessageError);
            }
            return workbookWrite;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateHeaderCellInExcelFile(Row rowHeader, Integer lastColumnIndex, Map<Integer, String> mapHeader) {
        for (Cell cell : rowHeader) {
            String cellValue = cell.getStringCellValue().trim();
            if (cell.getColumnIndex() >= lastColumnIndex) {
                return true;
            }
            if (!mapHeader.get(cell.getColumnIndex()).trim().equalsIgnoreCase(cellValue.trim())) {
                return false;
            }
        }
        return true;
    }

    private boolean validateEmptyCell(String cellName, String cellValue) {
        cellValue = cellValue.trim();
        if (cellValue.isEmpty()) {
            setCommentAndColorError(cellName + " không đươc bỏ trống");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateMaxLength(String cellName, String cellValue, int maxlength) {
        cellValue = cellValue.trim();
        if (cellValue.length() > maxlength) {
            setCommentAndColorError("Độ dài của " + cellName + " vượt quá " + maxlength);
            return false;
        }
        return true;
    }

    private void setErrorNotExist(String cellName) {
        setCommentAndColorError(cellName + " không tồn tại");
    }

    private Boolean validateBeforeCurrentDate(String dateRequest, String cellName) {
        Date date = Date.from(Objects.requireNonNull(formatDate(dateRequest)).atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (date == null) {
            setCommentAndColorError(cellName + " không đúng định dạng yyyy-MM-dd");
            return false;
        }
        if (date.before(formatDate2(new Date()))) {
            return true;
        } else {
            setCommentAndColorError(cellName + " phải nhỏ hơn hoặc bằng ngày hiện tại");
            return false;
        }
    }

    private void setCommentAndColorError(String messageIsError) {
        mapError.computeIfAbsent(currentRowIndex, k -> new ArrayList<>());
        mapError.get(currentRowIndex).add(messageIsError);
    }

    private static LocalDate formatDate(String strDate) {
        if ("".equals(strDate.trim())) {
            return null;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);
            try {
                Date date = dateFormat.parse(strDate);
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (ParseException e) {
                return null;
            }
        }
    }

    private Date formatDate2(Date date) {
        if (date == null) {
            return null;
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            date = dateFormat.parse(dateFormat.format(date));
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    boolean checkRegex(String value, String regex, String messageIsError, boolean isRequired) {
        if (!isRequired && StringUtils.isBlank(value)) {
            return true;
        }
        if (value.matches(regex)) {
            return true;
        } else {
            setCommentAndColorError(messageIsError);
            return false;
        }
    }
}
