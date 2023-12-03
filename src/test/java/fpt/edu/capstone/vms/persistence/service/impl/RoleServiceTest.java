package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IRoleController;
import fpt.edu.capstone.vms.exception.NotFoundException;
import fpt.edu.capstone.vms.oauth2.IPermissionResource;
import fpt.edu.capstone.vms.oauth2.IRoleResource;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    IRoleResource roleResource;

    @Mock
    SiteRepository siteRepository;

    @Mock
    private UserRepository userRepository;

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
        role1.setCode("ROLE_ADMIN");
        IRoleResource.RoleDto role2 = new IRoleResource.RoleDto();
        role2.setCode("ROLE_USER");
        List<IRoleResource.RoleDto> mockRoles = Arrays.asList(role1, role2);

        //when
        when(roleResource.findAll()).thenReturn(mockRoles);
        List<IRoleResource.RoleDto> roles = roleService.findAll();

        //then
        assertEquals(2, roles.size());
        assertEquals("ROLE_ADMIN", roles.get(0).getCode());
        assertEquals("ROLE_USER", roles.get(1).getCode());
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
        roleBasePayload.setCode("MANAGER");
        IRoleResource.RoleDto role1 = new IRoleResource.RoleDto();
        role1.setCode("ORG_MANAGER");
        IRoleResource.RoleDto role2 = new IRoleResource.RoleDto();
        role2.setCode("SITE_MANAGER");
        List<IRoleResource.RoleDto> mockRoles = Arrays.asList(role1, role2);

        when(roleResource.filter(roleBasePayload)).thenReturn(mockRoles);
        List<IRoleResource.RoleDto> roles = roleService.filter(roleBasePayload);

        assertEquals(2, roles.size());
        assertEquals("ORG_MANAGER", roles.get(0).getCode());
        assertEquals("SITE_MANAGER", roles.get(1).getCode());

        Mockito.verify(roleResource, Mockito.times(1)).filter(roleBasePayload);
    }

    @Test
    @DisplayName("given role id, when find existing role, then role are retrieved")
    void givenRoleId_whenFindExistingRole_ThenRoleRetrieved() {

        //given
        String existingRoleId = "123";
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setCode("Test123");

        //when
        when(roleResource.findById(existingRoleId)).thenReturn(roleDto);
        IRoleResource.RoleDto role = roleService.findById(existingRoleId);

        // then
        assertEquals("Test123", role.getCode());
        assertNotNull(role.getCode());
    }

    @Test
    @DisplayName("given roleBasePayload, when filter page role, then roles are retrieved")
    void whenFilterPageableRoles_ThenRolesRetrieved() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        // given
        IRoleController.RoleBasePayload roleBasePayload = new IRoleController.RoleBasePayload();
        roleBasePayload.setCode("MANAGER");
        IRoleResource.RoleDto role1 = new IRoleResource.RoleDto();
        role1.setCode("ORG_MANAGER");
        IRoleResource.RoleDto role2 = new IRoleResource.RoleDto();
        role2.setCode("SITE_MANAGER");

        Page<IRoleResource.RoleDto> mockedPage = new PageImpl<>(List.of());

        when(roleResource.filter(roleBasePayload, pageable)).thenReturn(mockedPage);
        Page<IRoleResource.RoleDto> roles = roleService.filter(roleBasePayload, pageable);

        assertEquals(0, roles.getTotalElements());

        Mockito.verify(roleResource, Mockito.times(1)).filter(roleBasePayload, pageable);
    }

    @Test
    @DisplayName("given role id, when delete existing role, then role are retrieved")
    void givenRoleId_whenDeleteRole_ThenRoleRetrieved() {

        //given
        String existingRoleId = "123";
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setCode("Test123");

        //when
        roleService.delete(existingRoleId);
        Mockito.verify(roleResource, Mockito.times(1)).delete(existingRoleId);

    }

    @Test
    void givenSites_whenGetAllRoleBySites_ThenRoleRetrieved() {

        //given
        String existingRoleId = "123";
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setCode("Test123");

        List<String> sites = new ArrayList<>();
        sites.add("123");

        //when
        List<IRoleResource.RoleDto> roleDtos = roleService.getBySites(sites);

        assertEquals(0, roleDtos.size());
        Mockito.verify(roleResource, Mockito.times(1)).getBySites(sites);

    }

    @Test
    void givenRoleDto_whenCreateRole_ThenRoleRetrieved() {

        //given
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setCode("Test123");

        //when
        IRoleResource.RoleDto roleDto1 = roleService.create(roleDto);
        assertEquals(null, roleDto1);
        Mockito.verify(roleResource, Mockito.times(1)).create(roleDto);

    }

    @Test
    void givenRoleDto_whenUpdateRole_ThenRoleRetrieved() throws NotFoundException {

        //given
        IRoleResource.RoleDto roleDto = new IRoleResource.RoleDto();
        roleDto.setCode("Test123");

        //when
        IRoleResource.RoleDto roleDto1 = roleService.update("abc", roleDto);
        assertEquals(null, roleDto1);
        Mockito.verify(roleResource, Mockito.times(1)).update("abc", roleDto);

    }

    @Test
    void givenRoleDto_whenUpdatePermission_ThenRoleRetrieved() {

        //given
        IPermissionResource.PermissionDto permissionDto = new IPermissionResource.PermissionDto();
        permissionDto.setName("Test123");

        //when
        IRoleResource.RoleDto roleDto1 = roleService.updatePermission("abc", permissionDto, true);
        assertEquals(null, roleDto1);
        Mockito.verify(roleResource, Mockito.times(1)).updatePermission("abc", permissionDto, true);

    }
}
