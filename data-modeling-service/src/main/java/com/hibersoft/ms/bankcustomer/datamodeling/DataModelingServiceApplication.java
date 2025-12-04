package com.hibersoft.ms.bankcustomer.datamodeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class DataModelingServiceApplication {
    
    private static final Logger log = LoggerFactory.getLogger(DataModelingServiceApplication.class);

    public static void main(String[] args) {
        log.info("Starting DataModelingServiceApplication...");
        SpringApplication.run(DataModelingServiceApplication.class, args);
        log.info("DataModelingServiceApplication started successfully.");
    }
}
