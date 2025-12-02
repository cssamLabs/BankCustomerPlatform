package com.hibersoft.ms.bankcustomer.datageneration.service;

import com.hibersoft.ms.bankcustomer.datageneration.model.RawTransactionEntity;
import com.hibersoft.ms.bankcustomer.datageneration.repository.RawTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataGenerationServiceTest {

    @InjectMocks
    private DataGenerationService service;

    @Mock
    private RawTransactionRepository repository;

    @Test
    public void testGenerateData_GeneratesCorrectNumberOfRecords() {
        // Arrange
        String bankId = "BANK_A";
        int countToGenerate = 50;

        // Mock the repository saveAll method (void method, so do nothing)
        when(repository.saveAll(anyList())).thenReturn(anyList()); // Mockito needs a return value even for void methods with saveAll

        // Act
        int generatedCount = service.generateData(bankId, countToGenerate);

        // Assert
        assertEquals(countToGenerate, generatedCount);

        // Capture the argument passed to the repository's saveAll method
        ArgumentCaptor<List<RawTransactionEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository, times(1)).saveAll(captor.capture());

        List<RawTransactionEntity> capturedEntities = captor.getValue();
        assertEquals(countToGenerate, capturedEntities.size());

        // Verify that the generated data has basic attributes set (e.g., amount is not null)
        capturedEntities.forEach(entity -> {
            // Assert basic formatting or presence of data in the generated entity
            assert entity.getAmount() != null && !entity.getAmount().isEmpty();
            assert entity.getBankSpecificAccountId() != null && !entity.getBankSpecificAccountId().isEmpty();
        });
    }
}
