package com.hibersoft.ms.bankcustomer.datamodeling.processor;

import com.hibersoft.ms.bankcustomer.datamodeling.model.RawBankTransaction;
import com.hibersoft.ms.bankcustomer.datamodeling.model.EnrichedBankTransaction;
import org.springframework.batch.item.ItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Currency;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

@Component // Mark as component to be used in BatchConfiguration
public class CleansingItemProcessor implements ItemProcessor<RawBankTransaction, EnrichedBankTransaction> {

    private static final Logger log = LoggerFactory.getLogger(CleansingItemProcessor.class);
    // Assuming incoming dates are consistent across banks
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // Define a set of valid ISO currency codes we expect
    private static final Set<String> VALID_CURRENCIES = new HashSet<>(Arrays.asList("USD", "CAD", "EUR", "GBP"));

    @Override
    public EnrichedBankTransaction process(final RawBankTransaction rawTransaction) throws Exception {
        
        EnrichedBankTransaction enrichedTransaction = new EnrichedBankTransaction();

        // --- 1. Pass Through Required Fields for Later Steps ---
        enrichedTransaction.setBankSpecificAccountId(rawTransaction.getBankSpecificAccountId());
        
        // --- 2. Validation and Harmonization ---

        // A. Amount Validation/Conversion
        try {
            // Assume we standardize to USD internally
            BigDecimal amount = standardizeAmount(rawTransaction.getAmount(), "USD"); 
            enrichedTransaction.setAmountStandard(amount);
        } catch (NumberFormatException e) {
            log.error("Invalid amount format for transaction ID: {}", rawTransaction.getBankSpecificTransactionId());

            return null;
        }

        // B. Date/Time Parsing
        LocalDateTime transactionTime = parseDateTime(rawTransaction.getTransactionDate());
        if (transactionTime == null) {
            log.error("Invalid date format for transaction ID: {}", rawTransaction.getBankSpecificTransactionId());
   
            return null;
        } else {
            enrichedTransaction.setTransactionTime(transactionTime);
        }

        // C. Description Cleansing/Standardization
        enrichedTransaction.setDescription(cleanseDescription(rawTransaction.getDescription()));
        enrichedTransaction.setTransactionType(determineTransactionType(rawTransaction.getDescription()));

        // D. Location Code Validation (simple check)
        if (rawTransaction.getLocationCode() == null || rawTransaction.getLocationCode().length() != 5) {
             log.warn("Potential invalid location code: {}", rawTransaction.getLocationCode());
             // This might not fail the record entirely, but it's a data quality flag
        }
        enrichedTransaction.setLocationCode(rawTransaction.getLocationCode());

        // --- 3. Finalize Data Quality Flag ---
        enrichedTransaction.setIsValid(true);

        // If data is invalid, we still pass it through to staging with isValid=false 
        // to audit the error, but the SQL tasklet will filter it out of the final FACT table load.
        return enrichedTransaction;
    }

    // --- Helper Methods for Detailed Cleansing and Harmonization ---

    private BigDecimal standardizeAmount(String amountStr, String targetCurrencyCode) throws NumberFormatException {
        // In a real scenario, this involves currency conversion API calls or exchange rate lookups.
        // Here we just parse the string to a precise BigDecimal.
        return new BigDecimal(amountStr);
    }

    private LocalDateTime parseDateTime(String dateString) {
        try {
            return LocalDateTime.parse(dateString, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException | NullPointerException e) {
            return null; // Return null if parsing fails
        }
    }

    private String cleanseDescription(String description) {
        if (description == null) return "UNKNOWN";
        // Remove extraneous whitespace and force uppercase for consistency in analysis
        return description.trim().toUpperCase().replaceAll("\\s+", " ");
    }
    
    private String determineTransactionType(String description) {
        // Use a more detailed mapping system based on a reference table if possible
        if (description.toLowerCase().contains("atm")) return "WITHDRAWAL";
        if (description.toLowerCase().contains("payment")) return "PAYMENT";
        if (description.toLowerCase().contains("online transfer")) return "TRANSFER";
        return "POS_PURCHASE";
    }
}
