package com.hibersoft.ms.bankcustomer.datamodeling.controller;

import com.hibersoft.ms.bankcustomer.datamodeling.service.IngestionJobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController; // Must be here
import org.springframework.web.bind.annotation.RequestMapping; // Must be here


@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionAdminController {

    private final IngestionJobService jobService;

    @Autowired
    public IngestionAdminController(IngestionJobService jobService) {
        this.jobService = jobService;
    }

    // POST /api/v1/ingestion/start/{bankId}
    @PostMapping("/start/{bankId}")
    public ResponseEntity<Map<String, String>> startIngestionJob(@PathVariable String bankId) {
        try {
            JobExecution execution = jobService.triggerJob(bankId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "STARTED");
            response.put("jobExecutionId", String.valueOf(execution.getId()));
            response.put("message", "Ingestion job launched.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "FAILED_TO_LAUNCH");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // GET /api/v1/ingestion/status/{jobId}
    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, String>> getJobStatus(@PathVariable Long jobId) {
        String status = jobService.getJobStatus(jobId);
        Map<String, String> response = new HashMap<>();
        response.put("jobExecutionId", String.valueOf(jobId));
        response.put("status", status);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/ingestion/schedule/pause/{bankId}
    @PostMapping("/schedule/pause/{bankId}")
    public ResponseEntity<String> pauseScheduler(@PathVariable String bankId) {
        // This is a placeholder for actual scheduler management logic
        jobService.pauseScheduler(bankId);
        return ResponseEntity.ok("Scheduler pause command sent for Bank ID: " + bankId);
    }

    // GET /actuator/health
    // This endpoint is automatically handled by the Spring Boot Actuator dependency.
}
