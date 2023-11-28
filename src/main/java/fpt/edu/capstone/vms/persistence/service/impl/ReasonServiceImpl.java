package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.repository.ReasonRepository;
import fpt.edu.capstone.vms.persistence.service.IReasonService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReasonServiceImpl extends GenericServiceImpl<Reason, UUID> implements IReasonService {

    private final ReasonRepository reasonRepository;
    private final ModelMapper mapper;


    public ReasonServiceImpl(ReasonRepository reasonRepository, ModelMapper mapper) {
        this.reasonRepository = reasonRepository;
        this.mapper = mapper;
        this.init(reasonRepository);
    }

    @Override
    public List<Reason> findAllByType(Constants.Reason type) {
        return reasonRepository.findAllByType(type);
    }
}
