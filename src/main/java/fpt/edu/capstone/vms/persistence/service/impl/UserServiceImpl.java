package fpt.edu.capstone.vms.persistence.service.impl;


import com.azure.storage.blob.BlobClient;
import com.monitorjbl.xlsx.StreamingReader;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.*;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.util.FileUtils;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.*;

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
    private final IRoleResource roleResource;


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
        HEADER_EXCEL_FILE.put(8, "Department");
        HEADER_EXCEL_FILE.put(9, "Role");
        HEADER_EXCEL_FILE.put(10, "Status");
    }


    @Override
    public Page<IUserController.UserFilterResponse> filter(Pageable pageable, List<String> usernames
        , String role, LocalDateTime createdOnStart
        , LocalDateTime createdOnEnd, Boolean enable
        , String keyword, List<String> departmentIds, List<String> siteIds, Integer provinceId, Integer districtId, Integer communeId) {
        List<UUID> departments = getListDepartments(siteIds, departmentIds);
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return userRepository.filter(
            pageableSort,
            usernames,
            role,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword,
            departments,
            provinceId,
            districtId,
            communeId);
    }

    @Override
    public List<IUserController.UserFilterResponse> filter(List<String> usernames, String role
        , LocalDateTime createdOnStart, LocalDateTime createdOnEnd
        , Boolean enable, String keyword, List<String> departmentIds, List<String> siteIds, Integer provinceId, Integer districtId, Integer communeId) {
        List<UUID> departments = getListDepartments(siteIds, departmentIds);
        return userRepository.filter(
            usernames,
            role,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword,
            departments,
            provinceId,
            districtId,
            communeId);
    }


    /**
     * The function getListDepartments retrieves a list of department UUIDs based on site and department IDs, with
     * permission checks.
     *
     * @param siteIds       A list of site IDs.
     * @param departmentIds A list of department IDs as strings.
     * @return The method is returning a List of UUIDs.
     */
    List<UUID> getListDepartments(List<String> siteIds, List<String> departmentIds) {

        if (SecurityUtils.getOrgId() == null && siteIds != null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You don't have permission to do this.");
        }
        List<UUID> departments = new ArrayList<>();
        if (SecurityUtils.getOrgId() != null) {
            if (siteIds == null) {
                siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId())).forEach(o -> {
                    addDepartmentToListFilter(departmentIds, departments, o.getId().toString());
                });
            } else {
                siteIds.forEach(o -> {
                    if (!SecurityUtils.checkSiteAuthorization(siteRepository, o)) {
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You don't have permission to do this.");
                    }
                    addDepartmentToListFilter(departmentIds, departments, o);
                });
                if (departments.isEmpty()) {
                    siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId())).forEach(o -> {
                        addDepartmentToListFilter(departmentIds, departments, o.getId().toString());
                    });
                }
            }

        } else {
            addDepartmentToListFilter(departmentIds, departments, SecurityUtils.getSiteId());
        }
        return departments;
    }

    /**
     * The function adds department IDs to a list based on certain conditions and checks for permission before adding.
     *
     * @param departmentIds A list of department IDs as strings.
     * @param departments   A list of UUIDs representing departments.
     * @param siteId        The `siteId` parameter is a String representing the ID of a site.
     */
    private void addDepartmentToListFilter(List<String> departmentIds, List<UUID> departments, String siteId) {
        if (departmentIds == null) {
            var departmentss = departmentRepository.findAllBySiteId(UUID.fromString(siteId));
            if (!departmentss.isEmpty()) {
                departmentss.forEach(a -> {
                    departments.add(a.getId());
                });
            }
        } else {
            departmentIds.forEach(e -> {
                if (!SecurityUtils.checkDepartmentInSite(departmentRepository, e, siteId)) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You don't have permission to do this.");
                }
                departments.add(UUID.fromString(e));
            });
        }
    }


    @Override
    @Transactional
    public User createAdmin(IUserResource.UserDto userDto) {
        User userEntity = null;
        // (1) Create user on Keycloak
        String kcUserId = userResource.create(userDto);

        try {
            if (!StringUtils.isEmpty(kcUserId)) {
                userEntity = mapper.map(userDto, User.class).setOpenid(kcUserId);
                String role = String.join(";", userDto.getRoles());
                userEntity.setRole(role);
                User user = userRepository.save(userEntity);
                auditLogRepository.save(new AuditLog(null
                    , userDto.getOrgId()
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
    public User createUser(IUserResource.UserDto userDto) {
        User userEntity = null;
        Department department = departmentRepository.findById(userDto.getDepartmentId()).orElse(null);

        if (ObjectUtils.isEmpty(department)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "department is null");
        }

        String siteId = department.getSiteId().toString();

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Can't create user in this site");
        }
        ;

        Site site = siteRepository.findById(UUID.fromString(siteId)).orElse(null);
        userDto.setUsername(department.getSite().getCode().toLowerCase() + "_" + userDto.getUsername());
        // (1) Create user on Keycloak
        String kcUserId = userResource.create(userDto);

        try {
            if (!StringUtils.isEmpty(kcUserId)) {
                userEntity = mapper.map(userDto, User.class).setOpenid(kcUserId);
                String role = String.join(";", userDto.getRoles());
                userEntity.setRole(role);
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
            if (userDto.getRoles() != null) {
                String role = String.join(";", userDto.getRoles());
                userEntity.setRole(role);
            }
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
    public void changePasswordUser(String username, String oldPassword, String newPassword) {

        var userEntity = userRepository.findByUsername(username).orElse(null);
        if (userEntity == null) throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found user");
        if (oldPassword == newPassword)
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The new password is the same as the old password");
        if (oldPassword.isEmpty())
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Can not null for new password");

        if (userResource.verifyPassword(username, oldPassword)) {
            userResource.changePassword(userEntity.getOpenid(), newPassword);
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The old password is valid");
        }
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findFirstByUsername(username);
    }

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
        Page<IUserController.UserFilterResponse> listData = filter(pageable, userFilter.getUsernames(), userFilter.getRole(), userFilter.getCreatedOnStart(), userFilter.getCreatedOnEnd(), userFilter.getEnable(), userFilter.getKeyword(), userFilter.getDepartmentId(), userFilter.getSiteId(), userFilter.getProvinceId(), userFilter.getDistrictId(), userFilter.getCommuneId());
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream(PATH_FILE));

            JRBeanCollectionDataSource listDataSource = new JRBeanCollectionDataSource(
                listData.getContent().size() == 0 ? Collections.singletonList(new User()) : listData.getContent());
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("tableDataset", listDataSource);
            parameters.put("exporter", SecurityUtils.loginUsername());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
            exporter.exportReport();

            byte[] excelBytes = byteArrayOutputStream.toByteArray();
            return new ByteArrayResource(excelBytes);
        } catch (Exception e) {
            throw new CustomException(ErrorApp.EXPORT_ERROR);
        }
    }

    @Transactional
    @Override
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
            ClassPathResource resource = new ClassPathResource("template/template-import-users.xlsx");
            byte[] bytes = Files.readAllBytes(resource.getFile().toPath());
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

                    //Department code
                    if (cellIndex == UserIndexColumn.ROLE_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(9), cellValue, 150) && validateEmptyCell(HEADER_EXCEL_FILE.get(9), cellValue)) {
                            List<String> rolesName = splitStringByComma(cellValue);
                            Set<String> uniqueName = new HashSet<>();
                            List<String> duplicateName = new ArrayList();
                            List<String> rolesOfUser = new ArrayList<>();
                            for (String name : rolesName
                            ) {
                                Optional<IRoleResource.RoleDto> roleDto = rolesOfSite.stream()
                                    .filter(x -> name.equalsIgnoreCase(x.getAttributes().get("name").get(0)))
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
