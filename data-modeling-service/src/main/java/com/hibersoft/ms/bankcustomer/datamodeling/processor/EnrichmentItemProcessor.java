package com.hibersoft.ms.bankcustomer.datamodeling.processor;

import com.hibersoft.ms.bankcustomer.datamodeling.model.EnrichedBankTransaction;
import com.hibersoft.ms.bankcustomer.datamodeling.model.CustomerMdmEntity;
import com.hibersoft.ms.bankcustomer.datamodeling.repository.CustomerMdmRepository;
import com.hibersoft.ms.bankcustomer.datamodeling.model.FactTransactionEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/*
Caching: A local ConcurrentHashMap is used as a simple cache. For a single-threaded batch step, this drastically reduces the number of database round-trips when the same account ID appears multiple times in a data chunk.

Performance: This mitigates the N+1 query problem that often cripples batch performance when calling a database repeatedly within an ItemProcessor loop.

Context: Note that this code is provided conceptually for if you needed a Java-based ETL. In our current SQL-based ELT flow, this specific Java class is not wired into the BatchConfiguration, as the SQL approach is more appropriate for large-scale data warehousing transformations. 

The data modeling service is now robust, using an event-driven Kafka trigger and a highly efficient Spring Batch ELT pipeline for batch processing.
*/

@Component
public class EnrichmentItemProcessor implements ItemProcessor<EnrichedBankTransaction, FactTransactionEntity> {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentItemProcessor.class);

    private final CustomerMdmRepository mdmRepository;
    Map<String, String> customerIdCache = new ConcurrentHashMap<>();

    @Autowired
    public EnrichmentItemProcessor(CustomerMdmRepository mdmRepository) {
        this.mdmRepository = mdmRepository;
    }

    // @Override
    // public EnrichedBankTransaction process(final EnrichedBankTransaction transaction) throws Exception {
        
    //     String bankAccountId = transaction.getBankSpecificAccountId();

    //     if (bankAccountId == null || bankAccountId.isEmpty()) {
    //         transaction.setIsValid(false);
    //         return transaction;
    //     }

    //     // --- Enhancement: Check cache first ---
    //     String unifiedCustomerId = customerIdCache.get(bankAccountId);

    //     if (unifiedCustomerId == null) {
    //         // Not in cache, perform database lookup
    //         Optional<CustomerMdmEntity> mdmEntry = mdmRepository.findByBankSpecificAccountId(bankAccountId);

    //         if (mdmEntry.isPresent()) {
    //             unifiedCustomerId = mdmEntry.get().getUnifiedCustomerId();
    //             // Store in cache for subsequent lookups during this batch run
    //             customerIdCache.put(bankAccountId, unifiedCustomerId); 
    //             log.debug("DB lookup performed and cached for account: {}", bankAccountId);
    //         } else {
    //             log.warn("Could not find unified Customer ID for account: {}", bankAccountId);
    //             transaction.setIsValid(false);
    //             return transaction; // Return the invalid transaction
    //         }
    //     } else {
    //         log.debug("Cache hit for account: {}", bankAccountId);
    //     }
        
    //     // Final enrichment with the unified ID
    //     transaction.setCustomerId(unifiedCustomerId);
    //     transaction.setIsValid(true); // Reset to valid as enrichment succeeded

    //     return transaction;
    // }

    @Override
    // The process method now must return FactTransactionEntity
    public FactTransactionEntity process(final EnrichedBankTransaction transaction) throws Exception {
        String bankAccountId = transaction.getBankSpecificAccountId();

        if (bankAccountId == null || bankAccountId.isEmpty()) {
            transaction.setIsValid(false);
            return null;
        }

        // --- Enhancement: Check cache first ---
        String unifiedCustomerId = customerIdCache.get(bankAccountId);

        if (unifiedCustomerId == null) {
            // Not in cache, perform database lookup
            Optional<CustomerMdmEntity> mdmEntry = mdmRepository.findByBankSpecificAccountId(bankAccountId);

            if (mdmEntry.isPresent()) {
                unifiedCustomerId = mdmEntry.get().getUnifiedCustomerId();
                // Store in cache for subsequent lookups during this batch run
                customerIdCache.put(bankAccountId, unifiedCustomerId); 
                log.debug("DB lookup performed and cached for account: {}", bankAccountId);
            } else {
                log.warn("Could not find unified Customer ID for account: {}", bankAccountId);
                transaction.setIsValid(false);
                return null; // Return the invalid transaction
            }
        } else {
            log.debug("Cache hit for account: {}", bankAccountId);
        }

        transaction.setCustomerId(unifiedCustomerId); // Set the retrieved unified ID
        transaction.setIsValid(true);
        
        FactTransactionEntity factEntity = new FactTransactionEntity();
        factEntity.setBankId(transaction.getBankId());
        factEntity.setCustomerId(transaction.getCustomerId());
        factEntity.setTransactionTime(transaction.getTransactionTime());
        factEntity.setAmountStandard(transaction.getAmountStandard());
        factEntity.setDescriptionStandard(transaction.getDescription()); 
        factEntity.setLocationCode(transaction.getLocationCode());
        factEntity.setTransactionType(transaction.getTransactionType());
        factEntity.setIsValid(true);

        return factEntity;
    }
}
