package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
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
public interface TicketRepository extends GenericRepository<Ticket, UUID> {

    @Query("SELECT COUNT(t) FROM Ticket t WHERE " +
        "t.roomId = :roomId " +
        "AND t.endTime >= :startTime " +
        "AND t.startTime <= :endTime " +
        "AND t.status <> :cancel " +
        "AND t.status <> :draft " +
        "AND t.status <> :complete")
    int countTicketsWithStatusNotLike(@Param("roomId") UUID roomId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("cancel") Constants.StatusTicket cancel,
                                      @Param("complete") Constants.StatusTicket complete,
                                      @Param("draft") Constants.StatusTicket draft);

    Integer countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqualAndStatusNotLike(UUID roomId, LocalDateTime startTime, LocalDateTime endTime, Constants.StatusTicket statusTicket);

    Integer countByUsernameAndEndTimeGreaterThanEqualAndStartTimeLessThanEqualAndStatusNotLike(String username, LocalDateTime startTime, LocalDateTime endTime, Constants.StatusTicket statusTicket);

    Boolean existsByIdAndUsername(UUID ticketId, String username);

    List<Ticket> findAllByCreatedOnBeforeAndStatus(LocalDateTime dateTime, Constants.StatusTicket statusTicket);

    List<Ticket> findAllByStartTimeLessThanEqualAndStartTimeGreaterThanAndStatus(LocalDateTime currentTime, LocalDateTime meetingStartTime, Constants.StatusTicket statusTicket);


    @Query(value = "select u from Ticket u " +
        "where ((coalesce(:names) is null) or (u.name in :names))" +
        "and ((coalesce(:sites) is null) or (u.siteId in :sites))" +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createdBy is null) or (u.createdBy like %:createdBy% )) " +
        "and ((:bookmark is null) or (u.isBookmark = :bookmark)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and (((cast(:startTimeStart as date) is null ) or (cast(:startTimeEnd as date) is null )) or (u.startTime between :startTimeStart and :startTimeEnd)) " +
        "and (((cast(:endTimeStart as date) is null ) or (cast(:endTimeEnd as date) is null )) or (u.endTime between :endTimeStart and :endTimeEnd)) " +
        "and ((coalesce(:usernames) is null) or (u.username in :usernames)) " +
        "and ((cast(:roomId as string) is null) or (u.roomId = :roomId)) " +
        "and ((cast(:status as string) is null) or (u.status = :status)) " +
        "and ((cast(:purpose as string) is null) or (u.purpose = :purpose)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.lastUpdatedBy) LIKE %:keyword% " +
        "or UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.createdBy) LIKE %:keyword% ))")
    Page<Ticket> filter(Pageable pageable,
                        @Param("names") @Nullable Collection<String> names,
                        @Param("sites") @Nullable Collection<String> sites,
                        @Param("usernames") @Nullable Collection<String> usernames,
                        @Param("roomId") @Nullable UUID roomId,
                        @Param("status") @Nullable Constants.StatusTicket status,
                        @Param("purpose") @Nullable Constants.Purpose purpose,
                        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                        @Param("startTimeStart") @Nullable LocalDateTime startTimeStart,
                        @Param("startTimeEnd") @Nullable LocalDateTime startTimeEnd,
                        @Param("endTimeStart") @Nullable LocalDateTime endTimeStart,
                        @Param("endTimeEnd") @Nullable LocalDateTime endTimeEnd,
                        @Param("createdBy") @Nullable String createdBy,
                        @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
                        @Param("bookmark") @Nullable Boolean bookmark,
                        @Param("keyword") @Nullable String keyword);

    @Query(value = "select u from Ticket u " +
        "where ((coalesce(:names) is null) or (u.name in :names))" +
        "and ((coalesce(:sites) is null) or (u.siteId in :sites))" +
        "and (((cast(:createdOnStart as date) is null ) or (cast(:createdOnEnd as date) is null )) or (u.createdOn between :createdOnStart and :createdOnEnd)) " +
        "and ((:createdBy is null) or (u.createdBy like %:createdBy%)) " +
        "and ((:bookmark is null) or (u.isBookmark = :bookmark)) " +
        "and ((:lastUpdatedBy is null) or (u.lastUpdatedBy in :lastUpdatedBy)) " +
        "and (((cast(:startTimeStart as date) is null ) or (cast(:startTimeEnd as date) is null )) or (u.startTime between :startTimeStart and :startTimeEnd)) " +
        "and (((cast(:endTimeStart as date) is null ) or (cast(:endTimeEnd as date) is null )) or (u.endTime between :endTimeStart and :endTimeEnd)) " +
        "and ((coalesce(:usernames) is null) or (u.username in :usernames)) " +
        "and ((cast(:roomId as string) is null) or (u.roomId = :roomId)) " +
        "and ((cast(:status as string) is null) or (u.status = :status)) " +
        "and ((cast(:purpose as string) is null) or (u.purpose = :purpose)) " +
        "and ((:keyword is null) " +
        "or (UPPER(u.lastUpdatedBy) LIKE %:keyword% " +
        "or UPPER(u.name) LIKE %:keyword% " +
        "or UPPER(u.createdBy) LIKE %:keyword% ))")
    List<Ticket> filter(@Param("names") @Nullable Collection<String> names,
                        @Param("sites") @Nullable Collection<String> sites,
                        @Param("usernames") @Nullable Collection<String> usernames,
                        @Param("roomId") @Nullable UUID roomId,
                        @Param("status") @Nullable Constants.StatusTicket status,
                        @Param("purpose") @Nullable Constants.Purpose purpose,
                        @Param("createdOnStart") @Nullable LocalDateTime createdOnStart,
                        @Param("createdOnEnd") @Nullable LocalDateTime createdOnEnd,
                        @Param("startTimeStart") @Nullable LocalDateTime startTimeStart,
                        @Param("startTimeEnd") @Nullable LocalDateTime startTimeEnd,
                        @Param("endTimeStart") @Nullable LocalDateTime endTimeStart,
                        @Param("endTimeEnd") @Nullable LocalDateTime endTimeEnd,
                        @Param("createdBy") @Nullable String createdBy,
                        @Param("lastUpdatedBy") @Nullable String lastUpdatedBy,
                        @Param("bookmark") @Nullable Boolean bookmark,
                        @Param("keyword") @Nullable String keyword);

    boolean existsByRoomId(UUID roomId);
}
