package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.Commune;
import fpt.edu.capstone.vms.persistence.repository.CommuneRepository;
import fpt.edu.capstone.vms.persistence.service.ICommuneService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommuneServiceImpl extends GenericServiceImpl<Commune, Integer> implements ICommuneService {

    private final CommuneRepository communeRepository;

    public CommuneServiceImpl(CommuneRepository communeRepository) {
        this.communeRepository = communeRepository;
        this.init(communeRepository);
    }

    @Override
    public List<Commune> findAllByDistrictId(Integer districtId) {
        return communeRepository.findAllByDistrictId(districtId);
    }
}
