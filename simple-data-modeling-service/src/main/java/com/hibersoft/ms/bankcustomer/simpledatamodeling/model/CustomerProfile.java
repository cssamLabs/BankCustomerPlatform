package com.hibersoft.ms.bankcustomer.simpledatamodeling.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile {
    @JsonProperty("Utilities")
    private String utilities;
    @JsonProperty("Groceries")
    private String groceries;
    @JsonProperty("Transport")
    private String transport;
    @JsonProperty("Shopping")
    private String shopping;
    @JsonProperty("Dining")
    private String dining;
    @JsonProperty("Other")
    private String other;
}