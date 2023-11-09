package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.ITemplateController;
import fpt.edu.capstone.vms.persistence.entity.Template;
import fpt.edu.capstone.vms.persistence.repository.SiteRepository;
import fpt.edu.capstone.vms.persistence.repository.TemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @BeforeAll
    public void init() {
        pageable = mock(Pageable.class);
        templateRepository = mock(TemplateRepository.class);
        templateService = new TemplateServiceImpl(templateRepository, siteRepository, new ModelMapper());
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

        //when
        when(templateRepository.save(any(Template.class))).thenReturn(template);
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
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> templateService.create(templateDto));

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 Object is empty", exception.getMessage());
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
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any(Template.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> templateService.update(templateInfo, nonExistingTemplateId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(expectedErrorMessage, exception.getMessage());

    }

//    @Test
//    void filter() {
//        // Given
//        List<String> names = Arrays.asList("Template1", "Template2");
//        UUID siteId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
//        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
//        LocalDateTime createdOnEnd = LocalDateTime.now();
//        Boolean enable = true;
//        String keyword = "example";
//
//        List<Template> expectedTemplates = List.of();
//        when(templateRepository.filter(names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase())).thenReturn(expectedTemplates);
//
//        // When
//        List<Template> filteredTemplates = templateService.filter(names, siteId, createdOnStart, createdOnEnd, enable, keyword);
//
//        // Then
//        assertNotNull(filteredTemplates);
//        // Add assertions to check the content of the filteredTemplates, depending on the expected behavior
//        verify(templateRepository, times(1)).filter(names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase());
//    }
//
//    @Test
//    void filterPageable() {
//        // Given
//        List<String> names = Arrays.asList("Template1", "Template2");
//        UUID siteId = UUID.fromString("06eb43a7-6ea8-4744-8231-760559fe2c08");
//        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
//        LocalDateTime createdOnEnd = LocalDateTime.now();
//        Boolean enable = true;
//        String keyword = "example";
//
//        Page<Template> expectedTemplatePage = new PageImpl<>(List.of());
//        when(templateRepository.filter(pageable, names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase())).thenReturn(expectedTemplatePage);
//
//        // When
//        Page<Template> filteredTemplatePage = templateService.filter(pageable, names, siteId, createdOnStart, createdOnEnd, enable, keyword);
//
//        // Then
//        assertNotNull(filteredTemplatePage);
//        // Add assertions to check the content of the filteredTemplatePage, depending on the expected behavior
//        verify(templateRepository, times(1)).filter(pageable, names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase());
//    }
}
