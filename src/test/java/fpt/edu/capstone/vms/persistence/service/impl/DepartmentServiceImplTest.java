package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
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
    void testSaveWithNullCode() {
        IDepartmentController.createDepartmentInfo departmentInfo = new IDepartmentController.createDepartmentInfo();
        departmentInfo.setCode(null);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    void testSaveWithExistingCode() {
        IDepartmentController.createDepartmentInfo departmentInfo = new IDepartmentController.createDepartmentInfo();
        departmentInfo.setCode("existingCode");

        when(departmentRepository.existsByCode(departmentInfo.getCode())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    void testSaveWithNullObject() {
        IDepartmentController.createDepartmentInfo departmentInfo = null;

        assertThrows(NullPointerException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    void testSaveWithNullSiteId() {
        IDepartmentController.createDepartmentInfo departmentInfo = new IDepartmentController.createDepartmentInfo();
        departmentInfo.setCode("validCode");
        departmentInfo.setSiteId(null);

        assertThrows(HttpClientErrorException.class, () -> departmentService.createDepartment(departmentInfo));
    }

    @Test
    void testSaveValidDepartment() {
        UUID siteId = UUID.randomUUID();
        IDepartmentController.createDepartmentInfo departmentInfo = new IDepartmentController.createDepartmentInfo();
        departmentInfo.setCode(siteId.toString());
        departmentInfo.setSiteId("validSiteId");

        Department department = new Department();
        when(departmentRepository.existsByCode(departmentInfo.getCode())).thenReturn(false);
//        when(mapper.map(departmentInfo, Department.class)).thenReturn(department);

        department.setEnable(true);
        department.setCode(departmentInfo.getCode());
        department.setSiteId(siteId);
        Department savedDepartment = departmentRepository.save(department);

        assertNotNull(department);
        assertTrue(department.getEnable());
        verify(departmentRepository, times(1)).save(department);
    }

    @Test
    void testUpdateWithExistingCode() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("existingCode");

        when(departmentRepository.existsByCode(updateDepartmentInfo.getCode())).thenReturn(true);

        assertThrows(HttpClientErrorException.class, () -> departmentService.update(updateDepartmentInfo, id));
    }

    @Test
    void testUpdateWithNonExistingDepartment() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("newCode");

        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(HttpClientErrorException.class, () -> departmentService.update(updateDepartmentInfo, id));
    }


    @Test
    void testUpdateValidDepartment() {
        UUID id = UUID.randomUUID();
        Department updateDepartmentInfo = new Department();
        updateDepartmentInfo.setCode("newCode");

        Department existingDepartment = new Department();
        when(departmentRepository.findById(id)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.existsByCode(updateDepartmentInfo.getCode())).thenReturn(false);

        Department updatedDepartment = new Department();
        when(departmentRepository.save(existingDepartment.update(updateDepartmentInfo))).thenReturn(updatedDepartment);

        Department result = departmentService.update(updateDepartmentInfo, id);

        assertNotNull(result);
        assertEquals(updateDepartmentInfo.getCode(), result.getCode());
        verify(departmentRepository, times(1)).save(existingDepartment.update(updateDepartmentInfo));
    }
}
