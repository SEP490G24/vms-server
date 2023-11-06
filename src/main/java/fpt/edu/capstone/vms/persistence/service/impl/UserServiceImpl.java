package fpt.edu.capstone.vms.persistence.service.impl;


import com.azure.storage.blob.BlobClient;
import com.monitorjbl.xlsx.StreamingReader;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.util.FileUtils;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static fpt.edu.capstone.vms.persistence.entity.User.checkPassword;
import static fpt.edu.capstone.vms.persistence.entity.User.encodePassword;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    static final String PATH_FILE = "/jasper/users.jrxml";

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FileServiceImpl fileService;
    private final IUserResource userResource;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;
    final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;


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

    public static final String USERNAME_ALREADY_EXISTS_MESSAGE = "Username already exist";
    public static final String EMAIL_ALREADY_EXISTS_MESSAGE = "Email already exist";
    public static final String INVALID_STATION_CODE_FORMAT_MESSAGE = "Username must not contain special characters";
    public static final String INVALID_PHONE_NUMBER_FORMAT_MESSAGE = "The phone number is not in the correct format";
    public static final String INVALID_EMAIL_FORMAT_MESSAGE = "Email invalidate";
    public static final String MALE = "MALE";
    public static final String FEMALE = "FEMALE";
    public static final String OTHER = "OTHER";

    public static final String PHONE_NUMBER_REGEX = "^(0[2356789]\\d{8})$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String SPECIAL_CHARACTERS_REGEX = "^[a-zA-Z0-9]*$";

    private static final String USER_TABLE_NAME = "User";

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


    @Override
    public Page<IUserController.UserFilterResponse> filter(Pageable pageable, List<String> usernames, String role, LocalDateTime createdOnStart,
                                                           LocalDateTime createdOnEnd, Boolean enable, String keyword, String departmentId, String siteId) {
        return userRepository.filter(
            pageable,
            usernames,
            role,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword,
            departmentId,
            siteId);
    }

    @Override
    public List<IUserController.UserFilterResponse> filter(List<String> usernames, String role, LocalDateTime createdOnStart,
                                                           LocalDateTime createdOnEnd, Boolean enable, String keyword, String departmentId, String siteId) {
        return userRepository.filter(
            usernames,
            role,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword,
            departmentId,
            siteId);
    }


    @Override
    @Transactional
    public User createUser(IUserResource.UserDto userDto) {
        User userEntity = null;
        Department department = departmentRepository.findById(userDto.getDepartmentId()).orElse(null);

        if (ObjectUtils.isEmpty(department)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "department is null");
        }

        String siteId = department.getSiteId().toString();

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Can't create user in this site");
        };

        Site site = siteRepository.findById(UUID.fromString(siteId)).orElse(null);
        userDto.setUsername(department.getSite().getCode().toLowerCase() + "_" + userDto.getUsername());
        userDto.setIsCreateUserOrg(false);
        // (1) Create user on Keycloak
        String kcUserId = userResource.create(userDto);

        try {
            if (!StringUtils.isEmpty(kcUserId)) {
                userEntity = mapper.map(userDto, User.class).setOpenid(kcUserId);
                String role = String.join(";", userDto.getRoles());
                userEntity.setRole(role);
//                userEntity.setPassword(encodePassword(userEntity.getPassword()));
                User user = userRepository.save(userEntity);
                auditLogRepository.save(new AuditLog(siteId
                    , site.getOrganizationId().toString()
                    , user.getId()
                    , USER_TABLE_NAME
                    , Constants.AuditType.CREATE
                    , null
                    , user.toString()));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (null != kcUserId) {
                userResource.delete(kcUserId);
            }
        }
        return userEntity;
    }

    @Override
    @Transactional
    public User updateUser(IUserResource.UserDto userDto) throws NotFoundException {
        var userEntity = userRepository.findByUsername(userDto.getUsername()).orElse(null);
        if (userEntity == null) throw new NotFoundException();
        if (userResource.update(userDto.setOpenid(userEntity.getOpenid()))) {
            var value = mapper.map(userDto, User.class);
            if (value.getAvatar() != null && !value.getAvatar().equals(userEntity.getAvatar())) {
                if (deleteAvatar(userEntity.getAvatar(), userDto.getAvatar(), userDto.getUsername())) {
                    userEntity.setAvatar(value.getAvatar());
                }
            }
            Site site = siteRepository.findById(userEntity.getDepartment().getSiteId()).orElse(null);

            User oldValue = userEntity;
            userEntity = userEntity.update(value);
            String role = String.join(";", userDto.getRoles());
            userEntity.setRole(role);
            userRepository.save(userEntity);
            auditLogRepository.save(new AuditLog(userEntity.getDepartment().getSiteId().toString()
                , site.getOrganizationId().toString()
                , userEntity.getId()
                , USER_TABLE_NAME
                , Constants.AuditType.UPDATE
                , oldValue.toString()
                , userEntity.toString()));
        }
        return userEntity;
    }

    @Override
    @Transactional
    public void changePasswordUser(IUserController.ChangePasswordUserDto userDto) {
        String username = SecurityUtils.loginUsername();
        var userEntity = userRepository.findByUsername(username).orElse(null);
        if (userEntity == null) throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found user");

        if (userDto.getNewPassword().isEmpty())
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Can not null for new password");
        if (checkPassword(userDto.getNewPassword(), userEntity.getPassword()))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Can not be user old password to update new");

        if (checkPassword(userDto.getOldPassword(), userEntity.getPassword())) {
            userEntity.setPassword(encodePassword(userDto.getNewPassword()));
            userResource.changePassword(userEntity.getOpenid(), userDto.getNewPassword());
            userRepository.save(userEntity);
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The old password not match in database");
        }
    }


    @Override
    public int updateState(boolean isEnable, String username) {
        return userRepository.updateStateByUsername(isEnable, username);
    }

    @Override
    public void handleAuthSuccess(String username) {
        var userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            var userEntity = userOptional.get();
            userEntity.setLastLoginTime(LocalDateTime.now());
            userRepository.save(userEntity);
        }
    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findFirstByUsername(username);
    }

//    @Override
//    public void synAccountFromKeycloak() {
//        List<IUserResource.UserDto> users = userResource.users();
//
//        for (IUserResource.UserDto userDto : users) {
//            if (null != userDto.getRole()) {
//                User userEntity = userRepository.findFirstByUsername(userDto.getUsername());
//                if (null == userEntity) {
//                    userEntity = mapper.map(userDto, User.class);
//                    userRepository.save(userEntity);
//                    log.info("Create user {}", userDto.getUsername());
//                }
//            }
//        }
//    }

    @Override
    @Transactional
    public Boolean deleteAvatar(String oldImage, String newImage, String username) {
        var oldFile = fileRepository.findByName(oldImage);
        var newFile = fileRepository.findByName(newImage);
        if (ObjectUtils.isEmpty(newFile))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found image in file");
        if (!SecurityUtils.loginUsername().equals(username)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User is not true");
        }

        BlobClient blobClient = fileService.getBlobClient(oldImage);
        blobClient.deleteIfExists();
        if (!ObjectUtils.isEmpty(oldFile)) {
            fileRepository.delete(oldFile);
            return true;
        }
        return true;
    }

    @Override
    public ByteArrayResource export(IUserController.UserFilterRequest userFilter) {
        Pageable pageable = PageRequest.of(0, 1000000);
        Page<IUserController.UserFilterResponse> listData = filter(pageable, userFilter.getUsernames(), userFilter.getRole(), userFilter.getCreatedOnStart(), userFilter.getCreatedOnEnd(), userFilter.getEnable(), userFilter.getKeyword(), userFilter.getDepartmentId(), userFilter.getSiteId());
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream(PATH_FILE));

            JRBeanCollectionDataSource listDataSource = new JRBeanCollectionDataSource(
                listData.getContent().size() == 0 ? Collections.singletonList(new User()) : listData.getContent());
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("tableDataset", listDataSource);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
            exporter.exportReport();

            byte[] excelBytes = byteArrayOutputStream.toByteArray();
            return new ByteArrayResource(excelBytes);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> importUser(MultipartFile file) {
        if (!FileUtils.isValidFileUpload(file, "xls", "xlsx", "XLS", "XLSX")) {
            throw new CustomException(ErrorApp.FILE_NOT_FORMAT);
        }
        if (file.isEmpty()) {
            throw new CustomException(ErrorApp.FILE_EMPTY);
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Workbook workbook = importExcel(file);
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
            headers.setContentDispositionFormData("attachment", "Thong-tin-loi-danh-sach-nguoi-dung.xlsx");
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(byteData);

        } catch (CustomException e) {
            log.error("Lỗi xảy ra trong quá trình import", e);
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Lỗi xảy ra trong quá trình import", e);
            return ResponseUtils.getResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> downloadExcel() {
        try {
            ClassPathResource resource = new ClassPathResource("template/Mau-danh-sach-nguoi-dung.xlsx");
            byte[] bytes = Files.readAllBytes(resource.getFile().toPath());
            ByteArrayResource byteArrayResource = new ByteArrayResource(bytes);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Mau-danh-sach-nguoi-dung.xlsx");
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(byteArrayResource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

//    @Override
//    public void updateRole(String username, List<String> roles) {
//        var userEntity = userRepository.findByUsername(username).orElse(null);
//        userResource.updateRole(userEntity.getOpenid(), roles);
//    }


    @Transactional
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
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Header is not in correct format");
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
            List<User> users = userRepository.findAllByEnableIsTrue();
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

                    //Enable
                    if (cellIndex == UserIndexColumn.ENABLE.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(9), cellValue)) {
                            dto.setEnable(BooleanUtils.toBoolean(Integer.parseInt(cellValue)));
                        }
                    }
                    dto.setPassword("123456aA@");
                }

                //check error by current row
                if (mapError.get(this.currentRowIndex) == null) {
                    User entity = createUser(mapper.map(dto, IUserResource.UserDto.class));
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
        LocalDate localDate = formatDate(dateRequest);
        if (localDate == null) {
            setCommentAndColorError(cellName + " không đúng định dạng yyyy-MM-dd");
            return false;
        }
        if (isDateOfBirthGreaterThanCurrentDate(localDate)) {
            setCommentAndColorError(cellName + " phải nhỏ hơn hoặc bằng ngày hiện tại");
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
}
