package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IPermissionController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    IPermissionResource permissionResource;

    @InjectMocks
    PermissionService permissionService;

    @Test
    void findAllModules() {
        permissionService.findAllModules(true);
        Mockito.verify(permissionResource, Mockito.times(1)).findAllModules(true);
    }

    @Test
    void findAllByModuleId() {
        permissionService.findAllByModuleId("1");
        Mockito.verify(permissionResource, Mockito.times(1)).findAllByModuleId("1");
    }

    @Test
    void findById() {
        permissionService.findById("1", "1");
        Mockito.verify(permissionResource, Mockito.times(1)).findById("1", "1");
    }

    @Test
    void create() {
        permissionService.create("1", null);
        Mockito.verify(permissionResource, Mockito.times(1)).create("1", null);
    }

    @Test
    void update() throws NotFoundException {
        permissionService.update("1", "1", null);
        Mockito.verify(permissionResource, Mockito.times(1)).update("1", "1", null);
    }

    @Test
    void updateAttribute() {
        permissionService.updateAttribute("1", null, null);
        Mockito.verify(permissionResource, Mockito.times(1)).updateAttribute("1", null, null);
    }

    @Test
    void delete() {
        permissionService.delete("1", "1");
        Mockito.verify(permissionResource, Mockito.times(1)).delete("1", "1");
    }

    @Test
    void filter() {
        IPermissionController.PermissionFilterPayload filterPayload = new IPermissionController.PermissionFilterPayload();
        filterPayload.setName("1");
        permissionService.filter(filterPayload);
        Mockito.verify(permissionResource, Mockito.times(1)).filter(filterPayload);
    }
}
