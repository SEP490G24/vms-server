package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.controller.IReasonController;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.repository.ReasonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@Tag("UnitTest")
@DisplayName("Reason Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class ReasonServiceImplTest {
    @Mock
    private ReasonRepository reasonRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ReasonServiceImpl reasonService;

    @Test
    @DisplayName("given valid ReasonDto, when create Reason, then Reason is returned")
    void givenValidReasonDto_whenCreateReason_thenReasonReturned() {
        // Given
        UUID siteId = UUID.fromString("92c0bd00-be4f-4508-9dba-c73867d6c8d9");
        IReasonController.ReasonDto reasonDto = new IReasonController.ReasonDto();
        reasonDto.setSiteId(siteId);
        reasonDto.setCode("Test");
        reasonDto.setName("Test");

        Reason mappedReason = new Reason();
        mappedReason.setCode("Test");
        mappedReason.setName("Test");
        mappedReason.setSiteId(UUID.fromString("92c0bd00-be4f-4508-9dba-c73867d6c8d9"));
        when(modelMapper.map(reasonDto, Reason.class)).thenReturn(mappedReason);

        // When
        Reason createdReason = reasonService.create(reasonDto);

        // Then
        assertNotNull(createdReason);
        assertEquals(true, createdReason.getEnable());
        verify(reasonRepository, times(1)).save(mappedReason);
    }

    @Test
    @DisplayName("given null ReasonDto, when create Reason, then HttpClientErrorException is thrown")
    void givenNullReasonDto_whenCreateReason_thenHttpClientErrorExceptionThrown() {
        // Given
        IReasonController.ReasonDto reasonDto = null;

        // When and Then
        assertThrows(HttpClientErrorException.class, () -> reasonService.create(reasonDto));
    }

    @Test
    @DisplayName("given ReasonDto with null SiteId, when create Reason, then HttpClientErrorException is thrown")
    void givenReasonDtoWithNullSiteId_whenCreateReason_thenHttpClientErrorExceptionThrown() {
        // Given
        IReasonController.ReasonDto reasonDto = new IReasonController.ReasonDto();

        // When and Then
        assertThrows(HttpClientErrorException.class, () -> reasonService.create(reasonDto));
    }

    @Test
    @DisplayName("given valid ReasonInfo and Id, when update Reason, then Reason is returned")
    void givenValidReasonInfoAndId_whenUpdateReason_thenReasonReturned() {
        // Given
        UUID reasonId = UUID.randomUUID();
        Reason reasonInfo = new Reason();
        Reason existingReason = new Reason();
        existingReason.setId(reasonId);
        when(reasonRepository.findById(reasonId)).thenReturn(Optional.of(existingReason));
        when(reasonRepository.save(existingReason.update(reasonInfo))).thenReturn(existingReason);

        // When
        Reason updatedReason = reasonService.update(reasonInfo, reasonId);

        // Then
        assertNotNull(updatedReason);
        verify(reasonRepository, times(1)).findById(reasonId);
        verify(reasonRepository, times(1)).save(existingReason.update(reasonInfo));
    }

    @Test
    @DisplayName("given invalid Id, when update Reason, then HttpClientErrorException is thrown")
    void givenInvalidId_whenUpdateReason_thenHttpClientErrorExceptionThrown() {
        // Given
        UUID invalidReasonId = UUID.randomUUID();
        Reason reasonInfo = new Reason();
        when(reasonRepository.findById(invalidReasonId)).thenReturn(Optional.empty());

        // When and Then
        assertThrows(HttpClientErrorException.class, () -> reasonService.update(reasonInfo, invalidReasonId));
    }

    @Test
    @DisplayName("given pageable and filter criteria, when filter Reasons, then Page<Reason> is returned")
    void givenPageableAndFilterCriteria_whenFilterReasons_thenPageOfReasonReturned() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdOn"), Sort.Order.desc("lastUpdatedOn")));
        Pageable pageableSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        List<String> names = Arrays.asList("Reason1", "Reason2");
        UUID siteId = UUID.randomUUID();
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "example";

        Page<Reason> filteredReasons = new PageImpl<>(List.of());
        when(reasonRepository.filter(pageable, names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase())).thenReturn(filteredReasons);

        // When
        Page<Reason> filteredReasonPage = reasonService.filter(pageableSort, names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase());

        // Then
        assertNotNull(filteredReasonPage);
        verify(reasonRepository, times(1)).filter(pageable, names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase());
    }

    @Test
    @DisplayName("given filter criteria, when filter Reasons, then List<Reason> is returned")
    void givenFilterCriteria_whenFilterReasons_thenListOfReasonReturned() {
        // Given
        List<String> names = Arrays.asList("Reason1", "Reason2");
        UUID siteId = UUID.randomUUID();
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        Boolean enable = true;
        String keyword = "example";

        List<Reason> filteredReasons = Arrays.asList(new Reason(), new Reason());
        when(reasonRepository.filter(names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase())).thenReturn(filteredReasons);

        // When
        List<Reason> filteredReasonList = reasonService.filter(names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase());


        // Then
        assertNotNull(filteredReasonList);
        verify(reasonRepository, times(1)).filter(names, siteId, createdOnStart, createdOnEnd, enable, keyword.toUpperCase());
    }

    @Test
    @DisplayName("given valid SiteId, when findAllBySiteId, then List<Reason> is returned")
    void givenValidSiteId_whenFindAllBySiteId_thenListOfReasonReturned() {
        // Given
        UUID siteId = UUID.randomUUID();
        List<Reason> reasons = Arrays.asList(new Reason(), new Reason());
        when(reasonRepository.findAllBySiteIdAndEnableIsTrue(siteId)).thenReturn(reasons);

        // When
        List<Reason> foundReasons = reasonService.finAllBySiteId(siteId);

        // Then
        assertNotNull(foundReasons);
        verify(reasonRepository, times(1)).findAllBySiteIdAndEnableIsTrue(siteId);
    }


}
