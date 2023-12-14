package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Device;
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
public interface DeviceRepository extends GenericRepository<Device, Integer> {

    @Query(value = "select u from Device u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((:deviceType is null) or (u.deviceType = :deviceType)) " +
        "and ((coalesce(:siteIds) is null) or (u.siteId in :siteIds)) " +
        "and ((:createdBy is null) or (u.createdBy in :createdBy)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.code) LIKE %:keyword% " +
        "or UPPER(u.description) LIKE %:keyword% ))")
    Page<Device> filter(Pageable pageable,
                        @Param("names") @Nullable Collection<String> names,
                        @Param("siteIds") @Nullable Collection<UUID> siteIds,
                        @Param("deviceType") @Nullable Constants.DeviceType deviceType,
                        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                        @Param("enable") @Nullable Boolean isEnable,
                        @Param("keyword") @Nullable String keyword,
                        @Param("createdBy") @Nullable String createdBy);

    @Query(value = "select u from Device u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((:deviceType is null) or (u.deviceType = :deviceType)) " +
        "and ((coalesce(:siteIds) is null) or (u.siteId in :siteIds)) " +
        "and ((:createdBy is null) or (u.createdBy in :createdBy)) " +
        "and ((:keyword is null) " +
        "or ( UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.code) LIKE %:keyword% " +
        "or UPPER(u.description) LIKE %:keyword% ))")
    List<Device> filter(
        @Param("names") @Nullable Collection<String> names,
        @Param("siteIds") @Nullable Collection<UUID> siteIds,
        @Param("deviceType") @Nullable Constants.DeviceType deviceType,
        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
        @Param("enable") @Nullable Boolean isEnable,
        @Param("keyword") @Nullable String keyword,
        @Param("createdBy") @Nullable String createdBy);

    Device findByMacIp(String macIp);

    @Query(value = "select u from Device u " +
        "left join Room r on r.deviceId = u.id " +
        "where ((coalesce(:siteIds) is null) or (u.siteId in :siteIds)) and r.deviceId is null and u.deviceType = :deviceType")
    List<Device> findAllWithNotUseInSite(@Param("siteIds") Collection<UUID> siteIds, Constants.DeviceType deviceType);

    boolean existsByCodeAndSiteId(String code, UUID siteId);

    boolean existsByMacIpAndSiteId(String macIp, UUID siteId);
}
