package fpt.edu.capstone.vms.persistence.repository;

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
        "((:year IS NULL) OR (EXTRACT(YEAR FROM t.startTime) = :year)) " +
        "AND ((:month IS NULL) OR (EXTRACT(MONTH FROM t.startTime) = :month)) " +
        "AND ((cast(:startTime as date) is null ) OR (t.startTime BETWEEN :startTime AND :endTime )) " +
        "AND ((cast(:endTime as date) is null ) OR (t.endTime BETWEEN :startTime AND :endTime)) " +
        "AND ((COALESCE(:sites) IS NULL) OR (t.siteId IN :sites)) " +
        "GROUP BY t.purpose")
    List<Object[]> countTicketsByPurposeWithPie(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("year") Integer year,
        @Param("month") Integer month,
        @Param("sites") @Nullable Collection<String> sites);

    @Query("SELECT TO_CHAR(t.startTime, 'MM') as formattedMonth, t.purpose, COUNT(t) " +
        "FROM Ticket t WHERE " +
        "EXTRACT(YEAR FROM t.startTime) = :year " +
        "AND ((COALESCE(:sites) IS NULL) OR (t.siteId IN :sites)) " +
        "GROUP BY formattedMonth, t.purpose " +
        "ORDER BY formattedMonth, t.purpose")
    List<Object[]> countTicketsByPurposeByWithMultiLine(@Param("year") Integer year, @Param("sites") @Nullable Collection<String> sites);

}
