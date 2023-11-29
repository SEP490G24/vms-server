package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ISettingSiteMapController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.SettingSiteMapPk;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingSiteMapService;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SettingSiteMapController implements ISettingSiteMapController {

    private final ISettingSiteMapService settingSiteService;
    private final ModelMapper mapper;
    private final SiteRepository siteRepository;

    /**
     * The function returns a ResponseEntity containing a SettingSiteMap object found by its siteId and settingId.
     *
     * @param siteId    The siteId parameter is a String that represents the unique identifier of a site. It is used to
     *                  identify a specific site in the system.
     * @param settingId The settingId parameter is of type Long and represents the ID of the setting.
     * @return The method is returning a ResponseEntity object containing a SettingSiteMap object.
     */
    @Override
    public ResponseEntity<?> findById(String siteId, Long settingId) {
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
        }
        SettingSiteMapPk pk = new SettingSiteMapPk(settingId, UUID.fromString(siteId));
        return ResponseEntity.ok(settingSiteService.findById(pk));
    }

    /**
     * The function deletes a setting from a site map using the provided site ID and setting ID.
     *
     * @param siteId    The siteId parameter is a String representing the unique identifier of a site.
     * @param settingId The settingId parameter is a Long value that represents the unique identifier of a setting in the
     *                  system.
     * @return The method is returning a ResponseEntity object with a generic type of SettingSiteMap.
     */
    @Override
    public ResponseEntity<?> delete(String siteId, Long settingId) {
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
        }
        SettingSiteMapPk pk = new SettingSiteMapPk(settingId, UUID.fromString(siteId));
        return settingSiteService.delete(pk);
    }

    /**
     * The function creates or updates a setting site map and returns a ResponseEntity object.
     *
     * @param settingSiteInfo The parameter "settingSiteInfo" is an object of type SettingSiteInfo. It contains information
     *                        related to a setting site map that needs to be created or updated.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> createOrUpdateSettingSiteMap(SettingSiteInfo settingSiteInfo) {
        try {
            return ResponseUtils.getResponseEntityStatus(settingSiteService.createOrUpdateSettingSiteMap(settingSiteInfo));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The function returns a ResponseEntity containing a list of all settings sites.
     *
     * @return The method is returning a ResponseEntity object containing a List of objects.
     */
    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(settingSiteService.findAll());
    }

    /**
     * The function returns a ResponseEntity containing the result of calling the findAllBySiteIdAndGroupId method of the
     * settingSiteService with the given settingGroupId and sites.
     *
     * @param settingGroupId The setting group ID is an integer value that represents the ID of the setting group. It is
     *                       used to filter the settings based on the group they belong to.
     * @param siteId          A siteId to filter the results by.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> findAllByGroupId(Integer settingGroupId, String siteId) {
        List<String> sites = new ArrayList<>();
        if (!StringUtils.isEmpty(siteId)) {
            sites.add(siteId);
        }
        return ResponseEntity.ok(settingSiteService.findAllBySiteIdAndGroupId(settingGroupId, sites));
    }

    /**
     * The function sets the default value for a specific site and returns a ResponseEntity object.
     *
     * @param siteId The siteId parameter is a unique identifier for a specific site. It is used to identify the site for
     *               which the default value needs to be set.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> setDefault(String siteId) {
        try {
            return ResponseUtils.getResponseEntityStatus(settingSiteService.setDefaultValueBySite(siteId));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
