package com.hibersoft.ms.bankcustomer.simpledatamodeling.processor;

// ... (Add necessary imports: ItemProcessor, RawSourceData, FactTransactionEntity, Value, StepScope, Component, etc.) ...
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;

import com.hibersoft.ms.bankcustomer.simpledatamodeling.model.RawSourceData;
import com.hibersoft.ms.bankcustomer.simpledatamodeling.model.FactTransactionEntity;
import com.hibersoft.ms.bankcustomer.simpledatamodeling.model.RawSourceData;
import com.hibersoft.ms.bankcustomer.simpledatamodeling.service.AimlServiceCaller;


@Component
@StepScope // Make it Step Scoped to inject job parameters
public class EnrichmentItemProcessor implements ItemProcessor<RawSourceData, FactTransactionEntity> {

    // Inject the bankId using @Value and a Job Parameter placeholder
    @Value("#{jobParameters['bankId']}")
    private String bankId;

    @Autowired
    private AimlServiceCaller aimlService;

    // ... (Add the categorizeTransaction helper method here from
    // BatchConfiguration) ...
    private String categorizeTransaction(String description) {
        // ... (categorization logic) ...
        String lowerDesc = description.toLowerCase();
        if (lowerDesc.contains("utility") || lowerDesc.contains("hydro") || lowerDesc.contains("bell")
                || lowerDesc.contains("rogers")) {
            return "Utilities";
        } else if (lowerDesc.contains("groceries") || lowerDesc.contains("supermarket")
                || lowerDesc.contains("costco")) {
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

    @Override
    public FactTransactionEntity process(RawSourceData rawData) throws Exception {
        FactTransactionEntity fact = new FactTransactionEntity();

        // --- USE DYNAMIC BANKID FROM INJECTION ---
        fact.setBankId(this.bankId.toUpperCase());
        // -----------------------------------------

        fact.setTransactionTime(LocalDateTime.parse(rawData.getTransactionDate()));
        fact.setAmountStandard(new BigDecimal(rawData.getAmount()));
        fact.setDescriptionStandard(rawData.getDescription());
        fact.setLocationCode(rawData.getLocationCode());

        // --- USE DYNAMIC TRANSACTION TYPE (INFERRED FROM DESCRIPTION) ---
        fact.setTransactionType(rawData.getAmount().startsWith("-") ? "DEBIT" : "CREDIT");
        // --------------------------------------------------------------

        fact.setCustomerId("U_" + rawData.getBankSpecificAccountId());
        fact.setCategory(categorizeTransaction(rawData.getDescription()));

        // Call the AI/ML service (Conceptual for now) ---
        // The processor only has single transactions, so we can't aggregate yet. 
        // This call must be deferred until data is aggregated.
        // In a real implementation: The batch job *first* inserts data into the fact table, 
        // and a *second* job/step aggregates the data and calls the AI/ML service in bulk.
        // and we can use a placeholder for the segment:
        fact.setCustomerSegment("Not Segmented Yet"); 

        return fact;
    }
}
