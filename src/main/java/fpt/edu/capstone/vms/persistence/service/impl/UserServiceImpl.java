package fpt.edu.capstone.vms.persistence.service.impl;


import com.azure.storage.blob.BlobClient;
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
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.FileRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FileServiceImpl fileService;
    private final IUserResource userResource;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;
    final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final IRoleResource roleResource;

    private static final String USER_TABLE_NAME = "User";


    @Override
    public Page<IUserController.UserFilterResponse> filter(Pageable pageable, List<String> usernames
        , String role, LocalDateTime createdOnStart
        , LocalDateTime createdOnEnd, Boolean enable
        , String keyword, List<String> departmentIds, List<String> siteIds, Integer provinceId, Integer districtId, Integer communeId) {
        List<UUID> departments = getListDepartments(siteIds, departmentIds);
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
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

        if (SecurityUtils.getOrgId() == null && siteIds != null && !siteIds.isEmpty()) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
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
                        throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
                    }
                    addDepartmentToListFilter(departmentIds, departments, o);
                });
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
        if (departmentIds == null || departmentIds.isEmpty()) {
            var _departments = departmentRepository.findAllBySiteId(UUID.fromString(siteId));
            if (!_departments.isEmpty()) {
                _departments.forEach(a -> {
                    departments.add(a.getId());
                });
            }
        } else {
            departmentIds.forEach(e -> {
                if (!SecurityUtils.checkDepartmentInSite(departmentRepository, e, siteId)) {
                    throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
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
            throw new CustomException(ErrorApp.DEVICE_NOT_FOUND);
        }

        String siteId = department.getSiteId().toString();

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
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
                userEntity.setEnable(true);
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
            Site site = null;
            if (!SecurityUtils.getUserDetails().isOrganizationAdmin() && !SecurityUtils.getUserDetails().isRealmAdmin()) {
                site = siteRepository.findById(userEntity.getDepartment().getSiteId()).orElse(null);
            }


            User oldValue = userEntity;
            userEntity = userEntity.update(value);
            if (userDto.getRoles() != null) {
                String role = String.join(";", userDto.getRoles());
                userEntity.setRole(role);
            }
            userRepository.save(userEntity);

            auditLogRepository.save(new AuditLog(site != null ? site.getId().toString() : null
                , site != null ? site.getOrganizationId().toString() : SecurityUtils.getOrgId()
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
        if (userEntity == null) throw new CustomException(ErrorApp.USER_NOT_FOUND);
        if (oldPassword == newPassword)
            throw new CustomException(ErrorApp.USER_NEW_PASSWORD_SAME_OLD_PASSWORD);
        if (oldPassword.isEmpty())
            throw new CustomException(ErrorApp.USER_NEW_PASSWORD_NOT_NULL);

        if (userResource.verifyPassword(username, oldPassword)) {
            userResource.changePassword(userEntity.getOpenid(), newPassword);
        } else {
            throw new CustomException(ErrorApp.USER_OLD_PASSWORD_NOT_CORRECT);
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
            throw new CustomException(ErrorApp.USER_CAN_NOT_FOUND_FILE_IMAGE);
        if (!SecurityUtils.loginUsername().equals(username)) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        if (oldImage != null) {
            BlobClient blobClient = fileService.getBlobClient(oldImage);
            blobClient.deleteIfExists();
        }
        if (!ObjectUtils.isEmpty(oldFile)) {
            fileRepository.delete(oldFile);
            return true;
        }
        return true;
    }

}
