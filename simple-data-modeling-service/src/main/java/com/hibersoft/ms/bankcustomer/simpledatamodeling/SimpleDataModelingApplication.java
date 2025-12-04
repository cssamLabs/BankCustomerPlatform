package com.hibersoft.ms.bankcustomer.simpledatamodeling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.kafka.annotation.EnableKafka; // Add this

@SpringBootApplication
// @EnableKafka // Add this
public class SimpleDataModelingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimpleDataModelingApplication.class, args);
    }
}
