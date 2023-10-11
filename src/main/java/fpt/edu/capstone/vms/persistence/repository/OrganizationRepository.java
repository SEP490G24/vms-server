package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;


@Repository
public interface OrganizationRepository extends GenericRepository<Organization, UUID>{

    @Query(value = "select u from Organization u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((:keyword is null) " +
        "or (u.name LIKE %:keyword% " +
        "or u.code LIKE %:keyword% " +
        "or u.website LIKE %:keyword% " +
        "or u.representative LIKE %:keyword% " +
        "or u.lastUpdatedBy LIKE %:keyword% " +
        "or u.createdBy LIKE %:keyword%))")
    Page<Organization> filter(Pageable pageable,
                      @Param("names") @Nullable Collection<String> names,
                      @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                      @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                      @Param("createBy") @Nullable String createBy,
                      @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
                      @Param("enable") @Nullable Boolean isEnable,
                      @Param("keyword") @Nullable String keyword);


}
