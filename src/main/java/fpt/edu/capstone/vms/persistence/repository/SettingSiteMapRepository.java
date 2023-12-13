package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface SettingSiteMapRepository extends GenericRepository<SettingSiteMap, SettingSiteMapPk>, SettingSiteMapRepositoryCustomer {

    List<SettingSiteMap> findAllBySettingSiteMapPk_SiteId(UUID siteId);

    SettingSiteMap findBySettingSiteMapPk_SiteIdAndSettingSiteMapPk_SettingId(UUID siteId, Long settingId);

    SettingSiteMap findByValue(String value);
}
