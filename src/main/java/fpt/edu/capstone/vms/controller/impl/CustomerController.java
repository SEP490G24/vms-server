package fpt.edu.capstone.vms.controller.impl;


import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.service.ICustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerController implements ICustomerController {

    final ICustomerService customerService;

    @Override
    public ResponseEntity<Customer> findById(UUID id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @Override
    public ResponseEntity<?> create(NewCustomers createCustomerDto) {
        return ResponseEntity.ok(customerService.create(createCustomerDto));
    }
}
