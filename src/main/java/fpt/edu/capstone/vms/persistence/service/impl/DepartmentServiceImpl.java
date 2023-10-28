package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.IDepartmentService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class DepartmentServiceImpl extends GenericServiceImpl<Department, UUID> implements IDepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper mapper;
    private final SiteRepository siteRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, ModelMapper mapper, SiteRepository siteRepository) {
        this.departmentRepository = departmentRepository;
        this.mapper = mapper;
        this.siteRepository = siteRepository;
        this.init(departmentRepository);
    }

    /**
     * The function updates a department's information and returns the updated department.
     *
     * @param updateDepartmentInfo The updateDepartmentInfo parameter is an object that contains the updated information
     * for a department. It likely includes properties such as code, name, description, etc.
     * @param id The `id` parameter is a `UUID` (Universally Unique Identifier) that represents the unique identifier of
     * the department that needs to be updated.
     * @return The method is returning a Department object.
     */
    @Override
    public Department update(Department updateDepartmentInfo, UUID id) {

        if (!StringUtils.isEmpty(updateDepartmentInfo.getCode())) {
            if (departmentRepository.existsByCode(updateDepartmentInfo.getCode())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of department is exist");
            }
        }

        var department = departmentRepository.findById(id).orElse(null);

        if (ObjectUtils.isEmpty(department))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Can't found department");

        String siteId = department.getSiteId().toString();

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Can't create department in this site");
        }

        departmentRepository.save(department.update(updateDepartmentInfo));
        return department;
    }

    /**
     * This Java function creates a department based on the provided department information, with various checks and
     * validations.
     *
     * @param departmentInfo The parameter `departmentInfo` is an object of type
     * `IDepartmentController.CreateDepartmentInfo`. It contains information required to create a department, such as the
     * site ID and department code.
     * @return The method is returning a Department object.
     */
    @Override
    @Transactional
    public Department createDepartment(IDepartmentController.CreateDepartmentInfo departmentInfo) {

        if (StringUtils.isEmpty(departmentInfo.getSiteId()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");

        String siteId = departmentInfo.getSiteId();

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Can't update department in this site");
        }

        if (StringUtils.isEmpty(departmentInfo.getCode())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code is null");
        }

        if (departmentRepository.existsByCode(departmentInfo.getCode())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "The Code of department is exist");
        }

        if (ObjectUtils.isEmpty(departmentInfo))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Object is empty");
        var department = mapper.map(departmentInfo, Department.class);
        department.setEnable(true);
        departmentRepository.save(department);
        return department;
    }

    /**
     * The function filters departments based on various criteria and returns a paginated result.
     *
     * @param pageable The pageable parameter is used for pagination and sorting. It allows you to specify the page number,
     * page size, and sorting criteria for the results.
     * @param names A list of department names to filter by.
     * @param siteId The siteId parameter is a UUID (Universally Unique Identifier) that represents the unique identifier
     * of a site. It is used to filter departments based on the site they belong to.
     * @param createdOnStart The start date and time for filtering departments based on their creation date.
     * @param createdOnEnd The "createdOnEnd" parameter is used to specify the end date and time for filtering departments
     * based on their creation date. It is a LocalDateTime object that represents the date and time when the department was
     * created.
     * @param createBy The "createBy" parameter is used to filter departments based on the user who created them.
     * @param lastUpdatedBy The "lastUpdatedBy" parameter is used to filter departments based on the user who last updated
     * them. It is a string parameter that represents the username or ID of the user who last updated the departments.
     * @param enable The "enable" parameter is a boolean value that indicates whether the department is enabled or
     * disabled. If the value is true, it means the department is enabled. If the value is false, it means the department
     * is disabled.
     * @param keyword The "keyword" parameter is a string that can be used to search for a specific keyword in the
     * department names or any other relevant fields.
     * @return The method is returning a Page object containing a list of Department objects.
     */
    @Override
    public Page<Department> filter(Pageable pageable, List<String> names, UUID siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return departmentRepository.filter(
            pageable,
            names,
            siteId,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }

    /**
     * The function filters a list of departments based on various criteria such as names, site ID, creation date, creator,
     * last updater, enable status, and keyword.
     *
     * @param names A list of department names to filter by.
     * @param siteId The siteId parameter is a unique identifier for a specific site or location. It is used to filter
     * departments based on the site they belong to.
     * @param createdOnStart The parameter "createdOnStart" is a LocalDateTime object that represents the start date and
     * time for filtering departments based on their creation date.
     * @param createdOnEnd The "createdOnEnd" parameter is used to specify the end date and time for filtering departments
     * based on their creation date. It is a LocalDateTime object that represents the date and time in the format
     * "yyyy-MM-dd HH:mm:ss".
     * @param createBy The "createBy" parameter is used to filter the departments based on the user who created them. It is
     * a string that represents the username or ID of the user who created the departments.
     * @param lastUpdatedBy The `lastUpdatedBy` parameter is used to filter the departments based on the user who last
     * updated them. It is a string that represents the username or ID of the user.
     * @param enable The "enable" parameter is a boolean value that indicates whether the department is enabled or not. If
     * it is set to true, it means that the department is enabled. If it is set to false, it means that the department is
     * disabled.
     * @param keyword The "keyword" parameter is used to search for departments that contain a specific keyword in their
     * name or description.
     * @return The method is returning a List of Department objects.
     */
    @Override
    public List<Department> filter( List<String> names, UUID siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        return departmentRepository.filter(
            names,
            siteId,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }
}
