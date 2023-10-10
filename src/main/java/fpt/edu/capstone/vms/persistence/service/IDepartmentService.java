package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.UUID;


public interface IDepartmentService extends IGenericService<Department, UUID> {

    Department createDepartment(IDepartmentController.createDepartmentInfo departmentInfo);
}
