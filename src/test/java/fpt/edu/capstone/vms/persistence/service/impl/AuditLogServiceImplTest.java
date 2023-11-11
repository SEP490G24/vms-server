package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.controller.IAuditLogController;
import fpt.edu.capstone.vms.persistence.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    Pageable pageable;


    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void filterTest() {
        // Arrange
        List<String> organizations = Arrays.asList("Org1", "Org2");
        List<String> sites = Arrays.asList("Site1", "Site2");
        Constants.AuditType auditType = Constants.AuditType.CREATE;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createdBy = "John Doe";
        String tableName = "SomeTable";
        String keyword = "SearchKeyword";

        // Mock the behavior of the auditLogRepository
        List<IAuditLogController.AuditLogFilterDTO> expectedResult = Arrays.asList(
            new IAuditLogController.AuditLogFilterDTO(/* initialize your DTO */));

        when(auditLogRepository.filter(
            eq(organizations), eq(sites), eq(auditType),
            eq(createdOnStart), eq(createdOnEnd), eq(createdBy),
            eq(tableName), eq(keyword)))
            .thenReturn(expectedResult);

        // Act
        List<IAuditLogController.AuditLogFilterDTO> result = auditLogService.filter(
            organizations, sites, auditType, createdOnStart,
            createdOnEnd, createdBy, tableName, keyword);

        // Assert
        assertEquals(expectedResult, result);

        // Verify that the filter method of auditLogRepository was called with the correct arguments
        verify(auditLogRepository).filter(
            eq(organizations), eq(sites), eq(auditType),
            eq(createdOnStart), eq(createdOnEnd), eq(createdBy),
            eq(tableName), eq(keyword));
    }

    @Test
    void filterPageableTest() {
        // Arrange
        List<String> organizations = Arrays.asList("Org1", "Org2");
        List<String> sites = Arrays.asList("Site1", "Site2");
        Constants.AuditType auditType = Constants.AuditType.CREATE;
        LocalDateTime createdOnStart = LocalDateTime.now().minusDays(7);
        LocalDateTime createdOnEnd = LocalDateTime.now();
        String createdBy = "John Doe";
        String tableName = "SomeTable";
        String keyword = "SearchKeyword";

        // Mock the behavior of the auditLogRepository
        List<IAuditLogController.AuditLogFilterDTO> expectedContent = Arrays.asList(
            new IAuditLogController.AuditLogFilterDTO(/* initialize your DTO */));
        Page<IAuditLogController.AuditLogFilterDTO> expectedResult = new PageImpl<>(expectedContent);

        when(auditLogRepository.filter(
            eq(pageable), eq(organizations), eq(sites), eq(auditType),
            eq(createdOnStart), eq(createdOnEnd), eq(createdBy),
            eq(tableName), eq(keyword)))
            .thenReturn(expectedResult);

        // Act
        Page<IAuditLogController.AuditLogFilterDTO> result = auditLogService.filter(
            pageable, organizations, sites, auditType, createdOnStart,
            createdOnEnd, createdBy, tableName, keyword);

        // Assert
        assertEquals(expectedResult, result);

        // Verify that the filter method of auditLogRepository was called with the correct arguments
        verify(auditLogRepository).filter(
            eq(pageable), eq(organizations), eq(sites), eq(auditType),
            eq(createdOnStart), eq(createdOnEnd), eq(createdBy),
            eq(tableName), eq(keyword));
    }
}
