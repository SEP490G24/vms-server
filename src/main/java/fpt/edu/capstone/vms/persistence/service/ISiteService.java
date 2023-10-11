package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface ISiteService extends IGenericService<Site, UUID> {

    Site updateSite(ISiteController.UpdateSiteInfo updateSite, UUID id);

    Page<Site> filter(int pageNumber,
                      List<String> names,
                      LocalDateTime createdOnStart,
                      LocalDateTime createdOnEnd,
                      String createBy,
                      String lastUpdatedBy,
                      Boolean enable,
                      String keyword);

    List<Site> findAllByOrganizationId(String organizationId);
}
