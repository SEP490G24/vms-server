package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Setting;
import org.springframework.stereotype.Repository;


@Repository
public interface SettingRepository extends GenericRepository<Setting, Long> {
    boolean existsByCode(String code);

}
