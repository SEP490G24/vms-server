package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.User;
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
public interface SiteRepository extends GenericRepository<Site, UUID> {

    @Query(value = "select u from Site u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((:keyword is null) " +
        "or (u.name LIKE %:keyword% " +
        "or u.address LIKE %:keyword% " +
        "or u.district LIKE %:keyword% " +
        "or u.province LIKE %:keyword% " +
        "or u.taxCode LIKE %:keyword% " +
        "or u.ward LIKE %:keyword% " +
        "or u.lastUpdatedBy LIKE %:keyword% " +
        "or u.createdBy LIKE %:keyword% " +
        "or u.phoneNumber LIKE %:keyword% ))")
    Page<Site> filter(Pageable pageable,
                      @Param("names") @Nullable Collection<String> names,
                      @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                      @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                      @Param("createBy") @Nullable String createBy,
                      @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
                      @Param("enable") @Nullable Boolean isEnable,
                      @Param("keyword") @Nullable String keyword);

}
