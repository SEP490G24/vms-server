package fpt.edu.capstone.vms.controller;

import fpt.edu.capstone.vms.persistence.entity.Organization;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "Organization Service")
@RequestMapping("/api/v1/organization")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface IOrganizationController extends IGenericController<Organization, String> {
}
