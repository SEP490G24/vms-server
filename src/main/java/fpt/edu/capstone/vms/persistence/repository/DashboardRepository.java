package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardRepository extends GenericRepository<Ticket, UUID> {

    @Query("SELECT t.purpose, COUNT(t) FROM Ticket t WHERE " +
        "((cast(:startTime as date) is null ) OR (t.startTime BETWEEN :startTime AND :endTime )) " +
        "AND ((cast(:endTime as date) is null ) OR (t.endTime BETWEEN :startTime AND :endTime)) " +
        "AND ((COALESCE(:sites) IS NULL) OR (t.siteId IN :sites)) " +
        "GROUP BY t.purpose")
    List<Object[]> countTicketsByPurposeWithPie(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("sites") @Nullable Collection<String> sites);

    @Query("SELECT TO_CHAR(t.startTime, 'YYYY-MM-DD') as day, t.purpose, COUNT(t) " +
        "FROM Ticket t WHERE " +
        "((cast(:startTime as date) is null ) OR (t.startTime BETWEEN :startTime AND :endTime )) " +
        "AND ((cast(:endTime as date) is null ) OR (t.endTime BETWEEN :startTime AND :endTime)) " +
        "AND ((COALESCE(:sites) IS NULL) OR (t.siteId IN :sites)) " +
        "GROUP BY day, t.purpose " +
        "ORDER BY day, t.purpose")
    List<Object[]> countTicketsByPurposeByWithMultiLine(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("sites") @Nullable Collection<String> sites);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE " +
        "((COALESCE(:status) IS NULL) OR (t.status IN :status)) " +
        "and ((cast(:startTime as date) is null ) OR (t.startTime BETWEEN :startTime AND :endTime )) " +
        "AND ((cast(:endTime as date) is null ) OR (t.endTime BETWEEN :startTime AND :endTime)) " +
        "AND ((COALESCE(:sites) IS NULL) OR (t.siteId IN :sites)) ")
    Integer countTotalTickets(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("status") @Nullable Collection<Constants.StatusTicket> status,
        @Param("sites") @Nullable Collection<String> sites);

    @Query("SELECT COUNT(t) FROM CustomerTicketMap t WHERE " +
        "((COALESCE(:status) IS NULL) OR (t.status IN :status)) " +
        "and (((cast(:startTime as date) is null ) AND (cast(:endTime as date) is null )) OR (t.checkInTime BETWEEN :startTime AND :endTime )) " +
        "AND ((COALESCE(:sites) IS NULL) OR (t.ticketEntity.siteId IN :sites)) ")
    Integer countTotalVisits(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("status") @Nullable Collection<Constants.StatusTicket> status,
        @Param("sites") @Nullable Collection<String> sites);
}
