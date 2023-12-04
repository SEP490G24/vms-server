package fpt.edu.capstone.vms.persistence.service;

import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.service.generic.IGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface ICustomerService extends IGenericService<Customer, UUID> {

    Page<Customer> filter(Pageable pageable,
                          List<String> names,
                          LocalDateTime createdOnStart,
                          LocalDateTime createdOnEnd,
                          String createBy,
                          String lastUpdatedBy,
                          String identificationNumber,
                          String keyword);

    List<Customer> filter(
        List<String> names,
        LocalDateTime createdOnStart,
        LocalDateTime createdOnEnd,
        String createBy,
        String lastUpdatedBy,
        String identificationNumber,
        String keyword);

    List<Customer> findAllByOrganizationId(ICustomerController.CustomerAvailablePayload customerAvailablePayload);

    void deleteCustomer(UUID id);

    void checkExistCustomer(ICustomerController.CustomerCheckExist customerCheckExist);
}
