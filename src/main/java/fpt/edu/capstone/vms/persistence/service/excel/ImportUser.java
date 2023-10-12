package fpt.edu.capstone.vms.persistence.service.excel;

import com.monitorjbl.xlsx.StreamingReader;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class ImportUser {
 /*   final DepartmentRepository departmentRepository;
    final SiteRepository siteRepository;
    final UserRepository userRepository;


    Integer currentRowIndex;

    enum UserIndexColumn {
        INDEX(0),
        USERNAME(1),
        FIRST_NAME(2),
        LAST_NAME(3),
        PHONE_NUMBER(4),
        EMAIL(5),
        GENDER(6),
        DEPARTMENT_CODE(7),
        ENABLE(8);
        final int value;

        UserIndexColumn(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    static final Integer LAST_COLUMN_INDEX = 8;
    static final Map<Integer, String> HEADER_EXCEL_FILE = new HashMap<>();

    public static final String STATION_CODE_ALREADY_EXISTS_MESSAGE = "Mã trạm BHUQ đã tồn tại";
    public static final String INVALID_STATION_CODE_FORMAT_MESSAGE = "Mã trạm BHUQ không được chứa ký tự đặc biệt và tiếng việt có dấu";

    public static final String DISTRICT_NOT_BELONG_TO_PROVINCE_MESSAGE = "Quận/huyện không thuộc tỉnh/thành phố";
    public static final String WARD_NOT_BELONG_TO_DISTRICT_MESSAGE = "Phường/xã không thuộc không thuộc quận/huyện";
    public static final String DISTRICT_CODE_NOT_FOUND_MESSAGE = "Mã quận/huyện không tồn tại";
    public static final String WARD_CODE_NOT_FOUND_MESSAGE = "Mã phường/xã không tồn tại";
    public static final String INDUSTRY_CODE_NOT_FOUND_MESSAGE = "Mã ngành hàng không tồn tại";
    public static final String INVALID_PHONE_NUMBER_FORMAT_MESSAGE = "Số điện thoại không đúng định dạng";
    public static final String INVALID_EMAIL_FORMAT_MESSAGE = "Email không đúng định dạng";
    public static final String INVALID_TAX_RATE_FORMAT_MESSAGE = "Thuế suất không đúng định dạng";
    public static final String INVALID_TAX_RATE_MESSAGE = "Thuế suất không được lớn hơn 100";
    public static final String INVALID_INDUSTRY_CODES_FORMAT_MESSAGE = "Mã ngành hàng được nhập cách nhau bởi dấu phẩy";
    public static final String DUPLICATE_INDUSTRY_CODE_MESSAGE = "Mã ngành hàng trong cùng một dòng không được trùng nhau";
    public static final String INVALID_SHIPPING_DAYS_FORMAT_MESSAGE = "Số ngày vận chuyển phải là số nguyên dương";
    public static final String EXCLUSIVE_STATION = "Trạm độc quyền";
    public static final String AUTHORIZED_STATION = "Trạm ủy quyền";
    public static final String DEPENDENT_STATION = "Trạm trực thuộc";

    public static final String PHONE_NUMBER_REGEX = "^(0[2356789]\\d{8})$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String TAX_RATE_REGEX = "^\\d+(\\.\\d{1,2})?$";
    public static final String INDUSTRY_CODES_REGEX = "^[^,]+(,[^,]+)*$";
    public static final String POSITIVE_INTEGER_REGEX = "^[1-9][0-9]*$";
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
        HEADER_EXCEL_FILE.put(7, "DepartmentCode");
        HEADER_EXCEL_FILE.put(8, "Enable");
    }

    @Transactional
    public User saveImportExcel(IUserController.CreateUserInfo dto) {
        try {
            CatWarrantyStationEntity warrantyStationEntity = ObjectMapperUtils.map(dto, CatWarrantyStationEntity.class);
            warrantyStationEntity.setIsDelete(false);
            warrantyStationEntity = warrantyStationAuthorizationRepository.save(warrantyStationEntity);

            List<Integer> industries = dto.getIndustryIds();
            for (Integer industryId : industries) {
                WarrantyStationIndustryEntity warrantyStationIndustryEntity = new WarrantyStationIndustryEntity();
                warrantyStationIndustryEntity.setWarrantyStationId(warrantyStationEntity.getWarrantyStationId());
                warrantyStationIndustryEntity.setIndustryId(industryId);
                warrantyStationIndustryRepositoryJAP.save(warrantyStationIndustryEntity);
            }

            List<Integer> districtHanLingWarranties = dto.getDistrictHanLingWarrantyIds();
            for (Integer regionId : districtHanLingWarranties) {
                DistrictHanLingWarrantyEntity districtHanLingWarrantyEntity = new DistrictHanLingWarrantyEntity();
                districtHanLingWarrantyEntity.setWarrantyStationId(warrantyStationEntity.getWarrantyStationId());
                districtHanLingWarrantyEntity.setRegionId(regionId);
                districtHanLingWarrantyRepositoryJAP.save(districtHanLingWarrantyEntity);
            }
            return warrantyStationEntity;
        } catch (Exception e) {
            log.error("Lỗi khi import trạm bảo hành ủy quyền:{} \nERROR: {}", dto, e);
            throw e;
        }
    }

    public Workbook importExcel(MultipartFile file) {
        try {
            this.mapError = new HashMap<>();

            //read data file
            InputStream inputStreamRead = file.getInputStream();
            Workbook workbookRead = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(inputStreamRead);
            Sheet currentSheetRead = workbookRead.getSheetAt(0);
            Map<Integer, Map<Integer, String>> listRowExcel = new HashMap<>(); //rowIndex - map<cellIndex-value>
            List<String> listWarrantyStationCodeValid = new ArrayList<>();
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
            List<Department> departments = departmentRepository.getAllBySite();
            List<CatIndustryEntity> industryEntities = catIndustryRepositoryJPA.findByIsActiveTrueAndIsDeleteFalse();

            for (Map.Entry<Integer, Map<Integer, String>> entryRow : listRowExcel.entrySet()) {
                this.currentRowIndex = entryRow.getKey();
                Map<Integer, String> dataRowCurrent = entryRow.getValue();
                if (dataRowCurrent.isEmpty()) {
                    //bỏ qua dòng trắng
                    continue;
                }

                CatWarrantyStationDTO dto = new CatWarrantyStationDTO();

                for (Map.Entry<Integer, String> entry : dataRowCurrent.entrySet()) {
                    int cellIndex = entry.getKey();
                    String cellValue = entry.getValue();

                    //Mã trạm
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(0), cellValue, 15) && validateEmptyCell(HEADER_EXCEL_FILE.get(0), cellValue) && checkRegex(cellValue, SPECIAL_CHARACTERS_REGEX, INVALID_STATION_CODE_FORMAT_MESSAGE, true)) {
                            Optional<CatWarrantyStationEntity> result = warrantyStationEntities.stream()
                                .filter(x -> cellValue.equals(x.getCode()))
                                .findFirst();
                            if (result.isEmpty() && !listWarrantyStationCodeValid.contains(cellValue)) {
                                dto.setCode(cellValue);
                            } else {
                                setCommentAndColorError(STATION_CODE_ALREADY_EXISTS_MESSAGE);
                            }
                        }
                        continue;
                    }

                    //Tên trạm
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.NAME.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(1), cellValue, 100) && validateEmptyCell(HEADER_EXCEL_FILE.get(1), cellValue)) {
                            dto.setName(cellValue);
                        }
                        continue;
                    }


                    //Loại trạm
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.TYPE_STATION.getValue()) {
                        Integer typeStation = switch (cellValue) {
                            case EXCLUSIVE_STATION -> 1;
                            case AUTHORIZED_STATION -> 2;
                            case DEPENDENT_STATION -> 3;
                            default -> null;
                        };
                        dto.setTypeStation(typeStation);
                        continue;
                    }


                    //Mã Khu vực
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.REGION_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(3), cellValue, 50) && validateEmptyCell(HEADER_EXCEL_FILE.get(3), cellValue)) {
                            Optional<CatRegionEntity> region = regionEntities.stream()
                                .filter(x -> cellValue.equals(x.getCode()))
                                .findFirst();
                            if (region.isPresent()) {
                                CatRegionEntity entity = region.get();
                                dto.setRegionId(entity.getRegionId());
                            } else {
                                setErrorNotExist(HEADER_EXCEL_FILE.get(3));
                            }
                        }
                        continue;
                    }

                    //Mã tỉnh thành phố
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.PROVINCE_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(4), cellValue, 50) && validateEmptyCell(HEADER_EXCEL_FILE.get(4), cellValue)) {
                            Optional<CatRegionEntity> province = regionEntities.stream()
                                .filter(x -> cellValue.equals(x.getCode()))
                                .findFirst();
                            if (province.isPresent()) {
                                CatRegionEntity entity = province.get();
                                dto.setProvince(entity.getRegionId());
                            } else {
                                setErrorNotExist(HEADER_EXCEL_FILE.get(4));
                            }
                        }
                        continue;
                    }

                    //Mã quận huyện
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.DISTRICTS_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(5), cellValue, 50) && validateEmptyCell(HEADER_EXCEL_FILE.get(5), cellValue)) {
                            if (dto.getProvince() != null) {
                                CatRegionEntity district = validateRegion(cellValue, dto.getProvince(), DISTRICT_NOT_BELONG_TO_PROVINCE_MESSAGE, regionEntities);
                                if (district != null) {
                                    dto.setDistrict(district.getRegionId());
                                }
                            }
                            foundRegion(cellValue, DISTRICT_CODE_NOT_FOUND_MESSAGE, regionEntities);
                        }
                        continue;
                    }

                    //Mã phường/xã
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.WARD_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(6), cellValue, 50) && validateEmptyCell(HEADER_EXCEL_FILE.get(6), cellValue)) {
                            if (dto.getDistrict() != null) {
                                CatRegionEntity ward = validateRegion(cellValue, dto.getDistrict(), WARD_NOT_BELONG_TO_DISTRICT_MESSAGE, regionEntities);
                                if (ward != null) {
                                    dto.setWard(ward.getRegionId());
                                }
                            }
                            foundRegion(cellValue, WARD_CODE_NOT_FOUND_MESSAGE, regionEntities);
                        }
                        continue;
                    }


                    //Địa chỉ
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.ADDRESS.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(7), cellValue, 255) && validateEmptyCell(HEADER_EXCEL_FILE.get(7), cellValue)) {
                            dto.setAddress(cellValue);
                        }
                        continue;
                    }

                    //Người đại diện
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.REPRESENTATIVE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(8), cellValue, 100)) {
                            dto.setRepresentative(cellValue);
                        }
                        continue;
                    }

                    //Số tài khoản ngân hàng
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.BANK_ACCOUNT_NUMBER.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(9), cellValue, 15) && validateNumber(cellValue, false, HEADER_EXCEL_FILE.get(9))) {
                            dto.setBankAccountNumber(cellValue);
                        }
                        continue;
                    }

                    //Ngân hàng chi nhánh
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.BANK_FAMILY.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(10), cellValue, 100)) {
                            dto.setBankFamily(cellValue);
                        }
                        continue;
                    }

                    //SDT cố định
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.LANDLINE_NUMBER.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(11), cellValue, 10) && checkRegex(cellValue, PHONE_NUMBER_REGEX, INVALID_PHONE_NUMBER_FORMAT_MESSAGE, false)) {
                            dto.setLandlineNumber(cellValue);
                        }
                        continue;
                    }

                    //SDT di động
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.PHONE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(12), cellValue, 10) && checkRegex(cellValue, PHONE_NUMBER_REGEX, INVALID_PHONE_NUMBER_FORMAT_MESSAGE, false)) {
                            dto.setPhone(cellValue);
                        }
                        continue;
                    }

                    //Mã số thuế
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.TAX_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(13), cellValue, 15) && validateNumber(cellValue, false, HEADER_EXCEL_FILE.get(13))) {
                            dto.setTaxCode(cellValue);
                        }
                        continue;
                    }

                    //Email
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.EMAIL.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(14), cellValue) && checkRegex(cellValue, EMAIL_REGEX, INVALID_EMAIL_FORMAT_MESSAGE, true)) {
                            dto.setEmail(cellValue);
                        }
                        continue;
                    }

                    //Mã ngành hàng
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.INDUSTRY_CODE.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(15), cellValue) && checkRegex(cellValue, INDUSTRY_CODES_REGEX, INVALID_INDUSTRY_CODES_FORMAT_MESSAGE, true)) {
                            List<String> industryCodes = splitStringByComma(cellValue);
                            List<Integer> industryIds = new ArrayList<>();
                            Set<String> uniqueCodes = new HashSet<>();
                            for (String code : industryCodes) {
                                if (isIndustryCodeValid(code, industryEntities)) {
                                    Optional<CatIndustryEntity> entity = industryEntities.stream()
                                        .filter(x -> code.equals(x.getCode()))
                                        .findFirst();
                                    entity.ifPresent(catIndustryEntity -> {
                                        // Kiểm tra xem mã đã tồn tại trong danh sách chưa
                                        if (uniqueCodes.contains(code)) {
                                            setCommentAndColorError(DUPLICATE_INDUSTRY_CODE_MESSAGE + ":" + code);
                                        } else {
                                            uniqueCodes.add(code); // Thêm mã mới vào danh sách
                                            industryIds.add(catIndustryEntity.getIndustryId());
                                        }
                                    });
                                } else {
                                    setCommentAndColorError(INDUSTRY_CODE_NOT_FOUND_MESSAGE + ":" + code);
                                }
                            }
                            dto.setIndustryIds(industryIds);
                        }
                        continue;
                    }

                    //Mã tỉnh thành phố xử lý ca bảo hành
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.PROVINCE_HAN_LING_WARRANTY_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(16), cellValue, 50) && validateEmptyCell(HEADER_EXCEL_FILE.get(16), cellValue)) {
                            Optional<CatRegionEntity> provinceHandlingWarranty = regionEntities.stream()
                                .filter(x -> cellValue.equals(x.getCode()))
                                .findFirst();
                            if (provinceHandlingWarranty.isPresent()) {
                                CatRegionEntity entity = provinceHandlingWarranty.get();
                                dto.setProvinceHanLingWarranty(entity.getRegionId());
                            } else {
                                setErrorNotExist(HEADER_EXCEL_FILE.get(16));
                            }
                        }
                        continue;
                    }

                    //Mã quận huyện xử lý ca bảo hành
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.DISTRICT_HAN_LING_WARRANTY_CODE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(17), cellValue, 50) && validateEmptyCell(HEADER_EXCEL_FILE.get(17), cellValue)) {
                            List<String> districtHandlingWarrantyCodes = splitStringByComma(cellValue);
                            List<Integer> districtHandlingWarrantyIds = new ArrayList<>();
                            for (String code : districtHandlingWarrantyCodes) {
                                if (dto.getProvinceHanLingWarranty() != null) {
                                    CatRegionEntity districtHandlingWarranty = validateRegion(code, dto.getProvinceHanLingWarranty(), DISTRICT_NOT_BELONG_TO_PROVINCE_MESSAGE + ":" + code, regionEntities);
                                    if (districtHandlingWarranty != null) {
                                        districtHandlingWarrantyIds.add(districtHandlingWarranty.getRegionId());
                                    }
                                }
                                foundRegion(code, DISTRICT_CODE_NOT_FOUND_MESSAGE + ":" + code, regionEntities);
                            }
                            dto.setDistrictHanLingWarrantyIds(districtHandlingWarrantyIds);
                        }
                        continue;
                    }

                    //Số ngày vận chuyển
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.SHIPPING_DAYS.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(18), cellValue) && validateMaxLength(HEADER_EXCEL_FILE.get(18), cellValue, 9) && checkRegex(cellValue, POSITIVE_INTEGER_REGEX, INVALID_SHIPPING_DAYS_FORMAT_MESSAGE, true)) {
                            dto.setShippingDays(Integer.parseInt(cellValue));
                        }
                        continue;
                    }

                    //Thuế suất
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.TAX_RATE.getValue()) {
                        if (validateMaxLength(HEADER_EXCEL_FILE.get(19), cellValue, 10) && validateEmptyCell(HEADER_EXCEL_FILE.get(19), cellValue) && checkRegex(cellValue, TAX_RATE_REGEX, INVALID_TAX_RATE_FORMAT_MESSAGE, true)) {
                            if (Float.parseFloat(cellValue) > 100) {
                                setCommentAndColorError(INVALID_TAX_RATE_MESSAGE);
                            }
                            dto.setTaxRate(Float.parseFloat(cellValue));
                        }
                        continue;
                    }

                    //Hiệu lực thuế suất từ ngày
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.TAX_EFFECT_FROM.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(20), cellValue) && validateEmptyCell(HEADER_EXCEL_FILE.get(20), cellValue) && validateBeforeCurrentDate(cellValue, HEADER_EXCEL_FILE.get(20))) {
                            dto.setTaxEffectFrom(formatDate(cellValue));
                        }
                        continue;
                    }

                    //Phí cố định
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.FIXED_FEES.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(21), cellValue) && validateMaxLength(HEADER_EXCEL_FILE.get(21), cellValue, 10) && validateNumber(cellValue, true, HEADER_EXCEL_FILE.get(21))) {
                            dto.setFixedFees(Float.parseFloat(cellValue));
                        }
                        continue;
                    }

                    //Trạng thái
                    if (cellIndex == WarrantyStationAuthorizationIndexColumn.IS_ACTIVE.getValue()) {
                        if (validateEmptyCell(HEADER_EXCEL_FILE.get(22), cellValue)) {
                            dto.setIsActive(BooleanUtils.toBoolean(Integer.parseInt(cellValue)));
                        }
                    }
                }

                //check error by current row
                if (mapError.get(this.currentRowIndex) == null) {
                    CatWarrantyStationEntity entity = saveImportExcel(dto);
                    listWarrantyStationCodeValid.add(entity.getCode());
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

    private boolean validateNumber(String value, Boolean isRequired, String cellName) {
        if (!isRequired && StringUtils.isBlank(value)) {
            return true;
        }
        if (NumberUtils.isParsable(value)) {
            return true;
        } else {
            setCommentAndColorError(cellName + " phải là số");
            return false;
        }
    }

    private void setErrorNotExist(String cellName) {
        setCommentAndColorError(cellName + " không tồn tại");
    }

    private Boolean validateBeforeCurrentDate(String dateRequest, String cellName) {
        Date date = formatDate(dateRequest);
        if (date == null) {
            setCommentAndColorError(cellName + " không đúng định dạng dd/MM/yyyy");
            return false;
        }
        if (!date.before(formatDate2(new Date()))) {
            return true;
        } else {
            setCommentAndColorError(cellName + " phải lớn hơn hoặc bằng ngày hiện tại");
            return false;
        }
    }

    private void setCommentAndColorError(String messageIsError) {
        mapError.computeIfAbsent(currentRowIndex, k -> new ArrayList<>());
        mapError.get(currentRowIndex).add(messageIsError);
    }

    private Date formatDate(String strDate) {
        if ("".equals(strDate.trim())) {
            return null;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setLenient(false);
            try {
                return dateFormat.parse(strDate);
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

    CatRegionEntity validateRegion(String cellValue, Integer regionParentId, String messageError, List<CatRegionEntity> regionEntities) {
        Optional<CatRegionEntity> result = regionEntities.stream()
            .filter(region -> regionParentId.equals(region.getRegionParentId()))
            .filter(region -> cellValue.equals(region.getCode()))
            .findFirst();
        if (result.isEmpty()) {
            setCommentAndColorError(messageError);
            return null;
        }
        return result.get();
    }

    void foundRegion(String cellValue, String messageError, List<CatRegionEntity> regionEntities) {
        Optional<CatRegionEntity> result = regionEntities.stream()
            .filter(region -> cellValue.equals(region.getCode()))
            .findFirst();
        if (result.isEmpty()) {
            setCommentAndColorError(messageError);
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
            String[] parts = input.split(",");
            for (String part : parts) {
                result.add(part.trim());
            }
        }
        return result;
    }


    boolean isIndustryCodeValid(String code, List<CatIndustryEntity> industryEntities) {
        for (CatIndustryEntity industry : industryEntities) {
            if (industry.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }*/
}
