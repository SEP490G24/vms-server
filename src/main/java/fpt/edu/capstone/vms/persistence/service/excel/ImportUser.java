package fpt.edu.capstone.vms.persistence.service.excel;

import com.monitorjbl.xlsx.StreamingReader;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.impl.UserServiceImpl;
import fpt.edu.capstone.vms.util.FileUtils;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
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
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;
    private final DepartmentRepository departmentRepository;
    private final IRoleResource roleResource;
    private final UserServiceImpl userService;
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
        ROLE_CODE(9),
        ENABLE(10);
        final int value;

        UserIndexColumn(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    static final Integer LAST_COLUMN_INDEX = 10;
    static final Map<Integer, String> HEADER_EXCEL_FILE = new HashMap<>();

    public static final String USERNAME_ALREADY_EXISTS_MESSAGE = "Username already exist";
    public static final String EMAIL_ALREADY_EXISTS_MESSAGE = "Email already exist";
    public static final String INVALID_STATION_CODE_FORMAT_MESSAGE = "Username must not contain special characters";
    public static final String INVALID_PHONE_NUMBER_FORMAT_MESSAGE = "The phone number is not in the correct format";
    public static final String INVALID_EMAIL_FORMAT_MESSAGE = "Email invalidate";
    public static final String DUPLICATE_ROLE_MESSAGE = "Roles within the same line cannot overlap";
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
        HEADER_EXCEL_FILE.put(8, "Department");
        HEADER_EXCEL_FILE.put(9, "Role");
        HEADER_EXCEL_FILE.put(10, "Status");
    }

    @Transactional
    public ResponseEntity<Object> importUser(String siteId, MultipartFile file) {
        if (!FileUtils.isValidFileUpload(file, "xls", "xlsx", "XLS", "XLSX")) {
            throw new CustomException(ErrorApp.FILE_NOT_FORMAT);
        }
        if (file.isEmpty()) {
            throw new CustomException(ErrorApp.FILE_EMPTY);
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Workbook workbook = importExcel(siteId, file);
            if (workbook == null) {
                return ResponseEntity.ok().build();
            }

            workbook.write(outputStream);

            // Create a ByteArrayResource for the Excel bytes
            byte[] excelBytes = outputStream.toByteArray();

            ZipSecureFile.setMinInflateRatio(0);
            ByteArrayResource byteData = new ByteArrayResource(excelBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "error-import-users.xlsx");
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).headers(headers).body(byteData);

        } catch (CustomException e) {
            log.error("Lỗi xảy ra trong quá trình import", e);
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Lỗi xảy ra trong quá trình import", e);
            return ResponseUtils.getResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ByteArrayResource> downloadExcel() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("template/template-import-users.xlsx");
            byte[] bytes = IOUtils.toByteArray(inputStream);
            ByteArrayResource byteArrayResource = new ByteArrayResource(bytes);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "template-import-users.xlsx");
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(byteArrayResource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @Transactional
    public Workbook importExcel(String siteId, MultipartFile file) {
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
                        throw new CustomException(ErrorApp.IMPORT_HEADER_ERROR);
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
                throw new CustomException(ErrorApp.FILE_EMPTY);
            }


            //get list combobox
            //check siteId
            List<String> sites = new ArrayList<>();
            if (!org.springframework.util.StringUtils.isEmpty(siteId)) {
                sites.add(siteId);
            }
            List<User> users = userRepository.findAllBySiteId(SecurityUtils.getListSiteToUUID(siteRepository, sites).get(0));
            List<Department> departments = departmentRepository.findAllBySiteId(SecurityUtils.getListSiteToUUID(siteRepository, sites).get(0));
            List<IRoleResource.RoleDto> rolesOfSite = roleResource.getBySites(SecurityUtils.getListSiteToString(siteRepository, sites));

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
                                setCommentAndColorError(USERNAME_ALREADY_EXISTS_MESSAGE);
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
                            Optional<User> user = users.stream()
                                .filter(x -> cellValue.equalsIgnoreCase(x.getEmail()))
                                .findFirst();
                            if (user.isEmpty()) {
                                dto.setEmail(cellValue);
                            } else {
                                setCommentAndColorError(EMAIL_ALREADY_EXISTS_MESSAGE);
                            }
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
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(7), cellValue) && validateEmptyCell(HEADER_EXCEL_FILE.get(7), cellValue) && validateBeforeCurrentDate(cellValue, HEADER_EXCEL_FILE.get(7))) {
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

                    //Role code
                    if (cellIndex == UserIndexColumn.ROLE_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(9), cellValue, 150) && validateEmptyCell(HEADER_EXCEL_FILE.get(9), cellValue)) {
                            List<String> rolesName = splitStringByComma(cellValue);
                            Set<String> uniqueName = new HashSet<>();
                            List<String> duplicateName = new ArrayList();
                            List<String> rolesOfUser = new ArrayList<>();
                            for (String name : rolesName
                            ) {
                                Optional<IRoleResource.RoleDto> roleDto = rolesOfSite.stream()
                                    .filter(x -> name.equalsIgnoreCase(x.getCode()))
                                    .findFirst();
                                if (roleDto.isPresent()) {
                                    if (uniqueName.contains(name)) {
                                        if (!duplicateName.contains(name)) {
                                            duplicateName.add(name);
                                        }
                                    } else {
                                        uniqueName.add(name);
                                        rolesOfUser.add(roleDto.get().getCode());
                                    }
                                } else {
                                    setErrorNotExist(HEADER_EXCEL_FILE.get(9));
                                }
                                dto.setRoles(rolesOfUser);
                            }
                            if (!duplicateName.isEmpty()) {
                                setCommentAndColorError(DUPLICATE_ROLE_MESSAGE + ":" + String.join(", ", duplicateName));
                            }

                        }
                        continue;
                    }

                    //Enable
                    if (cellIndex == UserIndexColumn.ENABLE.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(10), cellValue)) {
                            dto.setEnable(BooleanUtils.toBoolean(Integer.parseInt(cellValue)));
                        }
                    }
                    dto.setPassword("123456aA@");
                }

                //check error by current row
                if (mapError.get(this.currentRowIndex) == null) {
                    User entity = userService.createUser(mapper.map(dto, IUserResource.UserDto.class));
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
            workbookSheetWrite.getRow(0).createCell(columnIndexForError).setCellValue("ErrorDescription");
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
            setCommentAndColorError(cellName + " cannot be left blank");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateMaxLength(String cellName, String cellValue, int maxlength) {
        cellValue = cellValue.trim();
        if (cellValue.length() > maxlength) {
            setCommentAndColorError("Length of " + cellName + " exceed " + maxlength);
            return false;
        }
        return true;
    }

    private void setErrorNotExist(String cellName) {
        setCommentAndColorError(cellName + " does not exist");
    }

    private Boolean validateBeforeCurrentDate(String dateRequest, String cellName) {
        LocalDate localDate = formatDate(dateRequest);
        if (localDate == null) {
            setCommentAndColorError(cellName + " Incorrect format yyyy-MM-dd");
            return false;
        }
        if (isDateOfBirthGreaterThanCurrentDate(localDate)) {
            setCommentAndColorError(cellName + " must be less than or equal to the current date");
            return false;
        } else {
            return true;
        }
    }

    public boolean isDateOfBirthGreaterThanCurrentDate(LocalDate date) {
        LocalDate currentDate = LocalDate.now();
        int comparison = date.compareTo(currentDate);
        return comparison > 0;
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

    public static List<String> splitStringByComma(String input) {
        List<String> result = new ArrayList<>();

        if (input != null && !input.isEmpty()) {
            String[] parts = input.split(";");
            for (String part : parts) {
                result.add(part.trim());
            }
        }
        return result;
    }
}
