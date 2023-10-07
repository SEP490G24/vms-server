package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.service.impl.DepartmentServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class DepartmentController implements IDepartmentController {
    private final DepartmentServiceImpl departmentService;
    private final ModelMapper mapper;

    @Override
    public ResponseEntity<Department> findById(UUID id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    @Override
    public ResponseEntity<Department> delete(UUID id) {
        return departmentService.delete(id);
    }

    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    @Override
    public ResponseEntity<?> createDepartment(createDepartmentInfo departmentInfo) {
        var department = departmentService.save(mapper.map(departmentInfo, Department.class));
        return ResponseEntity.ok(department);
    }
}