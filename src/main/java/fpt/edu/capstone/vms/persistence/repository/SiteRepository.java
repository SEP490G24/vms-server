package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SiteRepository extends GenericRepository<Site, UUID> {

    @Query(value = "select u from Site u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createBy is null) or (u.createdBy like :createBy)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((:provinceId is null) or (u.provinceId = :provinceId)) " +
        "and ((:districtId is null) or (u.districtId = :districtId)) " +
        "and ((:communeId is null) or (u.communeId = :communeId)) " +
        "and ((cast(:orgId as string) is null) or (u.organizationId = :orgId)) " +
        "and ((:keyword is null) " +
        "or ( UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.address) LIKE %:keyword% " +
        "or UPPER(u.taxCode) LIKE %:keyword% " +
        "or UPPER(u.lastUpdatedBy) LIKE %:keyword% " +
        "or UPPER(u.createdBy) LIKE %:keyword% " +
        "or UPPER(u.phoneNumber) LIKE %:keyword% ))")
    Page<Site> filter(Pageable pageable,
                      @Param("names") @Nullable Collection<String> names,
                      @Param("orgId") @Nullable UUID orgId,
                      @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                      @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                      @Param("createBy") @Nullable String createBy,
                      @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
                      @Param("enable") @Nullable Boolean isEnable,
                      @Param("provinceId") @Nullable Integer provinceId,
                      @Param("districtId") @Nullable Integer districtId,
                      @Param("communeId") @Nullable Integer communeId,
                      @Param("keyword") @Nullable String keyword);


    @Query(value = "select u from Site u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((:provinceId is null) or (u.provinceId = :provinceId)) " +
        "and ((:districtId is null) or (u.districtId = :districtId)) " +
        "and ((:communeId is null) or (u.communeId = :communeId)) " +
        "and ((cast(:orgId as string) is null) or (u.organizationId = :orgId)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.address) LIKE %:keyword% " +
        "or UPPER(u.taxCode) LIKE %:keyword% " +
        "or UPPER(u.lastUpdatedBy) LIKE %:keyword% " +
        "or UPPER(u.createdBy) LIKE %:keyword% " +
        "or UPPER(u.phoneNumber) LIKE %:keyword% ))")
    List<Site> filter(
        @Param("names") @Nullable Collection<String> names,
        @Param("orgId") @Nullable UUID orgId,
        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
        @Param("createBy") @Nullable String createBy,
        @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
        @Param("enable") @Nullable Boolean isEnable,
        @Param("provinceId") @Nullable Integer provinceId,
        @Param("districtId") @Nullable Integer districtId,
        @Param("communeId") @Nullable Integer communeId,
        @Param("keyword") @Nullable String keyword);

    List<Site> findAllByOrganizationId(UUID organizationId);

    @Query("select s.organizationId from Site s where s.id =:siteId ")
    Optional<String> findOrganizationIdBySiteId(@Param("siteId") UUID siteId);

    boolean existsByIdAndOrganizationId(UUID siteId, UUID organizationId);

    boolean existsByCodeAndOrganizationId(String code, UUID organizationId);
}
