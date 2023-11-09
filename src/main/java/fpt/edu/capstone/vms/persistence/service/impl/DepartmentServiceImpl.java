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
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Override
    public List<IDepartmentController.DepartmentFilterDTO> FindAllBySiteId(String siteId) {

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Not permission");
        }
        var departments = departmentRepository.findAllBySiteId(UUID.fromString(siteId));
        return mapper.map(departments, new TypeToken<List<IDepartmentController.DepartmentFilterDTO>>() {
        }.getType());
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
    @Transactional
    public Department createDepartment(IDepartmentController.CreateDepartmentInfo departmentInfo) {

        if (StringUtils.isEmpty(departmentInfo.getSiteId().toString()))
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "SiteId is null");

        UUID siteId = departmentInfo.getSiteId();

        if (!SecurityUtils.checkSiteAuthorization(siteRepository, siteId.toString())) {
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
    public Page<Department> filter(Pageable pageable, List<String> names, List<String> siteId, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, Boolean enable, String keyword) {
        List<UUID> sites = getListSite(siteId);
        return departmentRepository.filter(
            pageable,
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
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
        List<UUID> sites = getListSite(siteId);
        return departmentRepository.filter(
            names,
            sites,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            enable,
            keyword);
    }

    private List<UUID> getListSite(List<String> siteId) {

        if (SecurityUtils.getOrgId() == null && siteId != null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You don't have permission to do this.");
        }
        List<UUID> sites = new ArrayList<>();
        if (SecurityUtils.getOrgId() != null) {
            if (siteId == null) {
                siteRepository.findAllByOrganizationId(UUID.fromString(SecurityUtils.getOrgId())).forEach(o -> {
                    sites.add(o.getId());
                });
            } else {
                siteId.forEach(o -> {
                    if (!SecurityUtils.checkSiteAuthorization(siteRepository, o)) {
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "You don't have permission to do this.");
                    }
                    sites.add(UUID.fromString(o));
                });
            }
        } else {
            sites.add(UUID.fromString(SecurityUtils.getSiteId()));
        }

        return sites;
    }
}
