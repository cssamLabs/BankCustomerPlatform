package com.hibersoft.ms.bankcustomer.datageneration.service;

import com.hibersoft.ms.bankcustomer.datageneration.model.RawTransactionEntity;
import com.hibersoft.ms.bankcustomer.datageneration.repository.RawTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataGenerationService {

    // @Autowired
    // private RawTransactionRepository repository;
    @Autowired // Autowire JdbcTemplate instead
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();
    private final String[] descriptions = {"Groceries", "Gas", "Dinner", "Online Purchase", "ATM Withdrawal", "Deposit"};
    private final String[] locationCodes = {"L1", "L2", "L3", "L4", "L5"};

    @Transactional
    public int generateData(String bankId, int recordCount) {
        // ... (data generation loop creates List<RawTransactionEntity> data) ...
        List<RawTransactionEntity> data = new ArrayList<>();
        List<Object[]> mdmBatchArgs = new ArrayList<>();

        for (int i = 0; i < recordCount; i++) {
            String transactionUuid = UUID.randomUUID().toString(); 
            String unifiedCustomerId = "U_CUST_" + bankId + "_" + random.nextInt(9000);
            mdmBatchArgs.add(new Object[]{ transactionUuid, unifiedCustomerId }); 

            String accountId = "ACC" + bankId.charAt(bankId.length() - 1) + (1000 + random.nextInt(9000));
            String amount = String.format("%.2f", 10.0 + (90.0 * random.nextDouble()));
            String description = descriptions[random.nextInt(descriptions.length)];
            String location = locationCodes[random.nextInt(locationCodes.length)];
            String date = LocalDateTime.now().minusHours(random.nextInt(100)).toString();

            RawTransactionEntity entity = new RawTransactionEntity();
            entity.setBankSpecificAccountId(transactionUuid); // Use UUID as PK for multiple inserts
            entity.setTransactionDate(date);
            entity.setAmount(amount);
            entity.setDescription(description);
            entity.setLocationCode(location);
            data.add(entity);
        }

        // --- Use dynamic table name insertion via JdbcTemplate ---
        String tableName = bankId.toLowerCase() + "_transactions";
        String sql = "INSERT INTO " + tableName + " (bank_specific_account_id, transaction_date, amount, description, location_code) VALUES (?, ?, ?, ?, ?)";
        
        List<Object[]> batchArgs = data.stream().map(entity -> new Object[] {
            entity.getBankSpecificAccountId(), entity.getTransactionDate(), entity.getAmount(), entity.getDescription(), entity.getLocationCode()
        }).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
        // --------------------------------------------------------
        
        // 2. Insert into the MDM table
        String mdmSql = "INSERT INTO customer_mdm_entity (bank_specific_account_id, unified_customer_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(mdmSql, mdmBatchArgs);

        return data.size();
    }
}
