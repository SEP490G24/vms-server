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
        "t.startTime >= :startTime AND t.endTime <= :endTime " +
        "and ((coalesce(:sites) is null) or (t.siteId in :sites)) " +
        "GROUP BY t.purpose")
    List<Object[]> countTicketsByPurpose(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("sites") @Nullable Collection<String> sites);


//    @Query("SELECT t.startTime, t.purpose, COUNT(t) FROM Ticket t WHERE " +
//        "t.startTime BETWEEN :startOfDay AND :endOfDay " +
//        "and ((coalesce(:sites) is null) or (t.siteId in :sites)) " +
//        "GROUP BY t.startTime, t.purpose")
//    List<Object[]> countTicketsByPurposeByDate(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime  endTime, @Param("sites") @Nullable Collection<String> sites);

}
