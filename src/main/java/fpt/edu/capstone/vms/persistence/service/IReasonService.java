package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.List;


public interface IReasonService extends IGenericService<Reason, Integer> {

    List<Reason> findAllByType(Constants.Reason type);
}
