package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.persistence.entity.District;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.List;


public interface IDistrictService extends IGenericService<District, Integer> {

    List<District> findAllByProvinceId(Integer provinceId);
}
