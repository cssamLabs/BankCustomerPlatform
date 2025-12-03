package com.hibersoft.ms.bankcustomer.datamodeling.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// This maps to FACT_TRANSACTIONS table structure
@Entity
@Table(name = "FACT_TRANSACTIONS")
@Getter
@Setter
public class FactTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Simplified fields for demonstration
    private String customerId; 
    private String descriptionStandard;
    private String locationCode;
}
