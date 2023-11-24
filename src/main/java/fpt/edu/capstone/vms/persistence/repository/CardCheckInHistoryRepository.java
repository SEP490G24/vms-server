package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.CardCheckInHistory;
import org.springframework.stereotype.Repository;


@Repository
public interface CardCheckInHistoryRepository extends GenericRepository<CardCheckInHistory, Integer>, CardCheckInHistoryRepositoryCustom {

}
