package com.hibersoft.ms.bankcustomer.datamodeling.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngestionEventPayload {
    private String eventId;
    private String timestamp;
    private String eventType;
    private String bankId;
    private String batchId;
    private String dataLocationURI; // The crucial output path
    private Integer recordCount;
    private String errorMessage;
}
