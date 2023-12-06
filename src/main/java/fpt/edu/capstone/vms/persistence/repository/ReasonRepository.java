package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReasonRepository extends GenericRepository<Reason, Integer> {
    List<Reason> findAllByType(Constants.Reason reason);
}
