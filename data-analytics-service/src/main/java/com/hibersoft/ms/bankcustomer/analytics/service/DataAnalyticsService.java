package com.hibersoft.ms.bankcustomer.analytics.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hibersoft.ms.bankcustomer.analytics.repository.FactTransactionRepository;

@Service
public class DataAnalyticsService {

        private static final Logger log = LoggerFactory.getLogger(DataAnalyticsService.class);

    @Autowired
    private FactTransactionRepository repository;

    /**
     * Retrieves the top N spending customers within a given time range.
     * @param startDate the start of the time range
     * @param endDate the end of the time range
     * @param limit the number of top customers to return
     * @return a map of customer ID to total spending amount
     */
    public Map<String, BigDecimal> getTopSpendingCustomers(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        // This repository method returns a List<Object[]>, where Object[0] is customerId and Object[1] is total_spent
        List<Object[]> results = repository.findTopSpendingCustomers(startDate, endDate);

        // Process the results into a readable map and apply the limit
        return results.stream()
                .limit(limit)
                .collect(Collectors.toMap(
                        result -> (String) result[0], // customerId
                        result -> (BigDecimal) result[1], // total_spent
                        (oldValue, newValue) -> oldValue, // handle potential key collisions (unlikely here)
                        HashMap::new
                ));
    }

    /**
     * Counts transactions grouped by location code.
     * @return a map of location code to transaction count
     */
    public Map<String, Long> getTransactionCountsByLocation() {
        // This repository method returns a List<Object[]>, where Object[0] is locationCode and Object[1] is count
        List<Object[]> results = repository.countTransactionsByLocation();

        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0], // locationCode
                        result -> (Long) result[1]  // count
                ));
    }

    public Map<String, BigDecimal> getSpendingByCategory() {
            // Note the casting might need adjustment based on runtime types in Object[]
            List<Object[]> results = repository.findSpendingByCategory();

            return results.stream()
                            .filter(result -> result[0] != null) // Filter out any rows where the category (result[0])
                                                                 // is null
                            .collect(Collectors.toMap(
                                            result -> (String) result[0], // Category name
                                            result -> (BigDecimal) result[1] // Sum of amount
                            ));
    }

    public Map<String, BigDecimal> getComparativeAverageSpending(String bankId) {
        log.info("Comparing average spending for bankId: {}", bankId);
        BigDecimal overallAvg = repository.findOverallAverageTransactionAmount();
        BigDecimal bankAvg = repository.findAverageTransactionAmountByBank(bankId.toUpperCase());

        log.debug("Overall Average: {}", overallAvg);
        log.debug("Bank {} Average: {}", bankId, bankAvg);

        Map<String, BigDecimal> comparison = new HashMap<>();
        comparison.put("Overall_Platform_Average", overallAvg != null ? overallAvg : BigDecimal.ZERO);
        comparison.put(bankId.toUpperCase() + "_Average", bankAvg != null ? bankAvg : BigDecimal.ZERO);

        return comparison;
    }

    public BigDecimal getOverallPlatformAverage() {
        BigDecimal overallAvg = repository.findOverallAverageTransactionAmount();
        return overallAvg != null ? overallAvg : BigDecimal.ZERO;
    }

    
}
