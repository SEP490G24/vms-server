package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMap;
import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMapPk;
import fpt.edu.capstone.vms.persistence.repository.SiteDepartmentMapRepository;
import fpt.edu.capstone.vms.persistence.service.ISiteDepartmentMapService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SiteDepartmentMapServiceImpl extends GenericServiceImpl<SiteDepartmentMap, SiteDepartmentMapPk> implements ISiteDepartmentMapService {

    private final SiteDepartmentMapRepository siteDepartmentMapRepository;

    public SiteDepartmentMapServiceImpl(SiteDepartmentMapRepository siteDepartmentMapRepository) {
        this.siteDepartmentMapRepository = siteDepartmentMapRepository;
        this.init(siteDepartmentMapRepository);
    }

}
