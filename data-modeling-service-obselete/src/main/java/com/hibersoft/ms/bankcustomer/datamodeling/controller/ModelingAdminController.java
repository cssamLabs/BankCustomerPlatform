package com.hibersoft.ms.bankcustomer.datamodeling.controller;

import com.hibersoft.ms.bankcustomer.datamodeling.service.JobLaunchRequestService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/modeling") // Base path for all modeling admin APIs
public class ModelingAdminController {

    private final JobLaunchRequestService jobLaunchRequestService;
    private final JobExplorer jobExplorer; // Injected to check job status

    @Autowired
    public ModelingAdminController(JobLaunchRequestService jobLaunchRequestService, JobExplorer jobExplorer) {
        this.jobLaunchRequestService = jobLaunchRequestService;
        this.jobExplorer = jobExplorer;
    }

    // Maps to: POST /api/v1/modeling/trigger-manual-job (We used this in previous steps)
    @PostMapping("/trigger-manual-job")
    public ResponseEntity<Map<String, String>> triggerManualJob(@RequestParam String bankId, @RequestParam String inputUri) {
        try {
           jobLaunchRequestService.launchModelingJobManually(bankId, inputUri); 
           Map<String, String> response = new HashMap<>();
           response.put("status", "SUCCESS");
           response.put("message", "Modeling job launched successfully for bankId: " + bankId);
           return ResponseEntity.ok(response);
        } catch (Exception e) {
             Map<String, String> response = new HashMap<>();
             response.put("status", "FAILED");
             response.put("error", e.getMessage());
             return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint to retrieve the status of an ongoing data modelling ETL job.
     * Maps to: GET /api/v1/modeling/status/{jobId}
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, String>> getJobStatus(@PathVariable Long jobId) {
        // How it gets served:
        // 1. Uses the injected JobExplorer (provided by Spring Batch) to query its DB tables.
        JobExecution jobExecution = jobExplorer.getJobExecution(jobId);

        Map<String, String> response = new HashMap<>();
        if (jobExecution != null) {
            response.put("jobExecutionId", String.valueOf(jobId));
            response.put("status", jobExecution.getStatus().toString()); // e.g., STARTED, COMPLETED, FAILED
            response.put("startTime", jobExecution.getStartTime().toString());
            response.put("endTime", Optional.ofNullable(jobExecution.getEndTime()).map(Object::toString).orElse("N/A"));
            response.put("exitMessage", jobExecution.getExitStatus().getExitDescription());
        } else {
            response.put("status", "JOB_NOT_FOUND");
            response.put("message", "No execution found with ID: " + jobId);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for triggering a reconciliation job.
     * Maps to: POST /api/v1/modeling/trigger-reconciliation
     * NOTE: This assumes you define a separate 'reconciliationJob' bean in your BatchConfiguration
     */
    @PostMapping("/trigger-reconciliation")
    public ResponseEntity<String> triggerReconciliationJob() {
        // In a full implementation, you would have a specific 'reconciliationJob' 
        // defined in BatchConfiguration and launched here using the service layer.
        // jobLaunchRequestService.launchReconciliationJob(); 
        return ResponseEntity.ok("Reconciliation job triggered (implementation pending).");
    }


    /**
     * Metrics Endpoint
     * Maps to: GET /actuator/metrics
     */
    // This endpoint is automatically exposed by Spring Boot Actuator. 
    // It requires no custom controller code. It reports metrics including 
    // details about batch job execution times and throughput.
}
