package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface IDepartmentService extends IGenericService<Department, UUID> {

    Department createDepartment(IDepartmentController.createDepartmentInfo departmentInfo);

    Page<Department> filter(Pageable pageable,
                      List<String> names,
                            UUID siteId,
                            LocalDateTime createdOnStart,
                            LocalDateTime createdOnEnd,
                            String createBy,
                            String lastUpdatedBy,
                            Boolean enable,
                            String keyword);

    List<Department> filter(
                            List<String> names,
                            UUID siteId,
                            LocalDateTime createdOnStart,
                            LocalDateTime createdOnEnd,
                            String createBy,
                            String lastUpdatedBy,
                            Boolean enable,
                            String keyword);
}
