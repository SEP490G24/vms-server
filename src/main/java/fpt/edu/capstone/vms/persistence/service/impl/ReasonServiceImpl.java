package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.repository.ReasonRepository;
import fpt.edu.capstone.vms.persistence.service.IReasonService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReasonServiceImpl extends GenericServiceImpl<Reason, Integer> implements IReasonService {

    private final ReasonRepository reasonRepository;


    public ReasonServiceImpl(ReasonRepository reasonRepository) {
        this.reasonRepository = reasonRepository;
        this.init(reasonRepository);
    }

    @Override
    public List<Reason> findAllByType(Constants.Reason type) {
        return reasonRepository.findAllByType(type);
    }
}
