package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.AuditLog;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.CustomerRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private CustomerServiceImpl customerService;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    private ModelMapper mapper;
    SecurityContext securityContext;
    Authentication authentication;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        mapper = mock(ModelMapper.class);
    }


    @Test
    public void testFilter() {
        // Mock data
        Pageable pageable = PageRequest.of(10, 10);
        List<String> names = new ArrayList<>();
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createBy = "John Doe";
        String lastUpdatedBy = "Jane Doe";
        String identificationNumber = "123456";
        String organizationId = "3d65906a-c6e3-4e9d-bbc6-ba20938f9cad";

        List<Customer> mockedCustomers = new ArrayList<>();  // Add mocked customers as needed

        Page<Customer> mockedPage = new PageImpl<>(mockedCustomers);

        when(customerRepository.filter(
            eq(pageable),
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(organizationId),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(null)
        )).thenReturn(mockedPage);

        // Call the method to test
        Page<Customer> resultPage = customerService.filter(
            pageable,
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            identificationNumber,
            null
        );
        assertEquals(null, resultPage);

    }

    @Test
    public void testFilterWithNoResults() {
        // Mock data
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<String> names = new ArrayList<>();
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createBy = "John Doe";
        String lastUpdatedBy = "Jane Doe";
        String identificationNumber = "123456";
        String keyword = null;
        String organizationId = "3d65906a-c6e3-4e9d-bbc6-ba20938f9cad";


        List<Customer> mockedCustomers = new ArrayList<>();  // Empty list

        Page<Customer> mockedPage = new PageImpl<>(mockedCustomers);

        when(customerRepository.filter(
            eq(pageable),
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(organizationId),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(keyword)
        )).thenReturn(mockedPage);

        // Call the method to test
        Page<Customer> resultPage = customerService.filter(
            pageableSort,
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            identificationNumber,
            keyword
        );

        // Assertions for no results
        assertEquals(mockedPage, resultPage);

        // Verify that the repository method was called with the expected arguments
        verify(customerRepository, times(1)).filter(
            eq(pageable),
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(organizationId),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(keyword)
        );
    }

    @Test
    public void testFilterList() {
        // Mock data
        List<String> names = new ArrayList<>();
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createBy = "John Doe";
        String lastUpdatedBy = "Jane Doe";
        String identificationNumber = "123456";
        String keyword = null;
        String organizationId = "3d65906a-c6e3-4e9d-bbc6-ba20938f9cad";


        List<Customer> mockedCustomers = new ArrayList<>();  // Add mocked customers as needed

        when(customerRepository.filter(
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(organizationId),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(keyword)
        )).thenReturn(mockedCustomers);

        // Call the method to test
        List<Customer> result = customerService.filter(
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            identificationNumber,
            keyword
        );

        // Assertions
        assertEquals(mockedCustomers, result);

        // Verify that the repository method was called with the expected arguments
        verify(customerRepository, times(1)).filter(
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(organizationId),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(null)
        );
    }

    @Test
    public void testFilterWithEmptyResult() {
        // Mock data
        List<String> names = new ArrayList<>();
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createBy = "John Doe";
        String lastUpdatedBy = "Jane Doe";
        String identificationNumber = "123456";
        String organizationId = "3d65906a-c6e3-4e9d-bbc6-ba20938f9cad";


        List<Customer> mockedCustomers = new ArrayList<>();  // Empty list

        when(customerRepository.filter(
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(organizationId),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(null)
        )).thenReturn(mockedCustomers);

        // Call the method to test
        List<Customer> result = customerService.filter(
            names,
            createdOnStart,
            createdOnEnd,
            createBy,
            lastUpdatedBy,
            identificationNumber,
            null
        );

        // Assertions for empty result
        assertEquals(mockedCustomers, result);

        // Verify that the repository method was called with the expected arguments
        verify(customerRepository, times(1)).filter(
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(organizationId),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(null)
        );
    }

    @Test
    public void testFindAllByOrganizationIdWithOrgIdProvided() {

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        LocalDateTime now = LocalDateTime.now();
        // Mock customerRepository response
        List<Customer> mockedCustomers = new ArrayList<>();  // Add mocked customers as needed
        when(customerRepository.findAllByOrganizationId("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad", now, now)).thenReturn(mockedCustomers);

        // Call the method to test
        List<Customer> result = customerService.findAllByOrganizationId(ICustomerController.CustomerAvailablePayload.builder().startTime(now).endTime(now).build());

        // Assertions
        assertEquals(mockedCustomers, result);

        // Verify that the repository method was called with the expected argument
        verify(customerRepository, times(1)).findAllByOrganizationId("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad", now, now);
    }

    @Test
    public void testFindAllByOrganizationIdWithInvalidSite() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock siteRepository response
        when(siteRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.empty());

        // Call the method to test, expecting an exception
        assertThrows(CustomException.class, () -> customerService.findAllByOrganizationId(ICustomerController.CustomerAvailablePayload.builder().build()));
    }

    @Test
    void testDeleteCustomer() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Mock input data
        UUID customerId = UUID.randomUUID();

        // Mock customer repository behavior
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setOrganizationId("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"); // Set a valid organizationId for authorization check
        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(customer));


        // Call the method
        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId));

        // Verify the interactions
        verify(customerRepository, times(1)).findById(customerId);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        verify(customerRepository, times(1)).deleteById(customerId);
    }

    @Test
    void testDeleteCustomerWithNoPermission() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock input data
        UUID customerId = UUID.randomUUID();

        // Mock customer repository behavior
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setOrganizationId("3d65906a-c6e3-4e9d-bbc6-ba20938f9ca8"); // Set an invalid organizationId for authorization check
        when(customerRepository.findById(customerId)).thenReturn(java.util.Optional.of(customer));

        // Call the method and expect an exception
        assertThrows(CustomException.class, () -> customerService.deleteCustomer(customerId));

        // Verify the interactions
        verify(customerRepository, times(1)).findById(customerId);
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verify(customerRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void testCheckExistCustomerWithEmail() {

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ICustomerController.CustomerCheckExist customerCheckExist = new ICustomerController.CustomerCheckExist();
        customerCheckExist.setType(Constants.CustomerCheckType.EMAIL);
        customerCheckExist.setValue("test@example.com");

        // Mock site
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"));
        when(siteRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(site));

        // Mock repository behavior
        when(customerRepository.existsByEmailAndOrganizationId(eq("test@example.com"), eq("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"))).thenReturn(true);

        // Perform the test
        assertThrows(CustomException.class, () -> customerService.checkExistCustomer(customerCheckExist));

        // Verify that the repository method was called
        verify(customerRepository, times(1)).existsByEmailAndOrganizationId("test@example.com", "3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
    }

    @Test
    void testCheckExistCustomerWithIdentificationNumber() {

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ICustomerController.CustomerCheckExist customerCheckExist = new ICustomerController.CustomerCheckExist();
        customerCheckExist.setType(Constants.CustomerCheckType.IDENTIFICATION_NUMBER);
        customerCheckExist.setValue("123456789012");

        // Mock site
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"));
        when(siteRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(site));

        // Mock repository behavior
        when(customerRepository.existsByIdentificationNumberAndOrganizationId(eq("123456789012"), eq("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"))).thenReturn(true);

        // Perform the test
        assertThrows(CustomException.class, () -> customerService.checkExistCustomer(customerCheckExist));

        // Verify that the repository method was called
        verify(customerRepository, times(1)).existsByIdentificationNumberAndOrganizationId("123456789012", "3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
    }

    @Test
    void testCheckExistCustomerWithPhoneNumber() {

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ICustomerController.CustomerCheckExist customerCheckExist = new ICustomerController.CustomerCheckExist();
        customerCheckExist.setType(Constants.CustomerCheckType.PHONE_NUMBER);
        customerCheckExist.setValue("1234567890");

        // Mock site
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"));
        when(siteRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(site));

        // Mock repository behavior
        when(customerRepository.existsByPhoneNumberAndOrganizationId(eq("1234567890"), eq("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"))).thenReturn(true);

        // Perform the test
        assertThrows(CustomException.class, () -> customerService.checkExistCustomer(customerCheckExist));

        // Verify that the repository method was called
        verify(customerRepository, times(1)).existsByPhoneNumberAndOrganizationId("1234567890", "3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
    }

    @Test
    void testCheckExistCustomerWithPhoneNumberNull() {

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ICustomerController.CustomerCheckExist customerCheckExist = new ICustomerController.CustomerCheckExist();
        customerCheckExist.setType(Constants.CustomerCheckType.PHONE_NUMBER);
        customerCheckExist.setValue(null);

        // Mock site
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"));
        when(siteRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(site));

        // Perform the test
        assertThrows(CustomException.class, () -> customerService.checkExistCustomer(customerCheckExist));

    }

    @Test
    void testCheckExistCustomerWithEmailNull() {

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ICustomerController.CustomerCheckExist customerCheckExist = new ICustomerController.CustomerCheckExist();
        customerCheckExist.setType(Constants.CustomerCheckType.EMAIL);
        customerCheckExist.setValue(null);

        // Mock site
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"));
        when(siteRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(site));

        // Perform the test
        assertThrows(CustomException.class, () -> customerService.checkExistCustomer(customerCheckExist));
    }

    @Test
    void testCheckExistCustomerWithIdentificationNumberNull() {

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ICustomerController.CustomerCheckExist customerCheckExist = new ICustomerController.CustomerCheckExist();
        customerCheckExist.setType(Constants.CustomerCheckType.IDENTIFICATION_NUMBER);
        customerCheckExist.setValue(null);

        // Mock site
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad"));
        when(siteRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(site));

        // Perform the test
        assertThrows(CustomException.class, () -> customerService.checkExistCustomer(customerCheckExist));

    }
}
