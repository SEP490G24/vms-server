package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.impl.SiteServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SiteController implements ISiteController {
    private final SiteServiceImpl siteService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<Site> findById(UUID id) {
        return ResponseEntity.ok(siteService.findById(id));
    }

    @Override
    public ResponseEntity<Site> delete(UUID id) {
        return siteService.delete(id);
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
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        }

    }

    @Override
    public ResponseEntity<?> updateSite(UpdateSiteInfo updateSiteInfo, UUID id) {
        try {
            var site = siteService.updateSite(updateSiteInfo, id);
            return ResponseEntity.ok(site);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> filter(SiteFilter filter) {
        return ResponseEntity.ok(
            siteService.filter(
                filter.getPageNumber(),
                filter.getNames(),
                filter.getCreatedOnStart(),
                filter.getCreatedOnEnd(),
                filter.getCreateBy(),
                filter.getLastUpdatedBy(),
                filter.getEnable(),
                filter.getKeyword()));
    }
}
