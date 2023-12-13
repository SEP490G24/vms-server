package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TemplateRepository extends GenericRepository<Template, UUID> {

    @Query(value = "select u from Template u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((:type is null) or (u.type = :type)) " +
        "and ((coalesce(:siteId) is null) or (u.siteId in :siteId)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.code) LIKE %:keyword% " +
        "or UPPER(u.subject) LIKE %:keyword% " +
        "or UPPER(u.body) LIKE %:keyword% " +
        "or UPPER(u.description) LIKE %:keyword% ))")
    Page<Template> filter(Pageable pageable,
                          @Param("names") @Nullable Collection<String> names,
                          @Param("siteId") @Nullable Collection<UUID> siteId,
                          @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                          @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                          @Param("enable") @Nullable Boolean isEnable,
                          @Param("type") @Nullable Constants.TemplateType type,
                          @Param("keyword") @Nullable String keyword);

    @Query(value = "select u from Template u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((:type is null) or (u.type = :type)) " +
        "and ((coalesce(:siteId) is null) or (u.siteId in :siteId)) " +
        "and ((:keyword is null) " +
        "or ( UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.code) LIKE %:keyword% " +
        "or UPPER(u.subject) LIKE %:keyword% " +
        "or UPPER(u.body) LIKE %:keyword% " +
        "or UPPER(u.description) LIKE %:keyword% ))")
    List<Template> filter(
        @Param("names") @Nullable Collection<String> names,
        @Param("siteId") @Nullable Collection<UUID> siteId,
        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
        @Param("enable") @Nullable Boolean isEnable,
        @Param("type") @Nullable Constants.TemplateType type,
        @Param("keyword") @Nullable String keyword);

    List<Template> findAllBySiteIdAndEnableIsTrue(UUID siteId);

    List<Template> findAllBySiteIdAndEnableIsTrueAndType(UUID siteId, Constants.TemplateType type);

    boolean existsByCodeAndSiteId(String code, UUID siteId);
}
