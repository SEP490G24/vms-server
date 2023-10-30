package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;

import java.util.UUID;


public interface ICustomerService extends IGenericService<Customer, UUID> {

    Customer create(ICustomerController.NewCustomers createCustomerDto);
}
