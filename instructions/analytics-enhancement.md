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
// In data-modeling-service/src/main/java/com/hibersoft/ms/bankcustomer/datamodeling/config/BatchConfiguration.java

// ... (imports and existing processor bean definition) ...

@Bean
public ItemProcessor<RawSourceData, FactTransactionEntity> processor() {
    return rawData -> {
        log.info("Processing RawSourceData: Account ID={}, Amount={}", rawData.getBankSpecificAccountId(), rawData.getAmount());
        FactTransactionEntity fact = new FactTransactionEntity();
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
    if (lowerDesc.contains("utility") || lowerDesc.contains("hydro") || lowerDesc.contains("bell")) {
        return "Utilities";
    } else if (lowerDesc.contains("groceries") || lowerDesc.contains("supermarket")) {
        return "Groceries";
    } else if (lowerDesc.contains("gas") || lowerDesc.contains("petrol")) {
        return "Transport";
    } else if (lowerDesc.contains("online") || lowerDesc.contains("amazon") || lowerDesc.contains("purchase")) {
        return "Shopping";
    } else {
        return "Other";
    }
}
// -------------------------
```
