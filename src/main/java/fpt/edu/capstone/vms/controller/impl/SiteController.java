package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.ISiteService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
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
    public ResponseEntity<?> findById(UUID id) {
        return ResponseEntity.ok(mapper.map(siteService.findById(id), SiteFilterDTO.class));
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
        return ResponseEntity.ok(mapper.map(siteService.findAll(), new TypeToken<List<SiteFilterDTO>>() {}.getType()));
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
        var siteEntity = siteService.filter(
            filter.getNames(),
            filter.getOrganizationId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getEnable(),
            filter.getKeyword());
        var siteEntityPageable = siteService.filter(
            pageable,
            filter.getNames(),
            filter.getOrganizationId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getEnable(),
            filter.getKeyword());


        List<ISiteController.SiteFilterDTO> siteFilterDTOS = mapper.map(siteEntityPageable.getContent(), new TypeToken<List<ISiteController.SiteFilterDTO>>() {
        }.getType());

        return isPageable ?
            ResponseEntity.ok(new PageImpl(siteFilterDTOS, pageable, siteFilterDTOS.size()))
            : ResponseEntity.ok(mapper.map(siteEntity, new TypeToken<List<SiteFilterDTO>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<List<?>> findAllByOrganizationId(String organizationId) {
        return ResponseEntity.ok(mapper.map(siteService.findAllByOrganizationId(organizationId), new TypeToken<List<SiteFilterDTO>>() {}.getType()));
    }
}
