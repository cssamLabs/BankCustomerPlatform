package com.hibersoft.ms.bankcustomer.analytics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "FACT_TRANSACTIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FactTransactionEntity {

    @Id // Primary key from the fact table
    private Long id; 
    
    private String bankId;
    private String customerId; // The unified/master ID
    private LocalDateTime transactionTime;
    private BigDecimal amountStandard;
    private String descriptionStandard; 
    private String locationCode;
    private String transactionType;
    private boolean isValid;
}
