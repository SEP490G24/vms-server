package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.SettingSiteMap;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface SettingSiteMapRepository extends GenericRepository<SettingSiteMap, SettingSiteMapPk> {

    List<SettingSiteMap> findAllBySettingSiteMapPk_SiteId(UUID siteId);
}
