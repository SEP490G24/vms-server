package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.List;
import java.util.UUID;


public interface IReasonService extends IGenericService<Reason, UUID> {

    List<Reason> findAllByType(Constants.Reason type);
}
