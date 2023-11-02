package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.IReasonController;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IReasonService extends IGenericService<Reason, UUID> {

    Reason create(IReasonController.ReasonDto roomDto);

    Page<Reason> filter(Pageable pageable,
                        List<String> names,
                        UUID siteId,
                        LocalDateTime createdOnStart,
                        LocalDateTime createdOnEnd,
                        Boolean enable,
                        String keyword);

    List<Reason> filter(
        List<String> names,
        UUID siteId,
        LocalDateTime createdOnStart,
        LocalDateTime createdOnEnd,
        Boolean enable,
        String keyword);

    List<Reason> finAllBySiteId(UUID siteId);
}
