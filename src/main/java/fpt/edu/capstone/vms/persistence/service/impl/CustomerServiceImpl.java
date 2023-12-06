package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.constants.ErrorApp;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ICustomerService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.PageableUtils;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service

public class CustomerServiceImpl extends GenericServiceImpl<Customer, UUID> implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;
    private final AuditLogRepository auditLogRepository;
    private static final String CUSTOMER_TABLE_NAME = "Customer";


    public CustomerServiceImpl(CustomerRepository customerRepository, SiteRepository siteRepository, ModelMapper mapper, AuditLogRepository auditLogRepository) {
        this.customerRepository = customerRepository;
        this.siteRepository = siteRepository;
        this.mapper = mapper;
        this.auditLogRepository = auditLogRepository;
        this.init(customerRepository);
    }

    @Override
    public Page<Customer> filter(Pageable pageable, List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, String identificationNumber, String keyword) {
        List<Sort.Order> sortColum = new ArrayList<>(PageableUtils.converterSort2List(pageable.getSort()));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.createdOn));
        sortColum.add(new Sort.Order(Sort.Direction.DESC, Constants.lastUpdatedOn));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortColum));
        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }
        return customerRepository.filter(
            pageableSort,
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            orgId,
            lastUpdatedBy,
            identificationNumber,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Customer> filter(List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, String identificationNumber, String keyword) {
        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }
        return customerRepository.filter(
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            orgId,
            lastUpdatedBy,
            identificationNumber,
            keyword != null ? keyword.toUpperCase() : null);
    }

    @Override
    public List<Customer> findAllByOrganizationId(ICustomerController.CustomerAvailablePayload customerAvailablePayload) {
        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }
        return customerRepository.findAllByOrganizationId(orgId, customerAvailablePayload.getStartTime(), customerAvailablePayload.getEndTime());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Throwable.class, Error.class, NullPointerException.class})
    public void deleteCustomer(UUID id) {
        var customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            throw new CustomException(ErrorApp.CUSTOMER_ERROR_IN_PROCESS_DELETE);
        }
        if (!SecurityUtils.checkOrganizationAuthor(siteRepository, customer.getOrganizationId())) {
            throw new CustomException(ErrorApp.USER_NOT_PERMISSION);
        }
        auditLogRepository.save(new AuditLog(null
            , customer.getOrganizationId()
            , customer.getId().toString()
            , CUSTOMER_TABLE_NAME
            , Constants.AuditType.DELETE
            , customer.toString()
            , null));
        customerRepository.deleteById(id);

    }

    @Override
    public void checkExistCustomer(ICustomerController.CustomerCheckExist customerCheckExist) {
        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new CustomException(ErrorApp.SITE_NOT_FOUND);
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }
        switch (customerCheckExist.getType()) {
            case EMAIL:
                if (customerCheckExist.getValue() == null) {
                    throw new CustomException(ErrorApp.CUSTOMER_EMAIL_NOT_FOUND);
                }
                if (customerRepository.existsByEmailAndOrganizationId(customerCheckExist.getValue(), orgId)) {
                    throw new CustomException(ErrorApp.CUSTOMER_EMAIL_EXIST);
                }
            case PHONE_NUMBER:
                if (customerCheckExist.getValue() == null) {
                    throw new CustomException(ErrorApp.CUSTOMER_PHONE_NUMBER_NOT_FOUND);
                }
                if (customerRepository.existsByPhoneNumberAndOrganizationId(customerCheckExist.getValue(), orgId)) {
                    throw new CustomException(ErrorApp.CUSTOMER_PHONE_NUMBER_EXIST);
                }
            case IDENTIFICATION_NUMBER:
                if (customerCheckExist.getValue() == null) {
                    throw new CustomException(ErrorApp.CUSTOMER_IDENTIFICATION_NUMBER_NOT_FOUND);
                }
                if (customerRepository.existsByIdentificationNumberAndOrganizationId(customerCheckExist.getValue(), orgId)) {
                    throw new CustomException(ErrorApp.CUSTOMER_IDENTITY_EXIST);
                }
            default:
        }
    }
}
