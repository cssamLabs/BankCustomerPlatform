package com.hibersoft.ms.bankcustomer.datamodeling.service;

import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class IngestionJobService {

    private static final Logger log = LoggerFactory.getLogger(IngestionJobService.class);

    private final JobLauncher jobLauncher;
    // This job bean must be defined in the IngestionBatchConfiguration class
    private final Job ingestBankDataJob; 
    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;

    @Autowired
    public IngestionJobService(JobLauncher jobLauncher, Job ingestBankDataJob, JobRepository jobRepository, JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.ingestBankDataJob = ingestBankDataJob;
        this.jobRepository = jobRepository;
        this.jobExplorer = jobExplorer;
    }

    /**
     * Manually triggers the data extraction job for a specific bank ID via REST call.
     * Maps to: POST /api/v1/ingestion/start/{bankId}
     */
    public JobExecution triggerJob(String bankId) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        // We use 'run.id' to ensure that even if the other parameters are the same, 
        // Spring Batch treats this as a unique job instance, allowing it to run again.
        JobParameters params = new JobParametersBuilder()
                .addString("bankId", bankId)
                .addLong("run.id", System.currentTimeMillis()) 
                .toJobParameters();
        
        log.info("Launching ingestion job for bank: {}", bankId);
        return jobLauncher.run(ingestBankDataJob, params);
    }

    /**
     * Retrieves the status of a running or completed ingestion job.
     * Maps to: GET /api/v1/ingestion/status/{jobId}
     */
    public String getJobStatus(Long jobExecutionId) {
        JobExecution execution = jobExplorer.getJobExecution(jobExecutionId);
        if (execution != null) {
            return execution.getStatus().toString();
        } else {
            return "JOB_NOT_FOUND";
        }
    }

    /**
     * A placeholder method for pausing the scheduler.
     * Maps to: POST /api/v1/ingestion/schedule/pause/{bankId}
     */
    public boolean pauseScheduler(String bankId) {
        // The actual implementation depends on how you configure your scheduler (e.g., Quartz).
        // This is where you would call the scheduler management API to pause the trigger associated with the bankId.
        log.warn("Scheduler pause requested for bank {}. Requires actual scheduler implementation.", bankId);
        return true;
    }
}
