package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.ITemplateController;
import fpt.edu.capstone.vms.exception.CustomException;
import fpt.edu.capstone.vms.persistence.entity.Site;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import fpt.edu.capstone.vms.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestInstance(PER_CLASS)
@ActiveProfiles("test")
@Tag("UnitTest")
@DisplayName("Template Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class TemplateServiceImplTest {
    TemplateRepository templateRepository;
    TemplateServiceImpl templateService;
    SiteRepository siteRepository;
    Pageable pageable;
    AuditLogRepository auditLogRepository;
    SecurityContext securityContext;
    Authentication authentication;

    @BeforeAll
    public void init() {
        pageable = mock(Pageable.class);
        templateRepository = mock(TemplateRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        siteRepository = mock(SiteRepository.class);
        templateService = new TemplateServiceImpl(templateRepository, siteRepository, new ModelMapper(), auditLogRepository);
    }

    @Test
    @DisplayName("when list template, then templates are retrieved")
    void whenListTemplates_ThenTemplatesRetrieved() {

        //given
        Template template1 = Template.builder().name("Template1").code("T1").build();
        Template template2 = Template.builder().name("Template2").code("T2").build();
        List<Template> mockTemplates = Arrays.asList(template1, template2);

        //when
        when(templateRepository.findAll()).thenReturn(mockTemplates);
        List<Template> templates = templateService.findAll();

        //then
        assertEquals(2, templates.size());
        assertEquals("Template1", templates.get(0).getName());
        assertEquals("Template2", templates.get(1).getName());
        assertNotNull(templates);
        assertFalse(templates.isEmpty());

        // Verify
        Mockito.verify(templateRepository, Mockito.times(1)).findAll();
    }

    @Test
    @DisplayName("given template id, when find existing template, then template are retrieved")
    void givenTemplateId_whenFindExistingTemplate_ThenTemplateRetrieved() {
        // Given
        UUID templateId = UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        Template expectedTemplate = new Template();
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(expectedTemplate));

        // When
        Template actualTemplate = templateService.findById(templateId);

        // Then
        assertNotNull(actualTemplate);
    }

    @Test
    @DisplayName("given template id, when find non existing template, then exception is thrown")
    void givenTemplateId_whenFindNonExistingTemplate_ThenExceptionThrown() {

        //given
        String nonExistingTemplateId = "06eb43a7-6ea8-4744-8231-760559fe2c08";
        String errorMsg = "Template Not Found : " + nonExistingTemplateId;
        when(templateRepository.findById(UUID.fromString(nonExistingTemplateId))).thenThrow(new EntityNotFoundException(errorMsg));

        //when
        EntityNotFoundException throwException = assertThrows(EntityNotFoundException.class, () -> templateService.findById((UUID.fromString(nonExistingTemplateId))));

        // then
        assertEquals(errorMsg, throwException.getMessage());
    }

    @Test
    @DisplayName("given template data, when create new Template, then Template id is returned")
    void givenTemplateData_whenCreateTemplate_ThenTemplateReturned() {

        //given
        Template template = Template.builder().name("Template2").code("R2").description("aaaalala").enable(true).siteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")).build();
        ITemplateController.TemplateDto templateDto = ITemplateController.TemplateDto.builder().name("Template2").code("R2").description("aaaalala").enable(true).siteId(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08")).build();
        template.setId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"));
        //when
        when(templateRepository.save(any(Template.class))).thenReturn(template);
        when(templateRepository.existsByCodeAndSiteId(template.getCode(), UUID.fromString(anyString()))).thenReturn(true);
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"));
        when(SecurityUtils.checkSiteAuthorization(siteRepository, template.getSiteId().toString())).thenReturn(true);
        when(siteRepository.findById(template.getSiteId())).thenReturn(Optional.of(site));
        Template templateActual = templateService.create(templateDto);

        //then
        assertEquals(template.getName(), templateActual.getName());
        assertNotNull(templateActual);
    }

    @Test
    @DisplayName("given TemplateDto is null, when create template, then exception is thrown")
    void givenTemplateDtoIsNull_whenCreateTemplate_ThenThrowHttpClientErrorException() {
        // Given
        ITemplateController.TemplateDto templateDto = null;

        // When and Then
        assertThrows(CustomException.class, () -> templateService.create(templateDto));

    }

    @Test
    @DisplayName("given Site is null, when create template, then exception is thrown")
    void givenSiteIsNull_whenCreateTemplate_thenThrowNullPointerException() {
        // Given
        ITemplateController.TemplateDto templateDto = new ITemplateController.TemplateDto();

        // When and Then
        NullPointerException exception = assertThrows(NullPointerException.class, () -> templateService.create(templateDto));

        //Verify
        //assertEquals("404 SiteId is null", exception.getMessage());
    }


    @Test
    @DisplayName("given Template is found, when update template, then Template id is returned")
    void givenTemplateData_whenUpdateTemplate_ThenTemplateReturned() {
        // Given
        UUID templateId = UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        Template templateInfo = new Template();
        Template existingTemplate = new Template();
        existingTemplate.setSiteId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"));
        existingTemplate.setId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"));
        templateInfo.setSiteId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Site site = new Site();
        site.setOrganizationId(UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d5"));
        when(SecurityUtils.checkSiteAuthorization(siteRepository, existingTemplate.getSiteId().toString())).thenReturn(true);
        when(siteRepository.findById(existingTemplate.getSiteId())).thenReturn(Optional.of(site));
        // When
        Template updatedTemplate = templateService.update(templateInfo, templateId);

        // Then
        assertNotNull(updatedTemplate);

        // Add more assertions to check specific details of the updatedTemplate object if needed.
        //verify(templateRepository, times(1)).findById(templateId);
        verify(templateRepository, times(1)).save(existingTemplate.update(templateInfo));
    }

    @Test
    @DisplayName("given Template is not found, when update template,  then exception is thrown")
    void givenTemplateNotFound_whenUpdateTemplate_thenThrowHttpClientErrorException() {
        // Given
        UUID nonExistingTemplateId = UUID.fromString("63139e5c-3d0b-46d3-8167-fe59cf46d3d1");
        Template templateInfo = new Template();
        String expectedErrorMessage = "404 Can't found template";

        when(templateRepository.findById(nonExistingTemplateId)).thenReturn(Optional.empty());

        // When and Then
        assertThrows(CustomException.class, () -> templateService.update(templateInfo, nonExistingTemplateId));


    }

    @Test
    void filter() {
        // Given
        List<String> names = Arrays.asList("Template1", "Template2");
        List<String> siteId = Arrays.asList("06eb43a7-6ea8-4744-8231-760559fe2c08", "06eb43a7-6ea8-4744-8231-760559fe2c07");
        List<UUID> siteIds = Arrays.asList(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"), UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));

        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "example";

        List<Template> expectedTemplates = List.of();
        when(templateRepository.filter(names, siteIds, createdOnStart, createdOnEnd, enable, keyword.toUpperCase())).thenReturn(expectedTemplates);
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(!SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);
        when(!SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c07")).thenReturn(true);
        // When
        List<Template> filteredTemplates = templateService.filter(names, siteId, createdOnStart, createdOnEnd, enable, keyword);

        // Then
        assertNotNull(filteredTemplates);
        // Add assertions to check the content of the filteredTemplates, depending on the expected behavior
        verify(templateRepository, times(1)).filter(names, siteIds, createdOnStart, createdOnEnd, enable, keyword.toUpperCase());
    }

    @Test
    void filterPageable() {
        // Given
        List<String> names = Arrays.asList("Template1", "Template2");
        List<String> siteId = Arrays.asList("06eb43a7-6ea8-4744-8231-760559fe2c08", "06eb43a7-6ea8-4744-8231-760559fe2c07");
        List<UUID> siteIds = Arrays.asList(UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08"), UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c07"));
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "example";
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim(Constants.Claims.SiteId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.Name)).thenReturn("username");
        when(jwt.getClaim(Constants.Claims.PreferredUsername)).thenReturn("preferred_username");
        when(jwt.getClaim(Constants.Claims.GivenName)).thenReturn("given_name");
        when(jwt.getClaim(Constants.Claims.OrgId)).thenReturn("63139e5c-3d0b-46d3-8167-fe59cf46d3d5");
        when(jwt.getClaim(Constants.Claims.FamilyName)).thenReturn("family_name");
        when(jwt.getClaim(Constants.Claims.Email)).thenReturn("email");
        when(authentication.getPrincipal()).thenReturn(jwt);

        // Set up SecurityContextHolder to return the mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        when(!SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c08")).thenReturn(true);
        when(!SecurityUtils.checkSiteAuthorization(siteRepository, "06eb43a7-6ea8-4744-8231-760559fe2c07")).thenReturn(true);
        Page<Template> expectedTemplatePage = new PageImpl<>(List.of());
        when(templateRepository.filter(pageable, names, siteIds, createdOnStart, createdOnEnd, enable, keyword.toUpperCase())).thenReturn(expectedTemplatePage);

        // When
        Page<Template> filteredTemplatePage = templateService.filter(pageableSort, names, siteId, createdOnStart, createdOnEnd, enable, keyword);

        // Then
        assertNotNull(filteredTemplatePage);
        // Add assertions to check the content of the filteredTemplatePage, depending on the expected behavior
        verify(templateRepository, times(1)).filter(pageable, names, siteIds, createdOnStart, createdOnEnd, enable, keyword.toUpperCase());
    }
}
