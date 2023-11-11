package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IOrganizationService extends IGenericService<Organization, UUID> {

    Page<Organization> filter(Pageable pageable,
                              List<String> names,
                              LocalDateTime createdOnStart,
                              LocalDateTime createdOnEnd,
                              String createBy,
                              String lastUpdatedBy,
                              Boolean enable,
                              String keyword);

    List<Organization> filter(List<String> names,
                              LocalDateTime createdOnStart,
                              LocalDateTime createdOnEnd,
                              String createBy,
                              String lastUpdatedBy,
                              Boolean enable,
                              String keyword);

}
