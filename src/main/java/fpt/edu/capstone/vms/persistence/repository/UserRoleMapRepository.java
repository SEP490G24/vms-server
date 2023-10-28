package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.UserRoleMap;
import fpt.edu.capstone.vms.persistence.entity.UserRoleMapPk;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRoleMapRepository extends GenericRepository<UserRoleMap, UserRoleMapPk> {
}
