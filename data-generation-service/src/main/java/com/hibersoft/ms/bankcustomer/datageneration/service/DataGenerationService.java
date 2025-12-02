package com.hibersoft.ms.bankcustomer.datageneration.service;

import com.hibersoft.ms.bankcustomer.datageneration.model.RawTransactionEntity;
import com.hibersoft.ms.bankcustomer.datageneration.repository.RawTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class DataGenerationService {

    @Autowired
    private RawTransactionRepository repository;

    private final Random random = new Random();
    private final String[] descriptions = {"Groceries", "Gas", "Dinner", "Online Purchase", "ATM Withdrawal", "Deposit"};
    private final String[] locationCodes = {"L1", "L2", "L3", "L4", "L5"};

    @Transactional
    public int generateData(String bankId, int recordCount) {
        List<RawTransactionEntity> data = new ArrayList<>();
        
        // Note: This service currently targets only 'bank_a_transactions' via the @Table annotation.
        // For different banks (BANK_B), you would need to dynamically change the table name
        // or use native SQL/JdbcTemplate to insert into the correct table dynamically.
        // We stick to BANK_A for now for simplicity.

        for (int i = 0; i < recordCount; i++) {
            String accountId = "ACC" + bankId.charAt(bankId.length() - 1) + (1000 + random.nextInt(9000));
            String amount = String.format("%.2f", 10.0 + (90.0 * random.nextDouble()));
            String description = descriptions[random.nextInt(descriptions.length)];
            String location = locationCodes[random.nextInt(locationCodes.length)];
            String date = LocalDateTime.now().minusHours(random.nextInt(100)).toString();

            RawTransactionEntity entity = new RawTransactionEntity();
            entity.setBankSpecificAccountId(UUID.randomUUID().toString()); // Use UUID as PK for multiple inserts
            entity.setTransactionDate(date);
            entity.setAmount(amount);
            entity.setDescription(description);
            entity.setLocationCode(location);
            data.add(entity);
        }

        repository.saveAll(data);
        return data.size();
    }
}
