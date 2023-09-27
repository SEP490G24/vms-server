package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Organization;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface OrganizationRepository extends GenericRepository<Organization, UUID>{
}
