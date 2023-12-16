package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.service.IFileService;
import fpt.edu.capstone.vms.persistence.service.IOrganizationService;
import fpt.edu.capstone.vms.persistence.service.IPermissionService;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OrganizationServiceImpl extends GenericServiceImpl<Organization, UUID> implements IOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final IFileService iFileService;
    private final IUserService iUserService;
    private final IRoleService roleService;
    private final IPermissionService permissionService;
    private final AuditLogRepository auditLogRepository;
    private static final String ORGANIZATION_TABLE_NAME = "Organization";

    public OrganizationServiceImpl(OrganizationRepository organizationRepository, IFileService iFileService, IUserService iUserService, IRoleService roleService, IPermissionService permissionService, AuditLogRepository auditLogRepository) {
        this.organizationRepository = organizationRepository;
        this.iFileService = iFileService;
        this.iUserService = iUserService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.auditLogRepository = auditLogRepository;
        this.init(organizationRepository);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Organization update(Organization entity, UUID id) {

        if (SecurityUtils.getUserDetails().isOrganizationAdmin()) {
            if (!SecurityUtils.getOrgId().equals(id.toString())) {
                throw new CustomException(ErrorApp.ORGANIZATION_NOT_PERMISSION);
            }
        } else if (!SecurityUtils.getUserDetails().isRealmAdmin()) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }

        if (StringUtils.isEmpty(id.toString())) {
            throw new CustomException(ErrorApp.ORGANIZATION_ID_NULL);
        }

        if (StringUtils.isEmpty(entity.getCode())) {
            if (organizationRepository.existsByCode(entity.getCode())) {
                throw new CustomException(ErrorApp.ORGANIZATION_CODE_EXIST);
            }
        }

        if (ObjectUtils.isEmpty(entity)) throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);
        var organizationEntity = organizationRepository.findById(id).orElse(null);

        var oldOrganization = organizationEntity;
        if (ObjectUtils.isEmpty(organizationEntity))
            throw new CustomException(ErrorApp.ORGANIZATION_NOT_FOUND);

        if (entity.getLogo() != null && !entity.getLogo().equals(organizationEntity.getLogo())) {
            if (iFileService.deleteImage(organizationEntity.getLogo(), entity.getLogo())) {
                organizationEntity.setLogo(entity.getLogo());
            }
        }
        var updateOrganization = organizationRepository.save(organizationEntity.update(entity));
        auditLogRepository.save(new AuditLog(null
            , organizationEntity.getId().toString()
            , organizationEntity.getId().toString()
            , ORGANIZATION_TABLE_NAME
            , Constants.AuditType.UPDATE
            , oldOrganization.toString()
            , updateOrganization.toString()));
        return updateOrganization;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Organization save(Organization entity) {

        if (!SecurityUtils.getUserDetails().isRealmAdmin()) {
            throw new CustomException(ErrorApp.ORGANIZATION_NOT_PERMISSION);
        }

        if (StringUtils.isEmpty(entity.getCode()))
            throw new CustomException(ErrorApp.OBJECT_CODE_NULL);

        if (organizationRepository.existsByCode(entity.getCode())) {
            throw new CustomException(ErrorApp.ORGANIZATION_CODE_EXIST);
        }
        if (ObjectUtils.isEmpty(entity)) throw new CustomException(ErrorApp.OBJECT_NOT_EMPTY);
        entity.setEnable(true);

        Organization organization = organizationRepository.save(entity);

        //create role admin for organization
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setCode(organization.getCode().toUpperCase() + "_" + "ADMIN");
        roleDto.setDescription(organization.getName());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("org_id", List.of(organization.getId().toString()));
        attributes.put("name", List.of("ADMIN"));
        roleDto.setAttributes(attributes);
        List<IPermissionResource.PermissionDto> permissionsApi = permissionService.findAllOrgByModuleId("6652b814-1563-4a75-9824-a8274112ce31");
        List<IPermissionResource.PermissionDto> permissionsScreen = permissionService.findAllOrgByModuleId("e21afc28-1413-4076-a46e-04f23257b85b");
        Set<IPermissionResource.PermissionDto> permissionsSet = new HashSet<>();
        permissionsSet.addAll(permissionsApi);
        permissionsSet.addAll(permissionsScreen);
        roleDto.setPermissionDtos(permissionsSet);
        roleService.create(roleDto);


        //Create account admin of organization
        IUserResource.UserDto userDto = new IUserResource.UserDto();
        userDto.setUsername(entity.getCode().toLowerCase() + "_" + "admin");
        userDto.setPassword("123456aA@");
        userDto.setFirstName(entity.getCode().toLowerCase());
        userDto.setLastName("admin");
        userDto.setEnable(true);
        userDto.setOrgId(organization.getId().toString());
        userDto.setRoles(List.of(organization.getCode().toUpperCase() + "_" + "ADMIN"));
        iUserService.createAdmin(userDto);

        auditLogRepository.save(new AuditLog(null
            , organization.getId().toString()
            , organization.getId().toString()
            , ORGANIZATION_TABLE_NAME
            , Constants.AuditType.CREATE
            , null
            , organization.toString()));
        return organization;
    }

    @Override
    public Page<Organization> filter(Pageable pageable, List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return organizationRepository.filter(
            pageableSort,
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Organization> filter(List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return organizationRepository.filter(
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword != null ? keyword.toUpperCase() : null);
    }
}
