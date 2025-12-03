package com.hibersoft.ms.bankcustomer.datamodeling.processor;

import com.hibersoft.ms.bankcustomer.datamodeling.model.CustomerMdmEntity;
import com.hibersoft.ms.bankcustomer.datamodeling.model.EnrichedBankTransaction;
import com.hibersoft.ms.bankcustomer.datamodeling.model.FactTransactionEntity; // Import Fact entity
import com.hibersoft.ms.bankcustomer.datamodeling.repository.CustomerMdmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrichmentItemProcessorTest {

    @InjectMocks
    private EnrichmentItemProcessor processor;

    @Mock
    private CustomerMdmRepository mdmRepository;

    @BeforeEach
    public void setUp() {
        // Manually inject a mock cache for testing purposes
        processor.customerIdCache = new ConcurrentHashMap<>();
    }

    @Test
    public void testProcess_ValidAccountFoundInMdm_EnrichesTransaction() throws Exception {
        // Arrange
        final String testAccountId = "ACC123";
        final String expectedUnifiedId = "U_CUST_456";

        EnrichedBankTransaction inputTransaction = new EnrichedBankTransaction();
        inputTransaction.setBankSpecificAccountId(testAccountId);
        inputTransaction.setAmountStandard(new BigDecimal("100.00"));
        inputTransaction.setIsValid(true); // Assuming valid input up to this point

        CustomerMdmEntity mockMdmEntity = new CustomerMdmEntity();
        mockMdmEntity.setBankSpecificAccountId(testAccountId);
        mockMdmEntity.setUnifiedCustomerId(expectedUnifiedId);

        // Mock the repository call
        when(mdmRepository.findByBankSpecificAccountId(testAccountId)).thenReturn(Optional.of(mockMdmEntity));

        // Act
        // The result type is now FactTransactionEntity
        FactTransactionEntity result = processor.process(inputTransaction);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUnifiedId, result.getCustomerId()); // Check if unified ID was set
        assertTrue(result.getIsValid()); // Should be true as mapping was successful
        
        // Verify DB lookup occurred once
        verify(mdmRepository, times(1)).findByBankSpecificAccountId(testAccountId);
        // Verify it was added to cache
        assertEquals(expectedUnifiedId, processor.customerIdCache.get(testAccountId));
    }
    
    @Test
    public void testProcess_AccountNotFoundInMdm_ReturnsNull() throws Exception {
        // Arrange
        final String testAccountId = "ACC999"; // Non-existent ID
        EnrichedBankTransaction inputTransaction = new EnrichedBankTransaction();
        inputTransaction.setBankSpecificAccountId(testAccountId);
        inputTransaction.setIsValid(true);

        // Mock repository to return empty optional (not found)
        when(mdmRepository.findByBankSpecificAccountId(testAccountId)).thenReturn(Optional.empty());

        // Act
        // The result type is now FactTransactionEntity, but we expect null for invalid records
        FactTransactionEntity result = processor.process(inputTransaction);

        // Assert
        assertNull(result, "Processor should return null if account is not found in MDM");
        // Verify DB lookup occurred once
        verify(mdmRepository, times(1)).findByBankSpecificAccountId(testAccountId);
    }
}
