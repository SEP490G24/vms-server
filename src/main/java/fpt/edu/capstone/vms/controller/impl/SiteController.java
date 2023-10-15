package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.ISiteService;
import fpt.edu.capstone.vms.persistence.service.impl.SiteServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SiteController implements ISiteController {
    private final ISiteService siteService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<Site> findById(UUID id) {
        return ResponseEntity.ok(siteService.findById(id));
    }

    @Override
    public ResponseEntity<?> delete(UUID id) {
        try {
            return siteService.delete(id);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(siteService.findAll());
    }

    @Override
    public ResponseEntity<?> createSite(CreateSiteInfo siteInfo) {
        try {
            var site = siteService.save(mapper.map(siteInfo, Site.class));
            return ResponseEntity.ok(site);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> updateSite(UpdateSiteInfo updateSiteInfo, UUID id) {
        try {
            var site = siteService.updateSite(updateSiteInfo, id);
            return ResponseEntity.ok(site);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> filter(SiteFilter filter, boolean isPageable, Pageable pageable) {
        return isPageable ? ResponseEntity.ok(
            siteService.filter(
                pageable,
                filter.getNames(),
                filter.getOrganizationId(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getCreateBy(),
                filter.getLastUpdatedBy(),
                filter.getEnable(),
                filter.getKeyword())) : ResponseEntity.ok(
            siteService.filter(
                filter.getNames(),
                filter.getOrganizationId(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getCreateBy(),
                filter.getLastUpdatedBy(),
                filter.getEnable(),
                filter.getKeyword())) ;
    }

    @Override
    public ResponseEntity<List<?>> findAllByOrganizationId(String organizationId) {
        return ResponseEntity.ok(siteService.findAllByOrganizationId(organizationId));
    }
}
