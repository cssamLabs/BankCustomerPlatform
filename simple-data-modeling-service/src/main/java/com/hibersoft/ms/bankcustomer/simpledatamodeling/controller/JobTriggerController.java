package com.hibersoft.ms.bankcustomer.simpledatamodeling.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/simple-ingestion")
public class JobTriggerController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job ingestBankDataJob; 

     private static final Logger log = LoggerFactory.getLogger(JobTriggerController.class);

    @PostMapping("/start/{bankId}")
    public ResponseEntity<Map<String, String>> startJob(@PathVariable String bankId) throws Exception {
        log.info("API requested job launch for Bank ID: {}", bankId);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("bankId", bankId)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        log.debug("Launching job with parameters: {}", jobParameters);
        jobLauncher.run(ingestBankDataJob, jobParameters);
        log.info("Job launch command sent successfully.");
        
        return ResponseEntity.ok(Map.of(
            "status", "STARTED",
            "message", "Simple ingestion job launched for " + bankId
        ));
    }

    @PostMapping("/start-all")
    public ResponseEntity<Map<String, String>> startJobForAllBanks() {
        log.info("API requested job launch for all banks");
        
        // In a real scenario, this would dynamically get a list of bank IDs
        List<String> allBankIds = Arrays.asList("BANK_A", "BANK_B"); // Example list
        
        allBankIds.forEach(bankId -> {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("bankId", bankId)
                    .addLong("run.id", System.currentTimeMillis() + bankId.hashCode())
                    .toJobParameters();
            try {
                jobLauncher.run(ingestBankDataJob, jobParameters);
                log.info("Job launched for Bank ID: {}", bankId);
            } catch (Exception e) {
                log.error("Failed to launch job for Bank ID: {}", bankId, e);
            }
        });

        return ResponseEntity.ok(Map.of(
            "status", "STARTED",
            "message", "Ingestion job launched for all configured banks"
        ));
    }
}