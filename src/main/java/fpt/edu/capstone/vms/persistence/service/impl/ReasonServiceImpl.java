package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IReasonController;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.repository.ReasonRepository;
import fpt.edu.capstone.vms.persistence.service.IReasonService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
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
    public Reason update(Reason reasonInfo, UUID id) {
        var reason = reasonRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(reason))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found reason");
        reasonRepository.save(reason.update(reasonInfo));
        return reason;
    }

    @Override
    @Transactional
    public Reason create(IReasonController.ReasonDto reasonDto) {
        if (ObjectUtils.isEmpty(reasonDto))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Object is empty");
        if (StringUtils.isEmpty(reasonDto.getSiteId().toString()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");
        var reason = mapper.map(reasonDto, Reason.class);
        reason.setEnable(true);
        reasonRepository.save(reason);
        return reason;
    }

    @Override
    public Page<Reason> filter(Pageable pageable, List<String> names, UUID siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        return reasonRepository.filter(
            pageable,
            names,
            siteId,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Reason> filter(List<String> names, UUID siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, Boolean enable, String keyword) {
        return reasonRepository.filter(
            names,
            siteId,
            createdOnStart,
            createdOnEnd,
            enable,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Reason> finAllBySiteId(UUID siteId) {
        return reasonRepository.findAllBySiteIdAndEnableIsTrue(siteId);
    }
}
