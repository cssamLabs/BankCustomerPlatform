package com.hibersoft.ms.bankcustomer.datamodeling.controller;

// ... (imports) ...
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingestion")
public class JobTriggerController {
    
    private static final Logger log = LoggerFactory.getLogger(JobTriggerController.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job ingestBankDataJob; 

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
                log.debug("Launching job for bankId: {}", bankId);
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
