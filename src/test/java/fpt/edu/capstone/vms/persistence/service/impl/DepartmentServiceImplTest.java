package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;



class DepartmentServiceImplTest {

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ModelMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void update() {
    }

    @Test
    @DisplayName("given incomplete data, when department with null code, then exception is thrown")
    void givenDepartment_WhenSaveWithNullCode_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode(null);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when department with existing siteId, then exception is thrown")
    void givenDepartment_WhenSaveWithExistingCode_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode("existingCode");

        when(departmentRepository.existsByCode(departmentInfo.getCode())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when department with null object, then exception is thrown")
    void givenDepartment_WhenSaveWithNullObject_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = null;

        assertThrows(NullPointerException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    @DisplayName("given incomplete data, when department with null siteId, then exception is thrown")
    void givenDepartment_WhenSaveWithNullSiteId_ThenThrowException() {
        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
        departmentInfo.setCode("validCode");
        departmentInfo.setSiteId(null);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

//    @Test
//    @DisplayName("given incomplete data, when create new department, then department is save")
//    void givenDepartment_WhenSaveValidDepartment_ThenCreateNewDepartment() {
//        UUID siteId = UUID.randomUUID();
//        IDepartmentController.CreateDepartmentInfo departmentInfo = new IDepartmentController.CreateDepartmentInfo();
//        departmentInfo.setCode(siteId.toString());
//        departmentInfo.setSiteId("validSiteId");
//
//        Department department = new Department();
//        when(departmentRepository.existsByCode(departmentInfo.getCode())).thenReturn(false);
////        when(mapper.map(departmentInfo, Department.class)).thenReturn(department);
//
//        department.setEnable(true);
//        department.setCode(departmentInfo.getCode());
//        department.setSiteId(siteId);
//        Department savedDepartment = departmentRepository.save(department);
//
//        assertNotNull(department);
//        assertTrue(department.getEnable());
//        verify(departmentRepository, times(1)).save(department);
//    }

    @Test
    @DisplayName("given incomplete data, when update department with existing code, then exception is thrown")
    void givenDepartmentId_WhenUpdateWithExistingCode_ThenThrowException() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("existingCode");

        when(departmentRepository.existsByCode(updateDepartmentInfo.getCode())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> departmentService.update(updateDepartmentInfo, id));
    }

    @Test
    @DisplayName("given incomplete data, when update department with non existing department, then exception is thrown")
    void givenDepartmentId_WhenUpdateWithNonExistingDepartment_ThenThrowException() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("newCode");

        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> departmentService.update(updateDepartmentInfo, id));
    }


//    @Test
//    void givenDepartmentId_WhenUpdateValidDepartment_ThenUpdateDepartment() {
//        UUID id = UUID.randomUUID();
//        Department updateDepartmentInfo = new Department();
//        updateDepartmentInfo.setCode("newCode");
//
//        Department existingDepartment = new Department();
//        when(departmentRepository.findById(id)).thenReturn(Optional.of(existingDepartment));
//        when(departmentRepository.existsByCode(updateDepartmentInfo.getCode())).thenReturn(false);
//
//        Department updatedDepartment = new Department();
//        when(departmentRepository.save(existingDepartment.update(updateDepartmentInfo))).thenReturn(updatedDepartment);
//
//        Department result = departmentService.update(updateDepartmentInfo, id);
//
//        assertNotNull(result);
//        assertEquals(updateDepartmentInfo.getCode(), result.getCode());
//        verify(departmentRepository, times(1)).save(existingDepartment.update(updateDepartmentInfo));
//    }
}
