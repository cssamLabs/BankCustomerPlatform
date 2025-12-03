package com.hibersoft.ms.bankcustomer.analytics.service;

import com.hibersoft.ms.bankcustomer.analytics.repository.FactTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataAnalyticsServiceTest {

    @InjectMocks
    private DataAnalyticsService analyticsService;

    @Mock
    private FactTransactionRepository repository;

    @Test
    public void testGetTopSpendingCustomers_ReturnsMappedResults() {
        // Arrange
        LocalDateTime start = LocalDateTime.MIN;
        LocalDateTime end = LocalDateTime.MAX;
        int limit = 2;

        // Mock the repository response: List<Object[]> where Object[0] is customerId, Object[1] is total_spent
        List<Object[]> mockResults = Arrays.asList(
            new Object[]{"U_CUST_1", new BigDecimal("550.75")},
            new Object[]{"U_CUST_2", new BigDecimal("400.00")},
            new Object[]{"U_CUST_3", new BigDecimal("300.00")} // This should be limited out
        );
        
        // This cast is necessary due to Java generics in the service method
        List<Object[]> resultsToReturn = (List<Object[]>) (List<?>) mockResults;
        
        when(repository.findTopSpendingCustomers(start, end)).thenReturn(resultsToReturn);

        // Act
        Map<String, BigDecimal> topCustomers = analyticsService.getTopSpendingCustomers(start, end, limit);

        // Assert
        assertNotNull(topCustomers);
        assertEquals(limit, topCustomers.size()); // Verify limit applied
        assertEquals(new BigDecimal("550.75"), topCustomers.get("U_CUST_1"));
        assertEquals(new BigDecimal("400.00"), topCustomers.get("U_CUST_2"));
    }

    @Test
    public void testGetTransactionCountsByLocation_ReturnsMappedResults() {
        // Arrange
        // Mock the repository response: List<Object[]> where Object[0] is locationCode, Object[1] is count
        List<Object[]> mockResults = Arrays.asList(
            new Object[]{"L1", 150L},
            new Object[]{"L2", 80L}
        );

        // This cast is necessary due to Java generics in the service method
        List<Object[]> resultsToReturn = (List<Object[]>) (List<?>) mockResults;

        when(repository.countTransactionsByLocation()).thenReturn(resultsToReturn);

        // Act
        Map<String, Long> countsByLocation = analyticsService.getTransactionCountsByLocation();

        // Assert
        assertNotNull(countsByLocation);
        assertEquals(2, countsByLocation.size());
        assertEquals(150L, countsByLocation.get("L1"));
        assertEquals(80L, countsByLocation.get("L2"));
    }
}
