package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.service.ICustomerService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import fpt.edu.capstone.vms.util.SecurityUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service

public class CustomerServiceImpl extends GenericServiceImpl<Customer, UUID> implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final ModelMapper mapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, SiteRepository siteRepository, ModelMapper mapper) {
        this.customerRepository = customerRepository;
        this.siteRepository = siteRepository;
        this.mapper = mapper;
        this.init(customerRepository);
    }

    @Override
    public Customer create(ICustomerController.NewCustomers createCustomerDto) {
        return customerRepository.save(mapper.map(createCustomerDto, Customer.class));
    }

    @Override
    public Page<Customer> filter(Pageable pageable, List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, String identificationNumber, String keyword) {
        return customerRepository.filter(
            pageable,
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            identificationNumber,
            keyword);
    }

    @Override
    public List<Customer> filter(List<String> names, LocalDateTime createdOnStart, LocalDateTime createdOnEnd, String createBy, String lastUpdatedBy, String identificationNumber, String keyword) {
        return customerRepository.filter(
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            identificationNumber,
            keyword);
    }

    @Override
    public List<Customer> findAllByOrganizationId(ICustomerController.CustomerAvailablePayload customerAvailablePayload) {
        String orgId;
        if (SecurityUtils.getOrgId() == null) {
            Site site = siteRepository.findById(UUID.fromString(SecurityUtils.getSiteId())).orElse(null);
            if (ObjectUtils.isEmpty(site)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "site is flase");
            }
            orgId = String.valueOf(site.getOrganizationId());
        } else {
            orgId = SecurityUtils.getOrgId();
        }
        return customerRepository.findAllByOrganizationId(orgId, customerAvailablePayload.getStartTime(), customerAvailablePayload.getEndTime());
    }
}
