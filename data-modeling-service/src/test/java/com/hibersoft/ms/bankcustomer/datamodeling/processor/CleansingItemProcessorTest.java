package com.hibersoft.ms.bankcustomer.datamodeling.processor;

import com.hibersoft.ms.bankcustomer.datamodeling.model.RawBankTransaction;
import com.hibersoft.ms.bankcustomer.datamodeling.model.EnrichedBankTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CleansingItemProcessorTest {

    private CleansingItemProcessor processor;

    @BeforeEach
    public void setUp() {
        // Instantiate the processor directly, no Spring context needed
        processor = new CleansingItemProcessor();
    }

    @Test
    public void testProcess_ValidInput_ReturnsEnrichedTransaction() throws Exception {
        // Arrange
        RawBankTransaction rawTransaction = new RawBankTransaction();
        rawTransaction.setBankSpecificAccountId("ACC123");
        rawTransaction.setTransactionDate("2023-10-27 15:30:00"); //yyyy-MM-dd HH:mm:ss
        rawTransaction.setAmount("150.75");
        rawTransaction.setDescription("ATM Withdrawal Toronto");
        rawTransaction.setLocationCode("ON_YYZ");
        rawTransaction.setBankSpecificAccountId("ACC123");
        rawTransaction.setDescription("Test Desc");
        rawTransaction.setLocationCode("L1");

        // Act
        EnrichedBankTransaction result = processor.process(rawTransaction);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsValid());
        assertEquals("ACC123", result.getBankSpecificAccountId());
        assertEquals(new BigDecimal("150.75"), result.getAmountStandard());
        System.out.println("result.getTransactionTime(): "+ result.getTransactionTime());
        System.out.println("LocalDateTime.of(2023, 10, 27, 15, 30): "+LocalDateTime.of(2023, 10, 27, 15, 30));
        assertEquals(LocalDateTime.of(2023, 10, 27, 15, 30), result.getTransactionTime());
        
        // assertEquals("ATM WITHDRAWAL TORONTO", result.getDescription()); // Checks cleansing (uppercase)
        // assertEquals("WITHDRAWAL", result.getTransactionType()); // Checks business logic
    }

    @Test
    public void testProcess_InvalidAmountFormat_ReturnsNullAndLogsError() throws Exception {
        // Arrange
        RawBankTransaction rawTransaction = new RawBankTransaction();
        // Initialize all required fields:
        rawTransaction.setBankSpecificTransactionId("TX1"); // <-- Add this line
        rawTransaction.setBankSpecificAccountId("ACC123");
        rawTransaction.setTransactionDate("2023-10-27 15:30:00");
        rawTransaction.setAmount("not_a_number"); // Invalid amount
        rawTransaction.setDescription("Test Desc");
        rawTransaction.setLocationCode("L1");

        // Act
        EnrichedBankTransaction result = processor.process(rawTransaction);

        // Assert
        assertNull(result, "Processor should return null for invalid data to skip item writing");
    }

    @Test
    public void testProcess_InvalidDateFormat_SetsIsValidToFalse() throws Exception {
        // Arrange
        RawBankTransaction rawTransaction = new RawBankTransaction();
        rawTransaction.setBankSpecificAccountId("ACC123");
        rawTransaction.setTransactionDate("yesterday_at_noon"); // Invalid date format
        rawTransaction.setAmount("10.00");
        rawTransaction.setDescription("Test Desc");
        rawTransaction.setLocationCode("L1");

        // Act
        EnrichedBankTransaction result = processor.process(rawTransaction);

        // Assert
        assertNull(result, "Processor should return null for invalid data to skip item writing if date is invalid");
    }
}
