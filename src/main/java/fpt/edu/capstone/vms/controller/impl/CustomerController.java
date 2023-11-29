package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.service.ICustomerService;
import fpt.edu.capstone.vms.util.ResponseUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerController implements ICustomerController {

    final ICustomerService customerService;
    final ModelMapper mapper;

    @Override
    public ResponseEntity<Customer> findById(UUID id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @Override
    public ResponseEntity<?> create(NewCustomers createCustomerDto) {
        return ResponseEntity.ok(customerService.create(createCustomerDto));
    }

    @Override
    public ResponseEntity<?> filter(CustomerFilter filter, boolean isPageable, Pageable pageable) {
        var customerEntity = customerService.filter(
            filter.getNames(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getIdentificationNumber(),
            filter.getKeyword());
        var customerEntityPageable = customerService.filter(
            pageable,
            filter.getNames(),
            filter.getCreatedOnStart(),
            filter.getCreatedOnEnd(),
            filter.getCreateBy(),
            filter.getLastUpdatedBy(),
            filter.getIdentificationNumber(),
            filter.getKeyword());


        List<CustomerInfo> customerInfos = mapper.map(customerEntityPageable.getContent(), new TypeToken<List<CustomerInfo>>() {
        }.getType());

        return isPageable ?
            ResponseEntity.ok(new PageImpl(customerInfos, pageable, customerInfos.size()))
            : ResponseEntity.ok(mapper.map(customerEntity, new TypeToken<List<CustomerInfo>>() {
        }.getType()));
    }

    @Override
    public ResponseEntity<?> findByOrganizationId(CustomerAvailablePayload customerAvailablePayload) {
        try {
            return ResponseUtils.getResponseEntityStatus(customerService.findAllByOrganizationId(customerAvailablePayload));
        } catch (CustomException e) {
            return ResponseUtils.getResponseEntity(e.getErrorApp(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
