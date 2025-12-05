package com.hibersoft.ms.bankcustomer.analytics.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hibersoft.ms.bankcustomer.analytics.service.DataAnalyticsService;

@RestController
@RequestMapping("/api/v1/analytics")
public class DataAnalyticsController {

    @Autowired
    private DataAnalyticsService analyticsService;

    /**
     * Endpoint to get top spending customers within a time range.
     * Example URL: http://localhost:8082/api/v1/analytics/top-customers?startDate=2023-01-01T00:00:00&endDate=2025-01-01T00:00:00&limit=5
     */
    @GetMapping("/top-customers")
    public ResponseEntity<Map<String, BigDecimal>> getTopCustomers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "5") int limit) {

        Map<String, BigDecimal> topSpenders = analyticsService.getTopSpendingCustomers(startDate, endDate, limit);
        return ResponseEntity.ok(topSpenders);
    }

    /**
     * Endpoint to get transaction counts grouped by location code.
     * Example URL: http://localhost:8082/api/v1/analytics/counts-by-location
     */
    @GetMapping("/counts-by-location")
    public ResponseEntity<Map<String, Long>> getCountsByLocation() {
        Map<String, Long> counts = analyticsService.getTransactionCountsByLocation();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/spending-by-category")
    public ResponseEntity<Map<String, BigDecimal>> getSpendingByCategory() {
        Map<String, BigDecimal> spending = analyticsService.getSpendingByCategory();
        return ResponseEntity.ok(spending);
    }

    // New endpoint for the overall platform average
    @GetMapping("/average-spending/overall")
    public ResponseEntity<BigDecimal> getOverallPlatformAverage() {
        BigDecimal overallAvg = analyticsService.getOverallPlatformAverage();
        return ResponseEntity.ok(overallAvg);
    }
    

    @GetMapping("/compare-spending/{bankId}")
    public ResponseEntity<Map<String, BigDecimal>> compareSpending(@PathVariable String bankId) {
        Map<String, BigDecimal> comparison = analyticsService.getComparativeAverageSpending(bankId);
        return ResponseEntity.ok(comparison);
    }
}
