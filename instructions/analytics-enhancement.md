# Enhancement of Analytics

Step 1: Update the `FACT_TRANSACTIONS` Schema and Entity

First, ensure your database has the new column, and your JPA entities map to it.

* **SQL (Run via `docker exec ... psql`):**
* bash

  ```
  docker exec -it postgres-db psql -U youruser -d datamodelingdb -c "ALTER TABLE FACT_TRANSACTIONS ADD COLUMN category VARCHAR(255);"
  ```
* **Java (`data-modeling-service/model/FactTransactionEntity.java`):**

  ```
  private String category; // Add this field
  ```
* **Java (`data-analytics-service/model/FactTransactionEntity.java`):**

```
  private String category; // Add this field
```

Step 2: Implement Categorization Logic in the Data Modelling Service

Update the processor in the data modelling service to assign a category based on the transaction description.

* **Java (`data-modeling-service/config/BatchConfiguration.java`):**

java

```

java
package com.hibersoft.ms.bankcustomer.simpledatamodeling.config;
// ... (imports and existing processor bean definition) ...

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    // ... 

    @Bean
    public ItemProcessor<RawSourceData, FactTransactionEntity> processor() {
        return rawData -> {
            // ... (existing mapping logic) ...
            fact.setDescriptionStandard(rawData.getDescription());
            fact.setLocationCode(rawData.getLocationCode());

            // --- ADD CATEGORIZATION LOGIC ---
            fact.setCategory(categorizeTransaction(rawData.getDescription()));
            // --------------------------------

            log.debug("Mapped to Fact Entity for Customer: {} in Category: {}", fact.getCustomerId(), fact.getCategory());
            return fact;
        };
    }

    // --- ADD HELPER METHOD ---
    private String categorizeTransaction(String description) {
        String lowerDesc = description.toLowerCase();
        if (lowerDesc.contains("utility") || lowerDesc.contains("hydro") || lowerDesc.contains("bell") || lowerDesc.contains("rogers")) {
            return "Utilities";
        } else if (lowerDesc.contains("groceries") || lowerDesc.contains("supermarket") || lowerDesc.contains("costco")) {
            return "Groceries";
        } else if (lowerDesc.contains("gas") || lowerDesc.contains("petrol") || lowerDesc.contains("shell")) {
            return "Transport";
        } else if (lowerDesc.contains("online") || lowerDesc.contains("amazon") || lowerDesc.contains("purchase")) {
            return "Shopping";
        } else if (lowerDesc.contains("dinner") || lowerDesc.contains("restaurant")) {
            return "Dining";
        } else {
            return "Other";
        }
    }
    // -------------------------
    // ...
}
```



Step 3: Implement New Analytics Logic and Endpoints

We will focus these changes on the **`data-analytics-service`**.

* **Java (`data-analytics-service/repository/FactTransactionRepository.java`):**
  Add methods for category analysis and comparative analysis using the new `category` **field (which you should ensure is added to this entity).**
  java

  ```
  // In data-analytics-service/src/main/java/com/hibersoft/ms/bankcustomer/analytics/repository/FactTransactionRepository.java
  // ... (imports) ...
  import java.util.List;
  import java.math.BigDecimal;
  import org.springframework.data.jpa.repository.Query;
  import org.springframework.data.repository.query.Param;

  @Repository
  public interface FactTransactionRepository extends JpaRepository<FactTransactionEntity, Long> {
      // ... (existing queries) ...

      // Query for spending by category
      @Query("SELECT f.category, SUM(f.amountStandard) FROM FactTransactionEntity f GROUP BY f.category")
      List<Object[]> findSpendingByCategory();

      // Query for average transaction amount, useful for comparative analysis
      @Query("SELECT AVG(f.amountStandard) FROM FactTransactionEntity f")
      BigDecimal findOverallAverageTransactionAmount();

      @Query("SELECT AVG(f.amountStandard) FROM FactTransactionEntity f WHERE f.bankId = :bankId")
      BigDecimal findAverageTransactionAmountByBank(@Param("bankId") String bankId);
  }
  ```
* **Java (`data-analytics-service/service/DataAnalyticsService.java`):**
  Add corresponding service methods that call the new repository methods.
  java

  ```
  // In data-analytics-service/src/main/java/com/hibersoft/ms/bankcustomer/analytics/service/DataAnalyticsService.java
  // ... (imports) ...
  import java.util.stream.Collectors;
  import java.util.Map;
  import java.math.BigDecimal;
  import java.util.HashMap;

  @Service
  public class DataAnalyticsService {
      // ... (autowired repository and existing methods) ...

      public Map<String, BigDecimal> getSpendingByCategory() {
          // Note the casting might need adjustment based on runtime types in Object[]
          List<Object[]> results = repository.findSpendingByCategory();
          return results.stream().collect(Collectors.toMap(
              result -> (String) result[0], // Category name
              result -> (BigDecimal) result[1] // Sum of amount
          ));
      }

      public Map<String, BigDecimal> getComparativeAverageSpending(String bankId) {
          BigDecimal overallAvg = repository.findOverallAverageTransactionAmount();
          BigDecimal bankAvg = repository.findAverageTransactionAmountByBank(bankId);

          Map<String, BigDecimal> comparison = new HashMap<>();
          comparison.put("Overall_Platform_Average", overallAvg != null ? overallAvg : BigDecimal.ZERO);
          comparison.put(bankId + "_Average", bankAvg != null ? bankAvg : BigDecimal.ZERO);

          return comparison;
      }
  }
  ```
* **Java (`data-analytics-service/controller/DataAnalyticsController.java`):**
  Expose the new service methods via new REST endpoints.
  java

  ```
  // In data-analytics-service/src/main/java/com/hibersoft/ms/bankcustomer/analytics/controller/DataAnalyticsController.java
  // ... (imports) ...

  @RestController
  @RequestMapping("/api/v1/analytics")
  public class DataAnalyticsController {
      // ... (existing endpoints) ...

      @GetMapping("/spending-by-category")
      public ResponseEntity<Map<String, BigDecimal>> getSpendingByCategory() {
          Map<String, BigDecimal> spending = analyticsService.getSpendingByCategory();
          return ResponseEntity.ok(spending);
      }

      @GetMapping("/compare-spending/{bankId}")
      public ResponseEntity<Map<String, BigDecimal>> compareSpending(@PathVariable String bankId) {
          Map<String, BigDecimal> comparison = analyticsService.getComparativeAverageSpending(bankId);
          return ResponseEntity.ok(comparison);
      }
  }
  ```
