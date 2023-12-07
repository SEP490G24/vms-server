package fpt.edu.capstone.vms.persistence.repository.impl;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import fpt.edu.capstone.vms.controller.IUserController;
import fpt.edu.capstone.vms.persistence.repository.UserRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    final EntityManager entityManager;

    @Override
    public Page<IUserController.UserFilterResponse> filter(Pageable pageable, Collection<String> usernames
        , String role, LocalDateTime createdOnStart, LocalDateTime createdOnEnd
        , Boolean enable, String keyword, Collection<UUID> departmentIds
        , Integer provinceId, Integer districtId, Integer communeId) {
        Map<String, Object> queryParams = new HashMap<>();
        Sort sort = pageable.getSort();
        String orderByClause = "";
        if (sort.isSorted()) {
            orderByClause = "ORDER BY ";
            for (Sort.Order order : sort) {
                orderByClause += order.getProperty() + " " + order.getDirection() + ", ";
            }
            orderByClause = orderByClause.substring(0, orderByClause.length() - 2);
        }
        String sqlCountAll = "SELECT COUNT(1) ";
        String sqlGetData = "SELECT u.username, u.first_name as firstName, u.last_name as lastName, u.email, u.gender, u.phone_number as phoneNumber," +
            " u.dob as dateOfBirth, u.enable, u.role as roleName, d.name as departmentName, u.country_code as countryCode, u.created_on as createdOn," +
            " u.last_updated_on as lastUpdatedOn, s.id as siteId, s.name as siteName, p.id as provinceId, p.name as provinceName, d2.id as districtId," +
            " d2.name as districtName, c.id as communeId, c.name as communeName, d.id as departmentId ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM \"user\" u ");
        sqlConditional.append("INNER JOIN department d ON u.department_id = d.id ");
        sqlConditional.append("INNER JOIN site s ON s.id = d.site_id ");
        sqlConditional.append("LEFT JOIN province p ON u.province_id = p.id ");
        sqlConditional.append("LEFT JOIN district d2 ON u.district_id = d2.id ");
        sqlConditional.append("LEFT JOIN commune c ON u.commune_id = c.id ");
        sqlConditional.append("WHERE 1=1 ");
        if (usernames != null && !usernames.isEmpty() && usernames.size() > 0) {
            sqlConditional.append("AND u.username IN :usernames ");
            queryParams.put("usernames", usernames);
        }
        if (role != null) {
            sqlConditional.append("AND u.role LIKE :role ");
            queryParams.put("role", "%" + role + "%");
        }
        if (createdOnStart != null && createdOnEnd != null) {
            sqlConditional.append("AND u.created_on between :createdOnStart and :createdOnEnd ");
            queryParams.put("createdOnStart", createdOnStart);
            queryParams.put("createdOnEnd", createdOnEnd);
        }
        if (!StringUtils.isBlank(keyword)) {
            sqlConditional.append("AND ( u.username LIKE :keyword OR u.first_name LIKE :keyword OR u.last_name LIKE :keyword  OR u.email LIKE :keyword  OR u.phone_number LIKE :keyword  ) ");
            queryParams.put("keyword", "%" + keyword + "%");
        }

        if (departmentIds != null && !departmentIds.isEmpty() && departmentIds.size() > 0) {
            sqlConditional.append("AND u.department_id IN :departmentIds ");
            queryParams.put("departmentIds", departmentIds);
        } else {
            sqlConditional.append("AND u.department_id = null ");
        }

        if (enable != null) {
            sqlConditional.append("AND u.enable = :enable ");
            queryParams.put("enable", enable);
        }

        if (provinceId != null) {
            sqlConditional.append("AND u.province_id = :provinceId ");
            queryParams.put("provinceId", provinceId);
        }

        if (districtId != null) {
            sqlConditional.append("AND u.district_id = :districtId ");
            queryParams.put("districtId", districtId);
        }

        if (communeId != null) {
            sqlConditional.append("AND u.commune_id = :communeId ");
            queryParams.put("communeId", communeId);
        }


        Query query = entityManager.createNativeQuery(sqlGetData + sqlConditional + orderByClause);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        queryParams.forEach(query::setParameter);
        List<Object[]> queryResult = query.getResultList();
        List<IUserController.UserFilterResponse> listData = new ArrayList<>();
        for (Object[] object : queryResult) {
            IUserController.UserFilterResponse userFilter = new IUserController.UserFilterResponse();
            userFilter.setUsername((String) object[0]);
            userFilter.setFirstName((String) object[1]);
            userFilter.setLastName((String) object[2]);
            userFilter.setEmail((String) object[3]);
            userFilter.setGender((String) object[4]);
            userFilter.setPhoneNumber((String) object[5]);
            userFilter.setDateOfBirth((Date) object[6]);
            userFilter.setEnable((Boolean) object[7]);
            String roleNames = (String) object[8];
            userFilter.setRoleName(roleNames);
            List<String> rolesList = roleNames == null ? null : Arrays.asList(roleNames.split(";"));
            userFilter.setRoles(rolesList);
            userFilter.setDepartmentName((String) object[9]);
            userFilter.setCountryCode((String) object[10]);
            userFilter.setCreatedOn((Date) object[11]);
            userFilter.setLastUpdatedOn((Date) object[12]);
            userFilter.setSiteId((UUID) object[13]);
            userFilter.setSiteName((String) object[14]);
            userFilter.setProvinceId((Integer) object[15]);
            userFilter.setProvinceName((String) object[16]);
            userFilter.setDistrictId((Integer) object[17]);
            userFilter.setDistrictName((String) object[18]);
            userFilter.setCommuneId((Integer) object[19]);
            userFilter.setCommuneName((String) object[20]);
            userFilter.setDepartmentId((UUID) object[21]);
            listData.add(userFilter);
        }
        Query queryCountAll = entityManager.createNativeQuery(sqlCountAll + sqlConditional);
        queryParams.forEach(queryCountAll::setParameter);
        int countAll = ((Number) queryCountAll.getSingleResult()).intValue();
        return new PageImpl<>(listData, pageable, countAll);
    }


    @Override
    public List<IUserController.UserFilterResponse> filter(Collection<String> usernames, String role
        , LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword
        , Collection<UUID> departmentIds, Integer provinceId, Integer districtId, Integer communeId) {
        Map<String, Object> queryParams = new HashMap<>();
        String orderByClause = "";
        String sqlGetData = "SELECT u.username, u.first_name as firstName, u.last_name as lastName, u.email, u.gender, u.phone_number as phoneNumber," +
            " u.dob as dateOfBirth, u.enable, u.role as roleName, d.name as departmentName, u.country_code as countryCode, u.created_on as createdOn," +
            " u.last_updated_on as lastUpdatedOn, s.id as siteId, s.name as siteName, p.id as provinceId, p.name as provinceName, d2.id as districtId," +
            " d2.name as districtName, c.id as communeId, c.name as communeName, d.id as departmentId ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM \"user\" u ");
        sqlConditional.append("INNER JOIN department d ON u.department_id = d.id ");
        sqlConditional.append("INNER JOIN site s ON s.id = d.site_id ");
        sqlConditional.append("LEFT JOIN province p ON u.province_id = p.id ");
        sqlConditional.append("LEFT JOIN district d2 ON u.district_id = d2.id ");
        sqlConditional.append("LEFT JOIN commune c ON u.commune_id = c.id ");
        sqlConditional.append("WHERE 1=1 ");
        if (usernames != null && !usernames.isEmpty() && usernames.size() > 0) {
            sqlConditional.append("AND u.username IN :usernames ");
            queryParams.put("usernames", usernames);
        }
        if (role != null) {
            sqlConditional.append("AND u.role = :role ");
            queryParams.put("roles", "%" + role + "%");
        }
        if (createdOnStart != null && createdOnEnd != null) {
            sqlConditional.append("AND u.created_on between :createdOnStart and :createdOnEnd ");
            queryParams.put("createdOnStart", createdOnStart);
            queryParams.put("createdOnEnd", createdOnEnd);
        }
        if (!StringUtils.isBlank(keyword)) {
            sqlConditional.append("AND ( u.username LIKE :keyword OR u.first_name LIKE :keyword OR u.last_name LIKE :keyword  OR u.email LIKE :keyword  OR u.phone_number LIKE :keyword  ) ");
            queryParams.put("keyword", "%" + keyword + "%");
        }

        if (departmentIds != null && !departmentIds.isEmpty() && departmentIds.size() > 0) {
            sqlConditional.append("AND d.id IN :departmentIds ");
            queryParams.put("departmentIds", departmentIds);
        } else {
            sqlConditional.append("AND u.department_id = null ");
        }

        if (enable != null) {
            sqlConditional.append("AND u.enable = :enable ");
            queryParams.put("enable", enable);
        }

        if (provinceId != null) {
            sqlConditional.append("AND u.province_id = :provinceId ");
            queryParams.put("provinceId", provinceId);
        }

        if (districtId != null) {
            sqlConditional.append("AND u.district_id = :districtId ");
            queryParams.put("districtId", districtId);
        }

        if (communeId != null) {
            sqlConditional.append("AND u.commune_id = :communeId ");
            queryParams.put("communeId", communeId);
        }

        Query query = entityManager.createNativeQuery(sqlGetData + sqlConditional + orderByClause);
        queryParams.forEach(query::setParameter);
        List<Object[]> queryResult = query.getResultList();
        List<IUserController.UserFilterResponse> listData = new ArrayList<>();
        for (Object[] object : queryResult) {
            IUserController.UserFilterResponse userFilter = new IUserController.UserFilterResponse();
            userFilter.setUsername((String) object[0]);
            userFilter.setFirstName((String) object[1]);
            userFilter.setLastName((String) object[2]);
            userFilter.setEmail((String) object[3]);
            userFilter.setGender((String) object[4]);
            userFilter.setPhoneNumber((String) object[5]);
            userFilter.setDateOfBirth((Date) object[6]);
            userFilter.setEnable((Boolean) object[7]);
            String roleNames = (String) object[8];
            List<String> rolesList = roleNames == null ? null : Arrays.asList(roleNames.split(";"));
            userFilter.setRoles(rolesList);
            userFilter.setDepartmentName((String) object[9]);
            userFilter.setCountryCode((String) object[10]);
            userFilter.setCreatedOn((Date) object[11]);
            userFilter.setLastUpdatedOn((Date) object[12]);
            userFilter.setSiteId((UUID) object[13]);
            userFilter.setSiteName((String) object[14]);
            userFilter.setProvinceId((Integer) object[15]);
            userFilter.setProvinceName((String) object[16]);
            userFilter.setDistrictId((Integer) object[17]);
            userFilter.setDistrictName((String) object[18]);
            userFilter.setCommuneId((Integer) object[19]);
            userFilter.setCommuneName((String) object[20]);
            userFilter.setDepartmentId((UUID) object[21]);
            listData.add(userFilter);
        }
        return listData;
    }
}
