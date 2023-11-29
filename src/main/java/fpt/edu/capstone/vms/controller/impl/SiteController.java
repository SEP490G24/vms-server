package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.ISiteService;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SiteController implements ISiteController {
    private final ISiteService siteService;
    private final ModelMapper mapper;

    /**
     * The function returns a ResponseEntity containing a SiteFilterDTO object mapped from the Site object with the given
     * id.
     *
     * @param siteId The parameter "id" is of type UUID, which stands for Universally Unique Identifier. It is a 128-bit value
     *               used to uniquely identify an object or entity in a distributed computing environment. In this code snippet, the
     *               method "findById" is used to retrieve a site object by its id and
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> findById(String siteId) {
        try {
            return ResponseEntity.ok(mapper.map(siteService.findById(siteId), SiteFilterDTO.class));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function deletes a site with the given ID and returns a ResponseEntity with the result.
     *
     * @param id The "id" parameter is of type UUID, which stands for Universally Unique Identifier. It is a 128-bit value
     * used to uniquely identify information in computer systems. In this case, it is used to identify a specific site that
     * needs to be deleted.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> delete(UUID id) {
        try {
            return ResponseUtils.getResponseEntityStatus(siteService.deleteSite(id));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The function returns a ResponseEntity containing a list of SiteFilterDTO objects.
     *
     * @return The method is returning a ResponseEntity object containing a list of objects.
     */
    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(mapper.map(siteService.findAll(), new TypeToken<List<SiteFilterDTO>>() {}.getType()));
    }

    /**
     * The function creates a site using the provided site information and returns a response entity with the created site
     * or an error message.
     *
     * @param siteInfo The parameter `siteInfo` is an object of type `CreateSiteInfo`. It contains information required to
     * create a site.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> createSite(CreateSiteInfo siteInfo) {
        try {
            var site = siteService.save(mapper.map(siteInfo, Site.class));
            return ResponseUtils.getResponseEntityStatus(site);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The function updates a site using the provided information and returns a response entity with the updated site or an
     * error message.
     *
     * @param updateSiteInfo The updateSiteInfo parameter is an object that contains the information needed to update a
     * site. It likely includes properties such as the site's name, address, and any other relevant details that can be
     * updated.
     * @param id The `id` parameter is of type `UUID` and represents the unique identifier of the site that needs to be
     * updated.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> updateSite(UpdateSiteInfo updateSiteInfo, UUID id) {
        try {
            var site = siteService.updateSite(updateSiteInfo, id);
            return ResponseUtils.getResponseEntityStatus(site);
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The function filters site entities based on the provided filter criteria and returns the result as a ResponseEntity.
     *
     * @param filter The `filter` parameter is an object of type `SiteFilter` which contains various filter criteria such
     * as names, organization ID, created on start and end dates, create by, last updated by, enable status, and keyword.
     * @param isPageable The `isPageable` parameter is a boolean value that indicates whether the filtering should be
     * pageable or not. If `isPageable` is `true`, the filtering results will be returned as a pageable response. If
     * `isPageable` is `false`, the filtering results will be returned as
     * @param pageable The `pageable` parameter is an object of type `Pageable` which is used for pagination. It contains
     * information about the current page number, page size, sorting criteria, etc. It is used to retrieve a specific page
     * of results from the filtered data.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> filter(SiteFilter filter, boolean isPageable, Pageable pageable) {
        var siteEntity = siteService.filter(
            filter.getNames(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getEnable(),
            filter.getProvinceId(),
            filter.getDistrictId(),
            filter.getCommuneId(),
            filter.getKeyword());
        var siteEntityPageable = siteService.filter(
            pageable,
            filter.getNames(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getEnable(),
            filter.getProvinceId(),
            filter.getDistrictId(),
            filter.getCommuneId(),
            filter.getKeyword());


        List<ISiteController.SiteFilterDTO> siteFilterDTOS = mapper.map(siteEntityPageable.getContent(), new TypeToken<List<ISiteController.SiteFilterDTO>>() {
        }.getType());

        return isPageable ?
            ResponseEntity.ok(new PageImpl(siteFilterDTOS, pageable, siteEntityPageable.getTotalElements()))
            : ResponseEntity.ok(mapper.map(siteEntity, new TypeToken<List<SiteFilterDTO>>() {
        }.getType()));
    }


    /**
     * The function returns a ResponseEntity containing a list of SiteFilterDTO objects mapped from a list of sites found
     * by organization ID.
     *
     * @return The method is returning a ResponseEntity object containing a list of objects.
     */
    @Override
    public ResponseEntity<List<?>> findAllByOrganization() {
        return ResponseEntity.ok(mapper.map(siteService.findAllByOrganizationId(SecurityUtils.getOrgId()), new TypeToken<List<SiteFilterDTO>>() {
        }.getType()));
    }
}
