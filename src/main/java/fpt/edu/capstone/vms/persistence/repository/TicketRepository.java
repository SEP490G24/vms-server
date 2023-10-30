package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Ticket;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TicketRepository extends GenericRepository<Ticket, UUID> {

    Integer countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqual(UUID roomId, LocalDateTime startTime, LocalDateTime endTime);

    Integer countByUsernameAndEndTimeGreaterThanEqualAndStartTimeLessThanEqual(String username, LocalDateTime startTime, LocalDateTime endTime);

    Boolean existsByIdAndUsername(UUID ticketId, String username);

//    @Query(value = "select u from Ticket u " +
//        "where (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
//        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
//        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
//        "and ((:enable is null) or (u.enable = :enable)) " +
//        "and ((cast(:username as string) is null) or (u.username = :username)) " +
//        "and ((cast(:roomId as string) is null) or (u.roomId = :roomId)) " +
//        "and ((cast(:templateId as string) is null) or (u.templateId = :templateId)) " +
//        "and ((cast(:status as string) is null) or (u.status = :status)) " +
//        "and ((:keyword is null) " +
//        "or u.lastUpdatedBy LIKE %:keyword% " +
//        "or u.createdBy LIKE %:keyword% " +
//        "or u.phoneNumber LIKE %:keyword% ))")
//    Page<Site> filter(Pageable pageable,
//                      @Param("username") @Nullable String username,
//                      @Param("roomId") @Nullable UUID roomId,
//                      @Param("templateId") @Nullable UUID templateId,
//                      @Param("status") @Nullable Constants.StatusTicket status,
//                      @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
//                      @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
//                      @Param("createBy") @Nullable String createBy,
//                      @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
//                      @Param("enable") @Nullable Boolean isEnable,
//                      @Param("keyword") @Nullable String keyword);


//    @Query(value = "select u from Site u " +
//        "where ((coalesce(:names) is null) or (u.name in :names)) " +
//        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
//        "and ((:createBy is null) or (u.createdBy in :createBy)) " +
//        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
//        "and ((:enable is null) or (u.enable = :enable)) " +
//        "and ((cast(:orgId as string) is null) or (u.organizationId = :orgId)) " +
//        "and ((:keyword is null) " +
//        "or (u.name LIKE %:keyword% " +
//        "or u.address LIKE %:keyword% " +
//        "or u.taxCode LIKE %:keyword% " +
//        "or u.lastUpdatedBy LIKE %:keyword% " +
//        "or u.createdBy LIKE %:keyword% " +
//        "or u.phoneNumber LIKE %:keyword% ))")
//    List<Site> filter(
//        @Param("names") @Nullable Collection<String> names,
//        @Param("orgId") @Nullable UUID orgId,
//        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
//        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
//        @Param("createBy") @Nullable String createBy,
//        @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
//        @Param("enable") @Nullable Boolean isEnable,
//        @Param("keyword") @Nullable String keyword);
}
