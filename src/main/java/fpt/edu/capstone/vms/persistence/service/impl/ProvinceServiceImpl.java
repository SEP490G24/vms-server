package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.Province;
import fpt.edu.capstone.vms.persistence.repository.ProvinceRepository;
import fpt.edu.capstone.vms.persistence.service.IProvinceService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProvinceServiceImpl extends GenericServiceImpl<Province, Integer> implements IProvinceService {

    private final ProvinceRepository provinceRepository;

    public ProvinceServiceImpl(ProvinceRepository provinceRepository) {
        this.provinceRepository = provinceRepository;
        this.init(provinceRepository);
    }
}
