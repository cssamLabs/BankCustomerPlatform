package com.hibersoft.ms.bankcustomer.datamodeling.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawBankTransaction {
    private String bankSpecificTransactionId;
    private String bankSpecificAccountId;
    private String transactionDate;
    private String amount; // raw string format
    private String description;
    private String locationCode;
    // Add other raw fields as necessary
}
