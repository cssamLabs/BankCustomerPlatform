package com.hibersoft.ms.bankcustomer.datamodeling.model;

import lombok.Getter;
import lombok.Setter;
// ... constructors, getters/setters
@Getter
@Setter
public class RawSourceData {
    private String bankSpecificAccountId;
    private String transactionDate;
    private String amount;
    private String description;
    private String locationCode;
}
