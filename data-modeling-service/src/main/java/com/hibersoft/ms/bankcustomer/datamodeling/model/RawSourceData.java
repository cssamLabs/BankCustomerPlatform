package com.hibersoft.ms.bankcustomer.datamodeling.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// This POJO maps to the column names in the bank source tables (e.g., bank_a_transactions)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RawSourceData {
    private String bankSpecificAccountId;
    private String transactionDate;
    private String amount;
    private String description;
    private String locationCode;
}
