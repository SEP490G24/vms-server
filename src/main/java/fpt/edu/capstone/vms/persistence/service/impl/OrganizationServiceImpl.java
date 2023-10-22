package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.oauth2.IUserResource;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.service.IFileService;
import fpt.edu.capstone.vms.persistence.service.IOrganizationService;
import fpt.edu.capstone.vms.persistence.service.IRoleService;
import fpt.edu.capstone.vms.persistence.service.IUserService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationServiceImpl extends GenericServiceImpl<Organization, UUID> implements IOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final IFileService iFileService;
    private final IUserService iUserService;
    private final IRoleService iRoleService;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository, IFileService iFileService, IUserService iUserService, IRoleService iRoleService) {
        this.organizationRepository = organizationRepository;
        this.iFileService = iFileService;
        this.iUserService = iUserService;
        this.iRoleService = iRoleService;
        this.init(organizationRepository);
    }

    @Override
    public Organization update(Organization entity, UUID id) {

        if (!StringUtils.isEmpty(entity.getCode())) {
            if (organizationRepository.existsByCode(entity.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of organization is exist");
            }
        }

        if (!SecurityUtils.getOrgId().equals(id.toString())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You are not in organization to update");
        }
        if (ObjectUtils.isEmpty(entity)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is empty");
        var organizationEntity = organizationRepository.findById(id).orElse(null);

        if (ObjectUtils.isEmpty(organizationEntity))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found organization by id: " + id);

        if (entity.getLogo() != null && !entity.getLogo().equals(organizationEntity.getLogo())) {
               if(iFileService.deleteImage(organizationEntity.getLogo(), entity.getLogo())) {
                    organizationEntity.setLogo(entity.getLogo());
               }
        }
        return organizationRepository.save(organizationEntity.update(entity));
    }

    @Override
    public Organization save(Organization entity) {

        if (StringUtils.isEmpty(entity.getCode())) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code is null");

        if (organizationRepository.existsByCode(entity.getCode())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of organization is exist");
        }
        if (ObjectUtils.isEmpty(entity)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is empty");
        entity.setEnable(true);

        Organization organization = organizationRepository.save(entity);

        //Create account admin of organization
        IUserResource.UserDto userDto = new IUserResource.UserDto();
        userDto.setUsername(entity.getCode().toLowerCase() + "_" + "admin");
        userDto.setPassword("123456aA@");
        userDto.setOrgId(organization.getId().toString());
        userDto.setIsCreateUserOrg(true);
        iUserService.createUser(userDto);

        return organization;
    }

    @Override
    public Page<Organization> filter(int pageNumber, List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return organizationRepository.filter(
            PageRequest.of(pageNumber, Constants.PAGE_SIZE),
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }
}
