package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ISettingController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Setting;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ISettingService;
import fpt.edu.capstone.vms.util.ResponseUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class SettingController implements ISettingController {

    private final ISettingService settingService;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;


    /**
     * The function updates a setting group with the provided ID and returns a ResponseEntity with the updated setting or
     * an error message.
     *
     * @param id          The "id" parameter is of type Long and represents the identifier of the setting group that needs to be
     *                    updated.
     * @param settingInfo The settingInfo parameter is an object of type UpdateSettingInfo. It contains the updated
     *                    information for a setting.
     * @return The method is returning a ResponseEntity object.
     */

    @Override
    public ResponseEntity<?> updateSettingGroup(Long id, UpdateSettingInfo settingInfo) {
        try {
            if (SecurityUtils.getUserDetails().isRealmAdmin()) {
                var setting = settingService.update(mapper.map(settingInfo, Setting.class), id);
                return ResponseUtils.getResponseEntityStatus(setting);
            } else {
                return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The function returns a ResponseEntity containing a list of all settings.
     *
     * @return The method is returning a ResponseEntity object containing a List of unknown type.
     */
    /**
     * The function returns a ResponseEntity containing a list of settings based on the provided groupId and siteId, with
     * authorization checks and filtering based on user details.
     *
     * @param groupId The groupId parameter is an Integer that represents the group ID. It is used to filter the settings
     *                based on the group ID. If the groupId is null, all settings will be returned regardless of the group ID.
     * @param siteId  The `siteId` parameter is a string that represents the ID of a site.
     * @return The method is returning a ResponseEntity object. The response entity contains a list of objects, which can
     * be of any type.
     */
    @Override
    public ResponseEntity<List<?>> findAll(Integer groupId, String siteId) {
        var userDetails = SecurityUtils.getUserDetails();

        var _siteId = userDetails.isOrganizationAdmin() ? siteId : userDetails.getSiteId();

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, _siteId)) {
            ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
        }

        return groupId == null ?
            ResponseEntity.ok(null) : ResponseEntity.ok(settingService.findAllByGroupIdAndSiteId(groupId, _siteId));
    }

    /**
     * The function creates a setting using the provided information and returns a ResponseEntity with the created setting
     * or an HttpClientResponse if an error occurs.
     *
     * @param settingInfo The parameter "settingInfo" is an object of type "CreateSettingInfo". It contains information
     *                    needed to create a new setting.
     * @return The method is returning a ResponseEntity object.
     */

    @Override
    public ResponseEntity<?> createSetting(CreateSettingInfo settingInfo) {
        try {
            if (SecurityUtils.getUserDetails().isRealmAdmin()) {
                var setting = settingService.save(mapper.map(settingInfo, Setting.class));
                return ResponseUtils.getResponseEntityStatus(setting);
            } else {
                return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
