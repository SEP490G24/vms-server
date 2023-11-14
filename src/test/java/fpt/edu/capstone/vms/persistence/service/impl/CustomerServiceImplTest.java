package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ICustomerController;
import fpt.edu.capstone.vms.persistence.entity.Customer;
import fpt.edu.capstone.vms.persistence.entity.Site;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
    public void testCreateWithNullDto() {
        // Use assertThrows to check for IllegalArgumentException
        assertThrows(NullPointerException.class, () -> customerService.create(null));
    }

    @Test
    public void testCreateWithRepositoryFailure() {
        // Mock data
        ICustomerController.NewCustomers createCustomerDto = new ICustomerController.NewCustomers();
        // Set properties for createCustomerDto

        when(mapper.map(createCustomerDto, Customer.class)).thenReturn(new Customer());
        when(customerRepository.save(any(Customer.class))).thenThrow(new RuntimeException("Repository failure"));

        // Use assertThrows to check for RuntimeException
        assertThrows(NullPointerException.class, () -> customerService.create(createCustomerDto));
    }

    @Test
    public void testFilter() {
        // Mock data
        Pageable pageable = Pageable.unpaged();
        List<String> names = new ArrayList<>();
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createBy = "John Doe";
        String lastUpdatedBy = "Jane Doe";
        String identificationNumber = "123456";
        String keyword = "some keyword";

        List<Customer> mockedCustomers = new ArrayList<>();  // Add mocked customers as needed

        Page<Customer> mockedPage = new PageImpl<>(mockedCustomers);

        when(customerRepository.filter(
            eq(pageable),
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(keyword)
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
            keyword
        );

        // Assertions
        assertEquals(mockedPage, resultPage);

        // Verify that the repository method was called with the expected arguments
        verify(customerRepository, times(1)).filter(
            eq(pageable),
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(keyword)
        );
    }

    @Test
    public void testFilterWithNoResults() {
        // Mock data
        Pageable pageable = Pageable.unpaged();
        List<String> names = new ArrayList<>();
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createBy = "John Doe";
        String lastUpdatedBy = "Jane Doe";
        String identificationNumber = "123456";
        String keyword = "nonexistent";

        List<Customer> mockedCustomers = new ArrayList<>();  // Empty list

        Page<Customer> mockedPage = new PageImpl<>(mockedCustomers);

        when(customerRepository.filter(
            eq(pageable),
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(keyword)
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
        String keyword = "some keyword";

        List<Customer> mockedCustomers = new ArrayList<>();  // Add mocked customers as needed

        when(customerRepository.filter(
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
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
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(keyword)
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
        String keyword = "nonexistent";

        List<Customer> mockedCustomers = new ArrayList<>();  // Empty list

        when(customerRepository.filter(
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
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

        // Assertions for empty result
        assertEquals(mockedCustomers, result);

        // Verify that the repository method was called with the expected arguments
        verify(customerRepository, times(1)).filter(
            eq(names),
            eq(createdOnStart),
            eq(createdOnEnd),
            eq(createBy),
            eq(lastUpdatedBy),
            eq(identificationNumber),
            eq(keyword)
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

        // Mock customerRepository response
        List<Customer> mockedCustomers = new ArrayList<>();  // Add mocked customers as needed
        when(customerRepository.findAllByOrganizationId("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad")).thenReturn(mockedCustomers);

        // Call the method to test
        List<Customer> result = customerService.findAllByOrganizationId();

        // Assertions
        assertEquals(mockedCustomers, result);

        // Verify that the repository method was called with the expected argument
        verify(customerRepository, times(1)).findAllByOrganizationId("3d65906a-c6e3-4e9d-bbc6-ba20938f9cad");
    }

    @Test
    public void testFindAllByOrganizationIdWithSiteId() {
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
        Site mockedSite = new Site();  // Add necessary properties
        when(siteRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(mockedSite));

        // Mock customerRepository response
        List<Customer> mockedCustomers = new ArrayList<>();  // Add mocked customers as needed
        when(customerRepository.findAllByOrganizationId(any(String.class))).thenReturn(mockedCustomers);

        // Call the method to test
        List<Customer> result = customerService.findAllByOrganizationId();

        // Assertions
        assertEquals(mockedCustomers, result);

        // Verify that the repository method was called with the expected argument
        verify(customerRepository, times(1)).findAllByOrganizationId(any(String.class));
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
        assertThrows(HttpClientErrorException.class, () -> customerService.findAllByOrganizationId());
    }
}
