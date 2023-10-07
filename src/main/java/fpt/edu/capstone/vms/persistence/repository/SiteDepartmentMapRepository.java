package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMap;
import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMapPk;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SiteDepartmentMapRepository extends GenericRepository<SiteDepartmentMap, SiteDepartmentMapPk> {
}
