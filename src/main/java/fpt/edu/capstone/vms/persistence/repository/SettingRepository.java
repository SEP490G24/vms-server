package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Setting;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface SettingRepository extends GenericRepository<Setting, Long> {
    boolean existsByCode(String code);

    List<Setting> findAllByGroupId(Long groupId);

    @Query("SELECT distinct s.groupId FROM Setting as s inner join SettingSiteMap as ss on s.id = ss.settingSiteMapPk.settingId where ss.settingSiteMapPk.siteId = :siteId")
    List<Object[]> findAllDistinctGroupIdBySiteId(UUID siteId);

    Setting findByCode(String code);
}
