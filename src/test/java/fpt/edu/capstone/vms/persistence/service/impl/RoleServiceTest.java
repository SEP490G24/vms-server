package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IRoleController;
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
        when(roleResource.findAll()).thenReturn(mockRoles);

        //when
        List<IRoleResource.RoleDto> roles = roleService.findAll();

        //then
        assertEquals(2, roles.size()); // Check that the list has 2 roles
        assertEquals("ROLE_ADMIN", roles.get(0).getName()); // Check the first role's name
        assertEquals("ROLE_USER", roles.get(1).getName()); // Check the second role's name
        assertNotNull(roles);
        assertFalse(roles.isEmpty());

        // Verify that the roleRepository's findAll method was called
        Mockito.verify(roleResource, Mockito.times(1)).findAll();

    }


    @Test
    void filter() {
        // Mock data
        IRoleController.RoleBasePayload roleBasePayload = new IRoleController.RoleBasePayload();
        roleBasePayload.setName("MANAGER");

        IRoleResource.RoleDto role1 = new IRoleResource.RoleDto();
        role1.setName("ORG_MANAGER");
        IRoleResource.RoleDto role2 = new IRoleResource.RoleDto();
        role2.setName("SITE_MANAGER");
        List<IRoleResource.RoleDto> mockRoles = Arrays.asList(role1, role2);

        // Mock behavior of the roleRepository
        when(roleResource.filter(roleBasePayload)).thenReturn(mockRoles);

        // Call the service method
        List<IRoleResource.RoleDto> roles = roleService.filter(roleBasePayload);

        // Assert the result
        assertEquals(2, roles.size()); // Check that the list has 2 roles
        assertEquals("ORG_MANAGER", roles.get(0).getName()); // Check the first role's name
        assertEquals("SITE_MANAGER", roles.get(1).getName()); // Check the second role's name

        // Verify that the roleRepository's findAll method was called
        Mockito.verify(roleResource, Mockito.times(1)).filter(roleBasePayload);
    }

    @Test
    @DisplayName("given Author id, when delete non existing Author, then exception is thrown")
    void givenAuthorId_whenDeleteNonExistingAuthor_ThenExceptionThrown() {

        //given
        String nonExistingAuthorId = "A";
        String errorMsg = "Author Not Found : " + nonExistingAuthorId;
        when(roleResource.findById(nonExistingAuthorId)).thenThrow(new EntityNotFoundException(errorMsg));

        //when
        EntityNotFoundException throwException = assertThrows(EntityNotFoundException.class, () -> roleService.findById(nonExistingAuthorId));

        // then
        assertEquals(errorMsg, throwException.getMessage());
    }

    @Test
    @DisplayName("given Author data, when create new Author, then Author id is returned")
    void givenAuthorData_whenCreateAuthor_ThenAuthorIdReturned() {

        //given
        IRoleResource.RoleDto authorDto1 = new IRoleResource.RoleDto();
        authorDto1.setName("Test");
        authorDto1.setSiteId("06eb43a7-6ea8-4744-8231-760559fe2c08");
        Site site = new Site();
        site.setId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"));

        //when
        when(roleResource.create(site, authorDto1)).thenReturn(authorDto1);
        IRoleResource.RoleDto authorId1 = roleService.create(authorDto1);

        //then
        assertEquals("te", authorId1.getName());
    }
}
