package com.hibersoft.ms.bankcustomer.datamodeling.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MDM_CUSTOMER_MAP") // Table in the MDM database
@Getter
@Setter
public class CustomerMdmEntity {
    @Id
    private Long id;
    private String unifiedCustomerId; // The master ID we want
    private String bankSpecificAccountId;
    private String bankId;
    // ... other MDM fields
}
