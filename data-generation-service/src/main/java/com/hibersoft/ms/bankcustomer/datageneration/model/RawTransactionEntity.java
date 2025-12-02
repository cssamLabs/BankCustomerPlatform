package com.hibersoft.ms.bankcustomer.datageneration.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "bank_a_transactions") // Default table name
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RawTransactionEntity {

    @Id // Using the account ID as the ID for simplicity in generation
    private String bankSpecificAccountId;
    private String transactionDate; // Storing as String for now, as the modeling service expects this
    private String amount;
    private String description;
    private String locationCode;
}
