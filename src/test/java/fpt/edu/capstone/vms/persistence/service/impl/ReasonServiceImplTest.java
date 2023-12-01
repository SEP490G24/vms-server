package fpt.edu.capstone.vms.persistence.service.impl;

import fpt.edu.capstone.vms.constants.Constants;
import fpt.edu.capstone.vms.persistence.entity.Reason;
import fpt.edu.capstone.vms.persistence.repository.ReasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@Tag("UnitTest")
@DisplayName("Reason Service Unit Tests")
@ExtendWith(MockitoExtension.class)
class ReasonServiceImplTest {

    private ReasonRepository reasonRepository = mock(ReasonRepository.class);
    private ReasonServiceImpl reasonService;

    @BeforeEach
    public void setUp() {
        reasonService = new ReasonServiceImpl(reasonRepository);
    }

    @Test
    public void testFindAllByType() {
        // Mock data
        Reason reason1 = new Reason();
        reason1.setName("Reason 1");
        reason1.setType(Constants.Reason.CANCEL);
        Reason reason2 = new Reason();
        reason2.setName("Reason 2");
        reason2.setType(Constants.Reason.CANCEL);
        // Mocking the repository behavior
        when(reasonRepository.findAllByType(Constants.Reason.CANCEL)).thenReturn(Arrays.asList(reason1, reason2));

        // Call the service method
        List<Reason> result = reasonService.findAllByType(Constants.Reason.CANCEL);

        // Assertions
        assertEquals(2, result.size());
        // Add more assertions based on your actual implementation

        // Verify that the repository method was called with the correct argument
        // You can add more specific verification based on your actual implementation
        Mockito.verify(reasonRepository).findAllByType(Constants.Reason.CANCEL);
    }
}
