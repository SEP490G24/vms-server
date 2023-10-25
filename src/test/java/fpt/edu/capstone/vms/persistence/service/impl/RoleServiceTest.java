package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    IRoleResource roleResource;

    @Mock
    SiteRepository siteRepository;

    @InjectMocks
    RoleService roleService;

    @BeforeEach
    public void setup() {
        //MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("when list role, then roles are retrieved")
    void whenListRoles_ThenRolesRetrieved() {

        //given
        IRoleResource.RoleDto role1 = new IRoleResource.RoleDto();
        role1.setName("ROLE_ADMIN");
        IRoleResource.RoleDto role2 = new IRoleResource.RoleDto();
        role2.setName("ROLE_USER");
        List<IRoleResource.RoleDto> mockRoles = Arrays.asList(role1, role2);

        //when
        when(roleResource.findAll()).thenReturn(mockRoles);
        List<IRoleResource.RoleDto> roles = roleService.findAll();

        //then
        assertEquals(2, roles.size());
        assertEquals("ROLE_ADMIN", roles.get(0).getName());
        assertEquals("ROLE_USER", roles.get(1).getName());
        assertNotNull(roles);
        assertFalse(roles.isEmpty());

        // Verify
        Mockito.verify(roleResource, Mockito.times(1)).findAll();
    }


    @Test
    @DisplayName("given roleBasePayload, when filter role, then roles are retrieved")
    void whenFilterRoles_ThenRolesRetrieved() {
        // given
        IRoleController.RoleBasePayload roleBasePayload = new IRoleController.RoleBasePayload();
        roleBasePayload.setName("MANAGER");
        IRoleResource.RoleDto role1 = new IRoleResource.RoleDto();
        role1.setName("ORG_MANAGER");
        IRoleResource.RoleDto role2 = new IRoleResource.RoleDto();
        role2.setName("SITE_MANAGER");
        List<IRoleResource.RoleDto> mockRoles = Arrays.asList(role1, role2);

        // then
        when(roleResource.filter(roleBasePayload)).thenReturn(mockRoles);
        List<IRoleResource.RoleDto> roles = roleService.filter(roleBasePayload);

        // Assert the result
        assertEquals(2, roles.size());
        assertEquals("ORG_MANAGER", roles.get(0).getName());
        assertEquals("SITE_MANAGER", roles.get(1).getName());

        // Verify
        Mockito.verify(roleResource, Mockito.times(1)).filter(roleBasePayload);
    }

    @Test
    @DisplayName("given role id, when find existing role, then role are retrieved")
    void givenRoleId_whenFindExistingRole_ThenRoleRetrieved() {

        //given
        String existingRoleId = "123";
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setName("Test123");

        //when
        when(roleResource.findById(existingRoleId)).thenReturn(roleDto);
        IRoleResource.RoleDto role = roleService.findById(existingRoleId);

        // then
        assertEquals("Test123", role.getName());
        assertNotNull(role.getName());
    }

    @Test
    @DisplayName("given role id, when find non existing role, then exception is thrown")
    void givenRoleId_whenFindNonExistingRole_ThenExceptionThrown() {

        //given
        String nonExistingRoleId = "A";
        String errorMsg = "Role Not Found : " + nonExistingRoleId;
        when(roleResource.findById(nonExistingRoleId)).thenThrow(new EntityNotFoundException(errorMsg));

        //when
        EntityNotFoundException throwException = assertThrows(EntityNotFoundException.class, () -> roleService.findById(nonExistingRoleId));

        // then
        assertEquals(errorMsg, throwException.getMessage());
    }


    @Test
    @DisplayName("given role data, when create new Role, then Role id is returned")
    void givenRoleData_whenCreateRole_ThenRoleReturned() {

        //given
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setName("Test");
        roleDto.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        //when
        when(roleResource.create(site, roleDto)).thenReturn(roleDto);
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));

        IRoleResource.RoleDto role = roleService.create(roleDto);

        //then
        assertEquals("Test", role.getName());
    }

//    @Test
//    @DisplayName("given Role incomplete data, when create new Role, then exception is thrown")
//    void givenAdIncompleteData_whenCreateAd_ThenExceptionIsThrown() {
//
//        //given
//        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
//        Site site = new Site();
//        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));
//        String errorMsg = "Unable to save an incomplete entity : " + roleDto;
//
//        //when
//        when(roleResource.create(site, roleDto)).thenThrow(new RuntimeException(errorMsg));
//        RuntimeException throwException = assertThrows(RuntimeException.class, () -> roleService.create(roleDto));
//
//        // then
//        assertEquals(errorMsg, throwException.getMessage());
//    }

    @Test
    @DisplayName("given role id, when update non existing role, then exception is thrown")
    void givenRoleId_whenUpdateNonExistingRole_ThenExceptionThrown() throws NotFoundException {

        //given
        String nonExistingRoleId = "A";
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setName("Test");
        String errorMsg = "Role Not Found : " + nonExistingRoleId;
        when(roleResource.update(nonExistingRoleId, roleDto)).thenThrow(new EntityNotFoundException(errorMsg));

        //when
        EntityNotFoundException throwException = assertThrows(EntityNotFoundException.class, () -> roleService.update(nonExistingRoleId, roleDto));

        // then
        assertEquals(errorMsg, throwException.getMessage());
    }
}
