package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.controller.ISiteController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.service.IDepartmentService;
import fpt.edu.capstone.vms.persistence.service.impl.DepartmentServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class DepartmentController implements IDepartmentController {
    private final IDepartmentService departmentService;
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
        try {
            var department = departmentService.createDepartment(departmentInfo);
            return ResponseEntity.ok(department);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> updateDepartment(updateDepartmentInfo updateInfo, UUID id) {
        try {
            var site = departmentService.update(mapper.map(updateInfo, Department.class), id);
            return ResponseEntity.ok(site);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> filter(DepartmentFilter filter, boolean isPageable, Pageable pageable) {
        var departmentEntity = departmentService.filter(
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getEnable(),
            filter.getKeyword());

        var departmentEntityPageable = departmentService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteId(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getEnable(),
            filter.getKeyword());

        return isPageable ? ResponseEntity.ok(mapper.map(departmentEntityPageable.getContent(), new TypeToken<List<DepartmentFilterDTO>>() {
        }.getType()))
            : ResponseEntity.ok(mapper.map(departmentEntity, new TypeToken<List<DepartmentFilterDTO>>() {
        }.getType()));
    }
}
