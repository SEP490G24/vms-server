package fpt.edu.capstone.vms.persistence.repository.impl;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import fpt.edu.capstone.vms.constants.Constants;
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
import java.util.*;
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {


    final EntityManager entityManager;


    @Override
    public Page<IUserController.UserFilter> filter(Pageable pageable, Collection<String> usernames, Collection<Constants.UserRole> roles, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword, String departmentId) {
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
            "u.dob as dateOfBirth, u.enable, u.role as roleName, d.name as departmentName ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM \"user\" u ");
        sqlConditional.append("LEFT JOIN department d ON u.department_id = d.id ");
        sqlConditional.append("WHERE 1=1 ");
        if (usernames != null && !usernames.isEmpty() && usernames.size() > 0 ) {
            sqlConditional.append("AND u.username = :usernames ");
            queryParams.put("usernames",usernames);
        }
        if (roles != null && !roles.isEmpty() && roles.size() > 0 ) {
            sqlConditional.append("AND u.role = :roles ");
            queryParams.put("roles",roles);
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

        if (!StringUtils.isBlank(departmentId)) {
            sqlConditional.append("AND d.id = :departmentId ");
            queryParams.put("departmentId",departmentId);
        }
        if (enable != null) {
            sqlConditional.append("AND u.enable = :enable ");
            queryParams.put("enable",enable);
        }

        Query query = entityManager.createNativeQuery(sqlGetData + sqlConditional + orderByClause);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        queryParams.forEach(query::setParameter);
        List<Object[]> queryResult = query.getResultList();
        List<IUserController.UserFilter> listData = new ArrayList<>();
        for (Object[] object : queryResult) {
            IUserController.UserFilter userFilter = new IUserController.UserFilter();
            userFilter.setUsername((String) object[0]);
            userFilter.setFirstName((String) object[1]);
            userFilter.setLastName((String) object[2]);
            userFilter.setEmail((String) object[3]);
            userFilter.setGender((String) object[4]);
            userFilter.setPhoneNumber((String) object[5]);
            userFilter.setDateOfBirth((Date) object[6]);
            userFilter.setEnable((Boolean) object[7]);
            userFilter.setRoleName((String) object[8]);
            userFilter.setDepartmentName((String) object[9]);
            listData.add(userFilter);
        }
        Query queryCountAll = entityManager.createNativeQuery(sqlCountAll + sqlConditional);
        queryParams.forEach(queryCountAll::setParameter);
        int countAll = ((Number) queryCountAll.getSingleResult()).intValue();
        return new PageImpl<>(listData, pageable, countAll);
    }


    @Override
    public List<IUserController.UserFilter> filter(Collection<String> usernames, Collection<Constants.UserRole> roles, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword, String departmentId) {
        Map<String, Object> queryParams = new HashMap<>();
        String orderByClause = "";
        String sqlGetData = "SELECT u.username, u.first_name as firstName, u.last_name as lastName, u.email, u.gender, u.phone_number as phoneNumber," +
            "u.dob as dateOfBirth, u.enable, u.role as roleName, d.name as departmentName ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM \"user\" u ");
        sqlConditional.append("LEFT JOIN department d ON u.department_id = d.id ");
        sqlConditional.append("WHERE 1=1 ");
        if (usernames != null && !usernames.isEmpty() && usernames.size() > 0 ) {
            sqlConditional.append("AND u.username = :usernames ");
            queryParams.put("usernames",usernames);
        }
        if (roles != null && !roles.isEmpty() && roles.size() > 0 ) {
            sqlConditional.append("AND u.role = :roles ");
            queryParams.put("roles",roles);
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

        if (!StringUtils.isBlank(departmentId)) {
            sqlConditional.append("AND d.id = :departmentId ");
            queryParams.put("departmentId",departmentId);
        }
        if (enable != null) {
            sqlConditional.append("AND u.enable = :enable ");
            queryParams.put("enable",enable);
        }

        Query query = entityManager.createNativeQuery(sqlGetData + sqlConditional + orderByClause);
        queryParams.forEach(query::setParameter);
        List<Object[]> queryResult = query.getResultList();
        List<IUserController.UserFilter> listData = new ArrayList<>();
        for (Object[] object : queryResult) {
            IUserController.UserFilter userFilter = new IUserController.UserFilter();
            userFilter.setUsername((String) object[0]);
            userFilter.setFirstName((String) object[1]);
            userFilter.setLastName((String) object[2]);
            userFilter.setEmail((String) object[3]);
            userFilter.setGender((String) object[4]);
            userFilter.setPhoneNumber((String) object[5]);
            userFilter.setDateOfBirth((Date) object[6]);
            userFilter.setEnable((Boolean) object[7]);
            userFilter.setRoleName((String) object[8]);
            userFilter.setDepartmentName((String) object[9]);
            listData.add(userFilter);
        }
        return listData;
    }
}
