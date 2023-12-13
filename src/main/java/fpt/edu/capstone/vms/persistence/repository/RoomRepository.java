package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Room;
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
public interface RoomRepository extends GenericRepository<Room, UUID> {

    @Query(value = "select u from Room u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((coalesce(:siteIds) is null) or (u.siteId in :siteIds)) " +
        "and ((:createdBy is null) or (u.createdBy in :createdBy)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.code) LIKE %:keyword% " +
        "or UPPER(u.description) LIKE %:keyword% ))")
    Page<Room> filter(Pageable pageable,
                      @Param("names") @Nullable Collection<String> names,
                      @Param("siteIds") @Nullable Collection<UUID> siteIds,
                      @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                      @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                      @Param("enable") @Nullable Boolean isEnable,
                      @Param("keyword") @Nullable String keyword,
                      @Param("createdBy") @Nullable String createdBy);

    @Query(value = "select u from Room u " +
        "where ((coalesce(:names) is null) or (u.name in :names)) " +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:enable is null) or (u.enable = :enable)) " +
        "and ((coalesce(:siteIds) is null) or (u.siteId in :siteIds)) " +
        "and ((:createdBy is null) or (u.createdBy in :createdBy)) " +
        "and ((:keyword is null) " +
        "or ( UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.code) LIKE %:keyword% " +
        "or UPPER(u.description) LIKE %:keyword% ))")
    List<Room> filter(
        @Param("names") @Nullable Collection<String> names,
        @Param("siteIds") @Nullable Collection<UUID> siteIds,
        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
        @Param("enable") @Nullable Boolean isEnable,
        @Param("keyword") @Nullable String keyword,
        @Param("createdBy") @Nullable String createdBy);

    List<Room> findAllBySiteIdAndEnableIsTrue(UUID siteId);

    boolean existsByCodeAndSiteId(String code, UUID siteId);

    boolean existsByDeviceId(Integer deviceId);
}
