package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMap;
import fpt.edu.capstone.vms.persistence.entity.SiteDepartmentMapPk;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteDepartmentMapRepository;
import fpt.edu.capstone.vms.persistence.service.IDepartmentService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DepartmentServiceImpl extends GenericServiceImpl<Department, UUID> implements IDepartmentService {

    private final DepartmentRepository departmentRepository;
    private final SiteDepartmentMapRepository siteDepartmentMapRepository;
    private final ModelMapper mapper;


    public DepartmentServiceImpl(DepartmentRepository departmentRepository, SiteDepartmentMapRepository siteDepartmentMapRepository, ModelMapper mapper) {
        this.departmentRepository = departmentRepository;
        this.siteDepartmentMapRepository = siteDepartmentMapRepository;
        this.mapper = mapper;
        this.init(departmentRepository);
    }

    @Override
    public Department update(Department updateDepartmentInfo, UUID id) {
        var department = departmentRepository.findById(id).orElse(null);
        if (ObjectUtils.isEmpty(department))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found department");
        departmentRepository.save(department.update(updateDepartmentInfo));
        return department;
    }

    @Override
    @Transactional
    public Department createDepartment(IDepartmentController.createDepartmentInfo departmentInfo) {
        if (ObjectUtils.isEmpty(departmentInfo))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Object is empty");
        if (StringUtils.isEmpty(departmentInfo.getSiteId()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");
        var department = mapper.map(departmentInfo, Department.class);
        department.setEnable(true);
        departmentRepository.save(department);
        SiteDepartmentMap siteDepartmentMap = new SiteDepartmentMap();
        SiteDepartmentMapPk pk = new SiteDepartmentMapPk(department.getId(), UUID.fromString(departmentInfo.getSiteId()));
        siteDepartmentMap.setId(pk);
        siteDepartmentMapRepository.save(siteDepartmentMap);
        return department;
    }

    @Override
    public Page<Department> filter(int pageNumber, List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return departmentRepository.filter(
            PageRequest.of(pageNumber, Constants.PAGE_SIZE),
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }
}
