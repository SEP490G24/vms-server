package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IOrganizationController;
import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.IOrganizationService;
import fpt.edu.capstone.vms.persistence.service.impl.OrganizationServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
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

            var site = organizationService.update(mapper.map(updateOrganizationInfo, Organization.class), id);
            return ResponseEntity.ok(site);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> filter(OrganizationFilter filter) {
        return ResponseEntity.ok(
            organizationService.filter(
                filter.getPageNumber(),
                filter.getNames(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getCreateBy(),
                filter.getLastUpdatedBy(),
                filter.getEnable(),
                filter.getKeyword()));
    }
}
