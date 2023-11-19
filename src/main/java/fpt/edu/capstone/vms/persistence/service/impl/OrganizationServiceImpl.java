package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.service.*;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.*;

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

        if (StringUtils.isEmpty(id.toString())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Id of organization is null");
        }

        if (StringUtils.isEmpty(entity.getCode())) {
            if (organizationRepository.existsByCode(entity.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of organization is exist");
            }
        }

        if (!SecurityUtils.getOrgId().equals(id.toString())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You are not in organization to update");
        }
        if (ObjectUtils.isEmpty(entity)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is empty");
        var organizationEntity = organizationRepository.findById(id).orElse(null);

        var oldOrganization = organizationEntity;
        if (ObjectUtils.isEmpty(organizationEntity))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found organization by id: " + id);

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

        if (StringUtils.isEmpty(entity.getCode()))
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code is null");

        if (organizationRepository.existsByCode(entity.getCode())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of organization is exist");
        }
        if (ObjectUtils.isEmpty(entity)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is empty");
        entity.setEnable(true);

        Organization organization = organizationRepository.save(entity);

        //create role admin for organization
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setCode(organization.getCode().toUpperCase() + "_" + "ADMIN");
        roleDto.setDescription("Role này là role admin của tổ chức " + organization.getName());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("org_id", List.of(organization.getId().toString()));
        attributes.put("name", List.of("ADMIN"));
        roleDto.setAttributes(attributes);
        List<IPermissionResource.PermissionDto> permissionsApi = permissionService.findAllByModuleId("339f9a15-bacf-48dd-acd6-87c482ebb36e");
        List<IPermissionResource.PermissionDto> permissionsScreen = permissionService.findAllByModuleId("75366af1-57bd-4115-b672-b2de7fa40a7d");
        Set<IPermissionResource.PermissionDto> permissionsSet = new HashSet<>();
        permissionsSet.addAll(permissionsApi);
        permissionsSet.addAll(permissionsScreen);
        roleDto.setPermissionDtos(permissionsSet);
        roleService.create(roleDto);


        //Create account admin of organization
        IUserResource.UserDto userDto = new IUserResource.UserDto();
        userDto.setUsername(entity.getCode().toLowerCase() + "_" + "admin");
        userDto.setPassword("123456aA@");
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
        return organizationRepository.filter(
            pageable,
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
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
            keyword);
    }
}
