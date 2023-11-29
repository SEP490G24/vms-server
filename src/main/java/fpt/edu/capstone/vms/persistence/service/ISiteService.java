package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface ISiteService extends IGenericService<Site, UUID> {

    Site updateSite(ISiteController.UpdateSiteInfo updateSite, UUID id);

    Page<Site> filter(Pageable pageable,
                      List<String> names,
                      LocalDateTime createdOnStart,
                      LocalDateTime createdOnEnd,
                      String createBy,
                      String lastUpdatedBy,
                      Boolean enable,
                      Integer provinceId,
                      Integer districtId,
                      Integer communeId,
                      String keyword);

    List<Site> filter(List<String> names,
                      LocalDateTime createdOnStart,
                      LocalDateTime createdOnEnd,
                      String createdBy,
                      String lastUpdatedBy,
                      Boolean enable,
                      Integer provinceId,
                      Integer districtId,
                      Integer communeId,
                      String keyword);

    List<Site> findAllByOrganizationId(String organizationId);

    Site findById(String id);

    Boolean deleteSite(UUID siteId);
}
