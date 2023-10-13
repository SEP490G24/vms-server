package fpt.edu.capstone.vms.persistence.service.impl;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.persistence.service.excel.ImportUser;
import fpt.edu.capstone.vms.util.FileUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static fpt.edu.capstone.vms.persistence.entity.User.checkPassword;
import static fpt.edu.capstone.vms.persistence.entity.User.encodePassword;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    @Value("${images.folder}")
    private String imagesFolder;

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final IUserResource userResource;
    private final ModelMapper mapper;
    private final ImportUser importUser;


    @Override
    public Page<User> filter(int pageNumber, List<String> usernames, List<Constants.UserRole> roles, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        return userRepository.filter(
            PageRequest.of(pageNumber, Constants.PAGE_SIZE),
            usernames,
            roles,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword);
    }


    @Override
    public User createUser(IUserResource.UserDto userDto) {
        User userEntity = null;

        // (1) Create user on Keycloak
        String kcUserId = userResource.create(userDto);

        try {
            if (!StringUtils.isEmpty(kcUserId)) {
                userEntity = mapper.map(userDto, User.class).setOpenid(kcUserId);
                userEntity.setPassword(encodePassword(userEntity.getPassword()));
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
            userEntity = userEntity.update(value);
            userRepository.save(userEntity);
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

    @Override
    public void synAccountFromKeycloak() {
        List<IUserResource.UserDto> users = userResource.users();

        for (IUserResource.UserDto userDto : users) {
            if (null != userDto.getRole()) {
                User userEntity = userRepository.findFirstByUsername(userDto.getUsername());
                if (null == userEntity) {
                    userEntity = mapper.map(userDto, User.class);
                    userRepository.save(userEntity);
                    log.info("Create user {}", userDto.getUsername());
                }
            }
        }
    }

    @Override
    public Boolean deleteAvatar(String oldImage, String newImage, String username) {
        var oldFile = fileRepository.findByName(oldImage);
        var newFile = fileRepository.findByName(newImage);
        String filePath = imagesFolder + "/";
        try {
            if (ObjectUtils.isEmpty(newFile)) throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can not found image in file");
            if (!SecurityUtils.loginUsername().equals(username)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User is not true");
            }
            Path rawFile = Paths.get(filePath, oldImage);
            Files.deleteIfExists(rawFile);
            if (!ObjectUtils.isEmpty(oldFile)) {
                fileRepository.delete(oldFile);
                return true;
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public ByteArrayResource export(IUserController.UserFilter userFilter) {
        /*Pageable pageable = PageRequest.of(0, 1000000);
        Page<User> listData = filter(pageable.getPageNumber(),);
        List<CatWarrantyStationDTO> listDto = ObjectMapperUtils.mapAll(listData.getContent(), CatWarrantyStationDTO.class);
        for (CatWarrantyStationDTO e : listDto) {
            if (e.getTypeStation() != null) {
                String typeStationName = 1 == e.getTypeStation() ? "Trạm độc quyền" :
                    2 == e.getTypeStation() ? "Trạm ủy quyền" :
                        "Trạm trực thuộc";
                e.setTypeStationName(typeStationName);
            }
        }

        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream(PATH_FILE));

            JRBeanCollectionDataSource listDataSource = new JRBeanCollectionDataSource(
                listDto.size() == 0 ? Collections.singletonList(new CatWarrantyStationDTO()) : listDto);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("tableDataset", listDataSource);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
            exporter.exportReport();

            byte[] excelBytes = byteArrayOutputStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "danh_sach_tram_bao_hanh_uy_quyen.xlsx");
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(new ByteArrayResource(excelBytes));
        } catch (Exception e){
            return ResponseUtils.getResponseEntityStatus(ErrorApp.INTERNAL_SERVER,null);
        }*/
        return null;
    }

    @Override
    public ResponseEntity<Objects> importUser(MultipartFile file) {
        /*if (!FileUtils.isValidFileUpload(file, "xls", "xlsx", "XLS", "XLSX")) {
            //throw new CustomException(ErrorApp.FILE_NOT_FORMAT);
        }
        if (file.isEmpty()) {
            //throw new CustomException(ErrorApp.FILE_EMPTY);
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Workbook workbook = importUser.importExcel(file);
            if (workbook == null) {
                //return ResponseUtils.getResponseEntityStatus(ErrorApp.SUCCESS, null);
            }

            workbook.write(outputStream);

            // Create a ByteArrayResource for the Excel bytes
            byte[] excelBytes = outputStream.toByteArray();

            ZipSecureFile.setMinInflateRatio(0);
            ByteArrayResource byteData = new ByteArrayResource(excelBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Thong-tin-loi-danh-sach-tram-bao-hanh-uy-quyen.xlsx");
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(byteData);

        } catch (CustomException e) {
            return ResponseUtils.getResponseEntityStatus(e.getErrorApp(), null);
        } catch (Exception e) {
            log.error("Lỗi xảy ra trong quá trình import", e);
            return ResponseUtils.getResponseEntityStatus(ErrorApp.INTERNAL_SERVER, null);
        }*/
        return null;
    }
}
