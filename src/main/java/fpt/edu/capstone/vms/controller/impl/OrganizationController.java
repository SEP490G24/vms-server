package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IOrganizationController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.service.IOrganizationService;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class OrganizationController implements IOrganizationController {
    private final IOrganizationService organizationService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<?> findById(UUID id) {
        if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            return ResponseEntity.ok(organizationService.findById(id));
        } else {
            return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public ResponseEntity<?> viewDetail() {
        if (SecurityUtils.getUserDetails().isRealmAdmin() || SecurityUtils.getUserDetails().isOrganizationAdmin()) {
            return ResponseEntity.ok(organizationService.findById(UUID.fromString(SecurityUtils.getOrgId())));
        } else {
            return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public ResponseEntity<Organization> delete(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            return ResponseEntity.ok(organizationService.findAll());
        } else {
            return null;
        }
    }

    @Override
    public ResponseEntity<?> createOrganization(CreateOrganizationInfo organizationInfo) {
        try {
            if (SecurityUtils.getUserDetails().isRealmAdmin()) {
                var organization = organizationService.save(mapper.map(organizationInfo, Organization.class));
                return ResponseUtils.getResponseEntityStatus(organization);
            } else {
                return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateOrganization(UpdateOrganizationInfo updateOrganizationInfo, UUID id) {
        try {
            var organization = organizationService.update(mapper.map(updateOrganizationInfo, Organization.class), id);
            return ResponseUtils.getResponseEntityStatus(organization);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> filter(OrganizationFilter filter, @QueryParam("isPageable") boolean isPageable, Pageable pageable) {
        if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            return isPageable ? ResponseEntity.ok(
                organizationService.filter(
                    pageable,
                    filter.getNames(),
                    filter.getCreatedOnStart(),
                    filter.getCreatedOnEnd(),
                    filter.getCreateBy(),
                    filter.getLastUpdatedBy(),
                    filter.getEnable(),
                    filter.getKeyword())) : ResponseEntity.ok(
                organizationService.filter(
                    filter.getNames(),
                    filter.getCreatedOnStart(),
                    filter.getCreatedOnEnd(),
                    filter.getCreateBy(),
                    filter.getLastUpdatedBy(),
                    filter.getEnable(),
                    filter.getKeyword()));
        } else {
            return null;
        }
    }

    @Data
    class OrganizationDTO {
        //organization info
        private String name;
        private String code;
        private String website;
        private String representative;
        private String description;
        private String logo;
        private String contactInfo;
        private String contactPhoneNumber;
        private Boolean enable;

        //site info
        private String phoneNumber;
        private Integer provinceId;
        private Integer communeId;
        private Integer districtId;
        private String address;
        private String taxCode;
    }
}
