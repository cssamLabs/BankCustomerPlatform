package com.hibersoft.ms.bankcustomer.datamodeling.service;

import org.springframework.stereotype.Component;

@Component
public class DynamicPathService {
    public String getIngestionOutputPath(String bankId) {
        // Use a standard location and a unique name
        return "/opt/data/staging/ingestion_" + bankId + "_" + System.currentTimeMillis() + ".csv";
    }
}
