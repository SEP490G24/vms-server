package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IOrganizationController;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.service.impl.OrganizationServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class OrganizationController implements IOrganizationController {
    private final OrganizationServiceImpl organizationService;
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
    public ResponseEntity<?> createOrganization(createOrganizationInfo organizationInfo) {
        var organization = organizationService.save(mapper.map(organizationInfo, Organization.class));
        return ResponseEntity.ok(organization);
    }
}
