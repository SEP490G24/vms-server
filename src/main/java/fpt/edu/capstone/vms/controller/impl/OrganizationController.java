package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IOrganizationController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.service.IOrganizationService;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class OrganizationController implements IOrganizationController {
    private final IOrganizationService organizationService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<Organization> findById(UUID id) {
        return ResponseEntity.ok(organizationService.findById(id));
    }

    @Override
    public ResponseEntity<Organization> delete(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(organizationService.findAll());
    }

    @Override
    public ResponseEntity<?> createOrganization(CreateOrganizationInfo organizationInfo) {
        try {
            var organization = organizationService.save(mapper.map(organizationInfo, Organization.class));
            return ResponseEntity.ok(organization);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> updateOrganization(UpdateOrganizationInfo updateOrganizationInfo, UUID id) {
        try {
            var organization = organizationService.update(mapper.map(updateOrganizationInfo, Organization.class), id);
            return ResponseEntity.ok(organization);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> filter(OrganizationFilter filter, @QueryParam("isPageable") boolean isPageable, Pageable pageable) {
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
