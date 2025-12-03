package com.hibersoft.ms.bankcustomer.datamodeling.model;

import lombok.Getter;
import lombok.Setter;

// This maps to bank_a_transactions table structure
@Getter
@Setter
public class RawSourceData {
    private String bankSpecificAccountId;
    private String transactionDate;
    private String amount;
    private String description;
    private String locationCode;
}
