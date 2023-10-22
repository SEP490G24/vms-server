package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.District;
import fpt.edu.capstone.vms.persistence.repository.DistrictRepository;
import fpt.edu.capstone.vms.persistence.service.IDistrictService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistrictServiceImpl extends GenericServiceImpl<District, Integer> implements IDistrictService {

    private final DistrictRepository districtRepository;

    public DistrictServiceImpl(DistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
        this.init(districtRepository);
    }

    /**
     * The function returns a list of districts based on the given province ID.
     *
     * @param provinceId The provinceId parameter is an Integer that represents the ID of a province.
     * @return The method is returning a list of District objects.
     */
    @Override
    public List<District> findAllByProvinceId(Integer provinceId) {
        return districtRepository.findAllByProvinceId(provinceId);
    }
}
