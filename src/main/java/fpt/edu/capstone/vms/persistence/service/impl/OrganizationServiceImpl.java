package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.service.IFileService;
import fpt.edu.capstone.vms.persistence.service.IOrganizationService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
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

    public OrganizationServiceImpl(OrganizationRepository organizationRepository, IFileService iFileService) {
        this.organizationRepository = organizationRepository;
        this.iFileService = iFileService;
        this.init(organizationRepository);
    }

    @Override
    public Organization update(Organization entity, UUID id) {
        if (!SecurityUtils.getOrgId().equals(id.toString())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You are not in organization to update");
        }
        if (ObjectUtils.isEmpty(entity)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is empty");
        var organizationEntity = organizationRepository.findById(id).orElse(null);

        if (entity.getLogo() != null) {
            iFileService.deleteImage(entity.getLogo());
        }
        if (ObjectUtils.isEmpty(organizationEntity))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found organization by id: " + id);
        return organizationRepository.save(organizationEntity.update(entity));
    }

    @Override
    public Organization save(Organization entity) {
        if (ObjectUtils.isEmpty(entity)) throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Object is empty");
        entity.setEnable(true);
        return organizationRepository.save(entity);
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
