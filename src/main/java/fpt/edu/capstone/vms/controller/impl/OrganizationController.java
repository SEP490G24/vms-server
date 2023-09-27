package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IOrganizationController;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class OrganizationController implements IOrganizationController {
    @Override
    public ResponseEntity<Organization> findById(String id) {
        return null;
    }

    @Override
    public ResponseEntity<Organization> update(Organization entity, String id) {
        return null;
    }

    @Override
    public ResponseEntity<Organization> delete(String id) {
        return null;
    }

    @Override
    public ResponseEntity<List<Organization>> findAll() {
        return null;
    }

    @Override
    public ResponseEntity<Organization> save(Organization entity) {
        return null;
    }
}
