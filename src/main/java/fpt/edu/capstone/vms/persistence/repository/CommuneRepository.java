package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.Commune;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommuneRepository extends GenericRepository<Commune, Integer> {

    List<Commune> findAllByDistrictId(Integer districtId);
}
