package fpt.edu.capstone.vms.persistence.repository.impl;

import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.persistence.repository.SettingSiteMapRepositoryCustomer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RequiredArgsConstructor
public class SettingSiteMapRepositoryImpl implements SettingSiteMapRepositoryCustomer {

    final EntityManager entityManager;

    /**
     * This Java function retrieves a list of SettingSiteDTO objects based on the provided siteId and settingGroupId.
     *
     * @param siteId The siteId parameter is a String representing the ID of a site.
     * @param settingGroupId The settingGroupId parameter is an Integer that represents the ID of the setting group. It is
     * used to filter the results and retrieve only the settings that belong to the specified setting group.
     * @return The method is returning a list of objects of type `ISettingSiteMapController.SettingSiteDTO`.
     */
    @Override
    public List<ISettingSiteMapController.SettingSite> findAllBySiteIdAndGroupId(String siteId, Integer settingGroupId) {
        Map<String, Object> queryParams = new HashMap<>();
        String sqlGetData = "SELECT s.setting_id, s.site_id, s.created_by, s.created_on, s.last_updated_by, s.last_updated_on," +
            " s.status, s.property_value, st.setting_group_id, st.\"type\", st.\"name\" as settingName, sg.\"name\" as groupName" +
            ", st.default_property_value, st.code, st.value_list ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM \"setting_site_map\" s ");
        sqlConditional.append("INNER JOIN setting st on st.id = s.setting_id ");
        sqlConditional.append("INNER JOIN setting_group sg on sg.id = st.setting_group_id ");
        sqlConditional.append("WHERE 1=1 ");

        sqlConditional.append("AND s.site_id = :siteId ");
        queryParams.put("siteId",UUID.fromString(siteId));

        sqlConditional.append("AND sg.id = :settingGroupId ");
        queryParams.put("settingGroupId",settingGroupId);

        Query query = entityManager.createNativeQuery(sqlGetData + sqlConditional);
        queryParams.forEach(query::setParameter);
        List<Object[]> queryResult = query.getResultList();
        List<ISettingSiteMapController.SettingSite> listData = new ArrayList<>();
        for (Object[] object : queryResult) {
            ISettingSiteMapController.SettingSite settingSiteDTO = new ISettingSiteMapController.SettingSite();
            settingSiteDTO.setSettingId((Long) object[0]);
            settingSiteDTO.setSiteId((UUID) object[1]);
            settingSiteDTO.setCreatedBy((String) object[2]);
            settingSiteDTO.setCreatedOn((Date) object[3]);
            settingSiteDTO.setLastUpdatedBy((String) object[4]);
            settingSiteDTO.setLastUpdatedOn((Date) object[5]);
            settingSiteDTO.setStatus((Boolean) object[6]);
            settingSiteDTO.setPropertyValue((String) object[7]);
            settingSiteDTO.setSettingGroupId((Long) object[8]);
            settingSiteDTO.setType((String) object[9]);
            settingSiteDTO.setSettingName((String) object[10]);
            settingSiteDTO.setSettingGroupName((String) object[11]);
            settingSiteDTO.setDefaultPropertyValue((String) object[12]);
            settingSiteDTO.setCode((String) object[13]);
            settingSiteDTO.setValueList((String) object[14]);
            listData.add(settingSiteDTO);
        }
        return listData;
    }
}
