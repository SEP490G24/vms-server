package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.IDepartmentController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Department;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.DepartmentRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.UserRepository;
import fpt.edu.capstone.vms.persistence.service.IDepartmentService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fpt.edu.capstone.vms.constants.ErrorApp.DEPARTMENT_CAN_NOT_DELETE;
import static fpt.edu.capstone.vms.constants.ErrorApp.DEPARTMENT_NOT_FOUND;
import static fpt.edu.capstone.vms.constants.ErrorApp.OBJECT_NOT_EMPTY;
import static fpt.edu.capstone.vms.constants.ErrorApp.SITE_NOT_NULL;
import static fpt.edu.capstone.vms.constants.ErrorApp.USER_NOT_PERMISSION;

@Service
public class DepartmentServiceImpl extends GenericServiceImpl<Department, UUID> implements IDepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper mapper;
    private final SiteRepository siteRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private static final String DEPARTMENT_TABLE_NAME = "Department";


    public DepartmentServiceImpl(DepartmentRepository departmentRepository, ModelMapper mapper, SiteRepository siteRepository, AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.mapper = mapper;
        this.siteRepository = siteRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
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
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Department update(Department updateDepartmentInfo, UUID id) {

        var department = departmentRepository.findById(id).orElse(null);
        var departmentOld = department;
        if (ObjectUtils.isEmpty(department))
            throw new CustomException(DEPARTMENT_NOT_FOUND);
        var site = siteRepository.findById(department.getSiteId()).orElse(null);

        if (!updateDepartmentInfo.getEnable()) {
            if (userRepository.existsByDepartmentId(id)) {
                throw new CustomException(ErrorApp.DEPARTMENT_CAN_NOT_DISABLE);
            }
        }
        var departmentUpdate = departmentRepository.save(department.update(updateDepartmentInfo));
        auditLogRepository.save(new AuditLog(site.getId().toString()
            , site.getOrganizationId().toString()
            , department.getId().toString()
            , DEPARTMENT_TABLE_NAME
            , Constants.AuditType.UPDATE
            , departmentOld.toString()
            , departmentUpdate.toString()));
        return department;
    }

    @Override
    public List<IDepartmentController.DepartmentFilterDTO> FindAllBySiteId(String siteId) {

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new CustomException(USER_NOT_PERMISSION);
        }
        var departments = departmentRepository.findAllBySiteId(UUID.fromString(siteId));
        return mapper.map(departments, new TypeToken<List<IDepartmentController.DepartmentFilterDTO>>() {
        }.getType());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public void deleteDepartment(UUID id) {
        var department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            throw new CustomException(DEPARTMENT_CAN_NOT_DELETE);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, department.getSiteId().toString())) {
            throw new CustomException(USER_NOT_PERMISSION);
        }
        if (userRepository.existsByDepartmentId(id)) {
            throw new CustomException(ErrorApp.DEPARTMENT_IS_USING_CAN_NOT_DELETE);
        }
        var site = siteRepository.findById(department.getSiteId()).orElse(null);
        auditLogRepository.save(new AuditLog(site.getId().toString()
            , site.getOrganizationId().toString()
            , department.getId().toString()
            , DEPARTMENT_TABLE_NAME
            , Constants.AuditType.UPDATE
            , department.toString()
            , null));
        departmentRepository.delete(department);
    }

    @Override
    public IDepartmentController.DepartmentFilterDTO findByDepartmentId(UUID id) {
        var department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            throw new CustomException(DEPARTMENT_NOT_FOUND);
        }
        if (!SecurityUtils.checkSiteAuthorization(siteRepository, department.getSiteId().toString())) {
            throw new CustomException(USER_NOT_PERMISSION);
        }
        return mapper.map(department, IDepartmentController.DepartmentFilterDTO.class);
    }

    /**
     * This Java function creates a department based on the provided department information, with various checks and
     * validations.
     *
     * @param departmentInfo The parameter `departmentInfo` is an object of type
     *                       `IDepartmentController.CreateDepartmentInfo`. It contains information required to create a department, such as the
     *                       site ID and department code.
     * @return The method is returning a Department object.
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public Department createDepartment(IDepartmentController.CreateDepartmentInfo departmentInfo) {

        if (ObjectUtils.isEmpty(departmentInfo))
            throw new CustomException(OBJECT_NOT_EMPTY);

        if (SecurityUtils.getOrgId() != null) {
            if (!SecurityUtils.checkSiteAuthorization(siteRepository, departmentInfo.getSiteId().toString())) {
                throw new CustomException(USER_NOT_PERMISSION);
            }
        } else {
            departmentInfo.setSiteId(SecurityUtils.getSiteId());
        }

        if (StringUtils.isEmpty(departmentInfo.getCode())) {
            throw new CustomException(ErrorApp.OBJECT_CODE_NULL);
        }

        UUID siteId = UUID.fromString(departmentInfo.getSiteId());

        var site = siteRepository.findById(siteId).orElse(null);

        if (ObjectUtils.isEmpty(site)) {
            throw new CustomException(SITE_NOT_NULL);
        }

        if (SecurityUtils.getUserDetails().isOrganizationAdmin()) {
            if (departmentRepository.existsByCodeAndSiteId(departmentInfo.getCode(), UUID.fromString(departmentInfo.getSiteId()))) {
                throw new CustomException(ErrorApp.DEPARTMENT_DUPLICATE);
            }
        } else if (SecurityUtils.getUserDetails().isSiteAdmin()) {
            if (departmentRepository.existsByCodeAndSiteId(departmentInfo.getCode(), UUID.fromString(SecurityUtils.getSiteId()))) {
                throw new CustomException(ErrorApp.DEPARTMENT_DUPLICATE);
            }
        }


        var department = mapper.map(departmentInfo, Department.class);
        department.setEnable(true);
        var departmentCreate = departmentRepository.save(department);
        auditLogRepository.save(new AuditLog(siteId.toString()
            , site.getOrganizationId().toString()
            , departmentCreate.getId().toString()
            , DEPARTMENT_TABLE_NAME
            , Constants.AuditType.CREATE
            , null
            , departmentCreate.toString()));
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
    public Page<Department> filter(Pageable pageable, List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteId);
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        return departmentRepository.filter(
            pageableSort,
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword == null ? null : keyword.toUpperCase());
    }

    /**
     * The function filters a list of departments based on various criteria such as names, site IDs, creation dates,
     * creators, last updaters, enable status, and keywords.
     *
     * @param names          A list of department names to filter by.
     * @param siteId         A list of site IDs to filter the departments by.
     * @param createdOnStart The start date and time for filtering departments based on their creation date.
     * @param createdOnEnd   The "createdOnEnd" parameter is a LocalDateTime object that represents the end date and time for
     *                       filtering departments based on their creation date.
     * @param createBy       The "createBy" parameter is a string that represents the user who created the department. It is used
     *                       as a filter criterion to search for departments created by a specific user.
     * @param lastUpdatedBy  The parameter "lastUpdatedBy" is a String that represents the username of the user who last
     *                       updated the department.
     * @param enable         The "enable" parameter is a boolean value that indicates whether the department is enabled or not. If
     *                       it is set to true, it means the department is enabled. If it is set to false, it means the department is disabled.
     * @param keyword        The "keyword" parameter is a string that is used to filter the departments based on a specific
     *                       keyword. It can be used to search for departments that have a specific name, description, or any other relevant
     *                       information.
     * @return The method is returning a List of Department objects.
     */
    @Override
    public List<Department> filter(List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        List<UUID> sites = SecurityUtils.getListSiteToUUID(siteRepository, siteId);
        return departmentRepository.filter(
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword == null ? null : keyword.toUpperCase());
    }

}
