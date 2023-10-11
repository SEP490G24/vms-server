package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.Organization;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IOrganizationService extends IGenericService<Organization, UUID> {

    Page<Organization> filter(int pageNumber,
                            List<String> names,
                            LocalDateTime createdOnStart,
                            LocalDateTime createdOnEnd,
                            String createBy,
                            String lastUpdatedBy,
                            Boolean enable,
                            String keyword);

}
