package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.repository.OrganizationRepository;
import fpt.edu.capstone.vms.persistence.service.IOrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationServiceImpl implements IOrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    @Override
    public Organization findById(String id) {
        return null;
    }

    @Override
    public Organization save(Organization entity) {
        return null;
    }

    @Override
    public Organization update(Organization entity, String id) {
        return null;
    }

    @Override
    public ResponseEntity<Organization> delete(String id) {
        return null;
    }
}
