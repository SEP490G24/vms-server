package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.service.ICustomerService;
import fpt.edu.capstone.vms.persistence.service.generic.GenericServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service

public class CustomerServiceImpl extends GenericServiceImpl<Customer, UUID> implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final ModelMapper mapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, ModelMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.init(customerRepository);
    }

    @Override
    public Customer create(ICustomerController.CreateCustomerDto createCustomerDto) {
        return customerRepository.save(mapper.map(createCustomerDto, Customer.class));
    }
}
