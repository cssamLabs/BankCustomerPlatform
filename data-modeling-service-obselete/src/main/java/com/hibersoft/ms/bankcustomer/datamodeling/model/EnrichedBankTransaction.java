package com.hibersoft.ms.bankcustomer.datamodeling.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class EnrichedBankTransaction {
    // Standardized fields
    private String bankId;
    private String customerId; // The unified ID we are looking for

    // Temporary field needed for lookup (carried over from RawBankTransaction)
    private String bankSpecificAccountId; 

    private LocalDateTime transactionTime;
    private BigDecimal amountStandard;
    private String description;
    private String locationCode;
    private String transactionType;
    private boolean isValid;

    public boolean getIsValid() {
        return this.isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }
}
