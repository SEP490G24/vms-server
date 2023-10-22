package fpt.edu.capstone.vms.persistence.repository;

import fpt.edu.capstone.vms.persistence.entity.District;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends GenericRepository<District, Integer> {

    List<District> findAllByProvinceId(Integer provinceId);
}
