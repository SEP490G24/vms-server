package fpt.edu.capstone.vms.persistence.repository.impl;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class AuditLogRepositoryCustomImpl implements AuditLogRepositoryCustom {

    final EntityManager entityManager;

    @Override
    public Page<IAuditLogController.AuditLogFilterDTO> filter(Pageable pageable, Collection<String> organizations
        , Collection<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd
        , String createdBy, String tableName, String keyword) {
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
        String sqlGetData = "SELECT u.id, u.code, u.site_id, s.name, u.organization_id, o.name," +
            " u.primary_key, u.table_name, u.audit_type, u.old_value, u.new_value, u.created_on as createdOn," +
            " u.created_by, u.last_updated_on as lastUpdatedOn, u.last_updated_by ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM audit_log u ");
        sqlConditional.append("LEFT JOIN site s ON cast(s.id as text) = u.site_id ");
        sqlConditional.append("LEFT JOIN organization o ON cast(o.id as text) = u.organization_id ");
        sqlConditional.append("WHERE 1=1 ");

        if (organizations != null && !organizations.isEmpty()) {
            sqlConditional.append("AND u.organization_id IN :organizations ");
            queryParams.put("organizations", organizations);
        }

        if (sites != null && !sites.isEmpty()) {
            sqlConditional.append("AND u.site_id IN :sites ");
            queryParams.put("sites", sites);
        }

        if (auditType != null) {
            sqlConditional.append("AND u.audit_type = :auditType ");
            queryParams.put("auditType", auditType.name());
        }

        if (createdOnStart != null && createdOnEnd != null) {
            sqlConditional.append("AND u.created_on between :createdOnStart and :createdOnEnd ");
            queryParams.put("createdOnStart", createdOnStart);
            queryParams.put("createdOnEnd", createdOnEnd);
        }
        if (!StringUtils.isBlank(keyword)) {
            sqlConditional.append("AND ( u.table_name LIKE :keyword OR u.primary_key LIKE :keyword OR u.created_by LIKE :keyword)");
            queryParams.put("keyword", "%" + keyword + "%");
        }

        if (createdBy != null) {
            sqlConditional.append("AND u.created_by = :createdBy ");
            queryParams.put("createdBy", createdBy);
        }

        if (tableName != null) {
            sqlConditional.append("AND u.table_name = :tableName ");
            queryParams.put("tableName", tableName);
        }

        Query query = entityManager.createNativeQuery(sqlGetData + sqlConditional + orderByClause);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        queryParams.forEach(query::setParameter);
        List<Object[]> queryResult = query.getResultList();
        List<IAuditLogController.AuditLogFilterDTO> listData = new ArrayList<>();
        for (Object[] object : queryResult) {
            IAuditLogController.AuditLogFilterDTO auditLogFilter = new IAuditLogController.AuditLogFilterDTO();
            auditLogFilter.setId((UUID) object[0]);
            auditLogFilter.setCode((String) object[1]);
            auditLogFilter.setSiteId((String) object[2]);
            auditLogFilter.setSiteName((String) object[3]);
            auditLogFilter.setOrganizationId((String) object[4]);
            auditLogFilter.setOrganizationName((String) object[5]);
            auditLogFilter.setPrimaryKey((String) object[6]);
            auditLogFilter.setTableName((String) object[7]);
            auditLogFilter.setAuditType((String) object[8]);
            auditLogFilter.setOldValue((String) object[9]);
            auditLogFilter.setNewValue((String) object[10]);
            auditLogFilter.setCreateOn((Date) object[11]);
            auditLogFilter.setCreateBy((String) object[12]);
            auditLogFilter.setLastUpdatedOn((Date) object[13]);
            auditLogFilter.setLastUpdatedBy((String) object[14]);
            listData.add(auditLogFilter);
        }
        Query queryCountAll = entityManager.createNativeQuery(sqlCountAll + sqlConditional);
        queryParams.forEach(queryCountAll::setParameter);
        int countAll = ((Number) queryCountAll.getSingleResult()).intValue();
        return new PageImpl<>(listData, pageable, countAll);
    }


    @Override
    public List<IAuditLogController.AuditLogFilterDTO> filter(Collection<String> organizations
        , Collection<String> sites, Constants.AuditType auditType, LocalDateTime createdOnStart, LocalDateTime createdOnEnd
        , String createdBy, String tableName, String keyword) {
        Map<String, Object> queryParams = new HashMap<>();
        String orderByClause = "";
        String sqlGetData = "SELECT u.id, u.code, u.site_id, s.name, u.organization_id, o.name," +
            " u.primary_key, u.table_name, u.audit_type, u.old_value, u.new_value, u.created_on," +
            " u.created_by, u.last_updated_on, u.last_updated_by ";
        StringBuilder sqlConditional = new StringBuilder();
        sqlConditional.append("FROM audit_log u ");
        sqlConditional.append("LEFT JOIN site s ON cast(s.id as string) = u.site_id ");
        sqlConditional.append("LEFT JOIN organization o ON cast(o.id as string) = u.organization_id ");
        sqlConditional.append("WHERE 1=1 ");

        if (organizations != null && !organizations.isEmpty()) {
            sqlConditional.append("AND u.organization_id IN :organizations ");
            queryParams.put("organizations", organizations);
        }

        if (sites != null && !sites.isEmpty()) {
            sqlConditional.append("AND u.site_id IN :sites ");
            queryParams.put("sites", sites);
        }

        if (auditType != null) {
            sqlConditional.append("AND u.audit_type = :auditType ");
            queryParams.put("auditType", auditType.name());
        }

        if (createdOnStart != null && createdOnEnd != null) {
            sqlConditional.append("AND u.created_on between :createdOnStart and :createdOnEnd ");
            queryParams.put("createdOnStart", createdOnStart);
            queryParams.put("createdOnEnd", createdOnEnd);
        }
        if (!StringUtils.isBlank(keyword)) {
            sqlConditional.append("AND ( u.table_name LIKE :keyword OR u.primary_key LIKE :keyword OR u.created_by LIKE :keyword)");
            queryParams.put("keyword", "%" + keyword + "%");
        }

        if (createdBy != null) {
            sqlConditional.append("AND u.created_by = :createdBy ");
            queryParams.put("createdBy", createdBy);
        }

        if (tableName != null) {
            sqlConditional.append("AND u.table_name = :tableName ");
            queryParams.put("tableName", tableName);
        }
        Query query = entityManager.createNativeQuery(sqlGetData + sqlConditional + orderByClause);
        queryParams.forEach(query::setParameter);
        List<Object[]> queryResult = query.getResultList();
        List<IAuditLogController.AuditLogFilterDTO> listData = new ArrayList<>();
        for (Object[] object : queryResult) {
            IAuditLogController.AuditLogFilterDTO auditLogFilter = new IAuditLogController.AuditLogFilterDTO();
            auditLogFilter.setId((UUID) object[0]);
            auditLogFilter.setCode((String) object[1]);
            auditLogFilter.setSiteId((String) object[2]);
            auditLogFilter.setSiteName((String) object[3]);
            auditLogFilter.setOrganizationId((String) object[4]);
            auditLogFilter.setOrganizationName((String) object[5]);
            auditLogFilter.setPrimaryKey((String) object[6]);
            auditLogFilter.setTableName((String) object[7]);
            auditLogFilter.setAuditType((String) object[8]);
            auditLogFilter.setOldValue((String) object[9]);
            auditLogFilter.setNewValue((String) object[10]);
            auditLogFilter.setCreateOn((Date) object[11]);
            auditLogFilter.setCreateBy((String) object[12]);
            auditLogFilter.setLastUpdatedOn((Date) object[13]);
            auditLogFilter.setLastUpdatedBy((String) object[14]);
            listData.add(auditLogFilter);
        }
        return listData;
    }
}
