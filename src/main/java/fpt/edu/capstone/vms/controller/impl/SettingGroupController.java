package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.ISettingGroupController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.SettingGroup;
import fpt.edu.capstone.vms.persistence.service.ISettingGroupService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

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
     * be found.
     * @return The method is returning a ResponseEntity object containing a SettingGroup object.
     */
    @Override
    public ResponseEntity<SettingGroup> findById(Long id) {
        return ResponseEntity.ok(settingGroupService.findById(id));
    }

    /**
     * The function deletes a setting group with the specified ID and returns a ResponseEntity containing the deleted
     * setting group.
     *
     * @param id The id parameter is of type Long and represents the unique identifier of the setting group that needs to
     * be deleted.
     * @return The method is returning a ResponseEntity object with a generic type of SettingGroup.
     */
    @Override
    public ResponseEntity<SettingGroup> delete(Long id) {
        return settingGroupService.delete(id);
    }


    /**
     * The function updates a setting group and returns a ResponseEntity with the updated setting group or an
     * HttpClientResponse if an error occurs.
     *
     * @param id The id parameter is of type Long and represents the identifier of the setting group that needs to be
     * updated.
     * @param settingGroupInfo The settingGroupInfo parameter is an object of type UpdateSettingGroupInfo. It contains the
     * updated information for a setting group.
     * @return The method is returning a ResponseEntity object.
     */

    @Override
    public ResponseEntity<?> updateSettingGroup(Long id, UpdateSettingGroupInfo settingGroupInfo) {
        try {
            var settingGroup = settingGroupService.update(mapper.map(settingGroupInfo, SettingGroup.class), id);
            return ResponseEntity.ok(settingGroup);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
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
     * setting group.
     * @return The method is returning a ResponseEntity object.
     */

    @Override
    public ResponseEntity<?> createSettingGroup(CreateSettingGroupInfo settingGroupInfo) {
        var settingGroup = settingGroupService.save(mapper.map(settingGroupInfo, SettingGroup.class));
        return ResponseEntity.ok(settingGroup);
    }


}
