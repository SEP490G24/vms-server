package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardRepository extends GenericRepository<Ticket, UUID> {

    @Query("SELECT t.purpose, COUNT(t) FROM Ticket t WHERE DATE(t.startTime) = :date " +
        "and ((coalesce(:sites) is null) or (t.siteId in :sites)) " +
        "GROUP BY t.purpose")
    List<Object[]> countTicketsByPurposeAndDate(@Param("date") Date date, @Param("sites") @Nullable Collection<String> sites);

    @Query("SELECT t.purpose, COUNT(t) FROM Ticket t WHERE EXTRACT(MONTH FROM t.startTime) = :month " +
        "and ((coalesce(:sites) is null) or (t.siteId in :sites)) " +
        "GROUP BY t.purpose")
    List<Object[]> countTicketsByPurposeAndMonth(@Param("month") int month, @Param("sites") @Nullable Collection<String> sites);

    @Query("SELECT t.purpose, COUNT(t) FROM Ticket t WHERE EXTRACT(YEAR FROM t.startTime) = :year " +
        "and ((coalesce(:sites) is null) or (t.siteId in :sites)) " +
        "GROUP BY t.purpose")
    List<Object[]> countTicketsByPurposeAndYear(@Param("year") int year, @Param("sites") @Nullable Collection<String> sites);


}
