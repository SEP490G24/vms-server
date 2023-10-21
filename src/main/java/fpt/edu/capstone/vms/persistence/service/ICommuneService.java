package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.persistence.entity.Commune;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.List;


public interface ICommuneService extends IGenericService<Commune, Integer> {

    List<Commune> findAllByDistrictId(Integer districtId);

}
