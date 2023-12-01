package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.I18n;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.repository.ReasonRepository;
import fpt.edu.capstone.vms.persistence.service.IReasonService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReasonServiceImpl extends GenericServiceImpl<Reason, Integer> implements IReasonService {

    private final ReasonRepository reasonRepository;


    public ReasonServiceImpl(ReasonRepository reasonRepository) {
        this.reasonRepository = reasonRepository;
        this.init(reasonRepository);
    }

    @Override
    public List<Reason> findAllByType(Constants.Reason type) {
        List<Reason> reasons = reasonRepository.findAllByType(type);
        reasons.stream().map(reason -> reason.setName(I18n.getMessage(reason.getCode()))).collect(Collectors.toList());
        return reasons;
    }
}
