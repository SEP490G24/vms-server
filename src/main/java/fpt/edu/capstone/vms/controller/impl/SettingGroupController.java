package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ISettingGroupController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.SettingGroup;
import fpt.edu.capstone.vms.persistence.service.ISettingGroupService;
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
public class SettingGroupController implements ISettingGroupController {

    private final ISettingGroupService settingGroupService;
    private final ModelMapper mapper;

    /**
     * The function returns a ResponseEntity containing a SettingGroup object found by its id.
     *
     * @param id The parameter "id" is of type Long and represents the unique identifier of the setting group that needs to
     *           be found.
     * @return The method is returning a ResponseEntity object containing a SettingGroup object.
     */
    @Override
    public ResponseEntity<?> findById(Long id) {
        if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            var settingGroup = settingGroupService.findById(id);
            return ResponseEntity.ok(settingGroup);
        } else {
            return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * The function deletes a setting group with the specified ID and returns a ResponseEntity containing the deleted
     * setting group.
     *
     * @param id The id parameter is of type Long and represents the unique identifier of the setting group that needs to
     *           be deleted.
     * @return The method is returning a ResponseEntity object with a generic type of SettingGroup.
     */
    @Override
    public ResponseEntity<?> delete(Long id) {
        if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            return settingGroupService.delete(id);
        } else {
            return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
        }
    }


    /**
     * The function updates a setting group and returns a ResponseEntity with the updated setting group or an
     * HttpClientResponse if an error occurs.
     *
     * @param id               The id parameter is of type Long and represents the identifier of the setting group that needs to be
     *                         updated.
     * @param settingGroupInfo The settingGroupInfo parameter is an object of type UpdateSettingGroupInfo. It contains the
     *                         updated information for a setting group.
     * @return The method is returning a ResponseEntity object.
     */

    @Override
    public ResponseEntity<?> updateSettingGroup(Long id, UpdateSettingGroupInfo settingGroupInfo) {
        try {
            if (SecurityUtils.getUserDetails().isRealmAdmin()) {
                var settingGroup = settingGroupService.update(mapper.map(settingGroupInfo, SettingGroup.class), id);
                return ResponseUtils.getResponseEntityStatus(settingGroup);
            } else {
                return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
            }
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The function returns a ResponseEntity containing a list of all setting groups.
     *
     * @return The method is returning a ResponseEntity object containing a List of unknown type.
     */

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(settingGroupService.findAll());
    }


    /**
     * The function creates a setting group using the provided information and returns a ResponseEntity with the created
     * setting group.
     *
     * @param settingGroupInfo An object of type CreateSettingGroupInfo that contains the information needed to create a
     *                         setting group.
     * @return The method is returning a ResponseEntity object.
     */

    @Override
    public ResponseEntity<?> createSettingGroup(CreateSettingGroupInfo settingGroupInfo) {
        if (SecurityUtils.getUserDetails().isRealmAdmin()) {
            var settingGroup = settingGroupService.save(mapper.map(settingGroupInfo, SettingGroup.class));
            return ResponseEntity.ok(settingGroup);
        } else {
            return ResponseUtils.getResponseEntity(ErrorApp.USER_NOT_PERMISSION, HttpStatus.FORBIDDEN);
        }
    }


}
