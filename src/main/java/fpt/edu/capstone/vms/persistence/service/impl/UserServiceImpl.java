package fpt.edu.capstone.vms.persistence.service.impl;


import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.DepartmentUserMap;
import fpt.edu.capstone.vms.persistence.entity.DepartmentUserMapPk;
import fpt.edu.capstone.vms.persistence.entity.User;
import fpt.edu.capstone.vms.persistence.repository.DepartmentUserMapRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final DepartmentUserMapRepository departmentUserMapRepository;
    private final IUserResource userResource;
    private final ModelMapper mapper;


    @Override
    public Page<User> filter(int pageNumber, List<String> usernames, List<Constants.UserRole> roles, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable,String keyword) {
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
                userRepository.save(userEntity);
                DepartmentUserMapPk departmentUserMapPk = new DepartmentUserMapPk();
                departmentUserMapPk.setDepartmentId(UUID.fromString(userDto.getDepartmentId()));
                departmentUserMapPk.setUsername(userDto.getUsername());
                departmentUserMapRepository.save(new DepartmentUserMap().setDepartmentUserMapPk(departmentUserMapPk));
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
    public User updateUser(IUserResource.UserDto userDto) throws NotFoundException {
        var userEntity = userRepository.findByUsername(userDto.getUsername()).orElse(null);
        if (userEntity == null) throw new NotFoundException();
        if (userResource.update(userDto.setOpenid(userEntity.getOpenid()))) {
            var value = mapper.map(userDto, User.class);
            userEntity = userEntity.update(value);
            userRepository.save(userEntity);
        }
        return userEntity;
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
}
