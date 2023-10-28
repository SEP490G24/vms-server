package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Ticket;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TicketRepository extends GenericRepository<Ticket, UUID> {

    Integer countByRoomIdAndEndTimeGreaterThanEqualAndStartTimeLessThanEqual(UUID roomId, LocalDateTime startTime, LocalDateTime endTime);


}
