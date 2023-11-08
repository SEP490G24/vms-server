package fpt.edu.capstone.vms.controller.impl;

import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.exception.HttpClientResponse;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.service.IDepartmentService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
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

    /**
     * The function returns a ResponseEntity containing the Department object with the specified UUID.
     *
     * @param id The parameter "id" is of type UUID, which stands for Universally Unique Identifier. It is a 128-bit value
     * used to uniquely identify information in computer systems. In this case, it is used to find a specific department by
     * its unique identifier.
     * @return The method is returning a ResponseEntity object containing a Department object.
     */
    @Override
    public ResponseEntity<Department> findById(UUID id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    /**
     * The function deletes a department with the specified ID and returns a ResponseEntity containing the deleted
     * department.
     *
     * @param id The id parameter is of type UUID, which stands for Universally Unique Identifier. It is a 128-bit value
     * used to uniquely identify an object or entity in a distributed computing environment. In this case, it is used to
     * identify the department that needs to be deleted.
     * @return The method is returning a ResponseEntity object with a generic type of Department.
     */
    @Override
    public ResponseEntity<Department> delete(UUID id) {
        return departmentService.delete(id);
    }

    /**
     * The function returns a ResponseEntity containing a list of all departments.
     *
     * @return The method is returning a ResponseEntity object containing a List of objects.
     */
    @Override
    public ResponseEntity<List<?>> findAll() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    /**
     * The function creates a department and returns a ResponseEntity with the created department or an error message.
     *
     * @param departmentInfo The parameter `departmentInfo` is of type `createDepartmentInfo`. It is an object that
     * contains information about the department that needs to be created. The specific structure and properties of the
     * `createDepartmentInfo` object would depend on the implementation of the `createDepartment` method in the
     * `departmentService
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> createDepartment(CreateDepartmentInfo departmentInfo) {
        try {
            var department = departmentService.createDepartment(departmentInfo);
            return ResponseEntity.ok(department);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    /**
     * The function updates a department with the given information and ID, and returns a response entity with the updated
     * department or an error message.
     *
     * @param updateInfo The updateInfo parameter is an object of type updateDepartmentInfo. It contains the information
     * needed to update a department, such as the department name, description, or any other relevant details.
     * @param id The "id" parameter is a UUID (Universally Unique Identifier) that represents the unique identifier of the
     * department that needs to be updated.
     * @return The method is returning a ResponseEntity object.
     */
    @Override
    public ResponseEntity<?> updateDepartment(UpdateDepartmentInfo updateInfo, UUID id) {
        try {
            var department = departmentService.update(mapper.map(updateInfo, Department.class), id);
            return ResponseEntity.ok(department);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new HttpClientResponse(e.getMessage()));
        }
    }

    /**
     * The function filters department entities based on the provided filter criteria and returns the result as a
     * ResponseEntity.
     *
     * @param filter The `filter` parameter is an object of type `DepartmentFilter` which contains various filter criteria
     * for the department entities.
     * @param isPageable A boolean value indicating whether the results should be pageable or not.
     * @param pageable The `pageable` parameter is an object of type `Pageable` which is used for pagination. It contains
     * information about the current page number, page size, sorting criteria, etc. It is used to retrieve a specific page
     * of results from the filtered departments.
     * @return The method is returning a ResponseEntity object. The response entity contains either a list of
     * DepartmentFilterDTO objects or a PageImpl object, depending on the value of the isPageable parameter.
     */
    @Override
    public ResponseEntity<?> filter(DepartmentFilter filter, boolean isPageable, Pageable pageable) {
        var departmentEntity = departmentService.filter(
            filter.getNames(),
            filter.getSiteIds(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getEnable(),
            filter.getKeyword());

        var departmentEntityPageable = departmentService.filter(
            pageable,
            filter.getNames(),
            filter.getSiteIds(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getEnable(),
            filter.getKeyword());

        List<DepartmentFilterDTO> departmentFilterDTOS = mapper.map(departmentEntityPageable.getContent(), new TypeToken<List<DepartmentFilterDTO>>() {
        }.getType());

        return isPageable ? ResponseEntity.ok(new PageImpl(departmentFilterDTOS, pageable, departmentFilterDTOS.size()))
            : ResponseEntity.ok(mapper.map(departmentEntity, new TypeToken<List<DepartmentFilterDTO>>() {
        }.getType()));
    }
}
