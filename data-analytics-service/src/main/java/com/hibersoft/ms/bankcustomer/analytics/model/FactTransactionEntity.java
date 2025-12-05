package com.hibersoft.ms.bankcustomer.analytics.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String category;
}
