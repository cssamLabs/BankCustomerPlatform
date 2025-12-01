package com.hibersoft.ms.bankcustomer.datamodeling.service;

import com.hibersoft.ms.bankcustomer.datamodeling.model.IngestionEventPayload;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JobLaunchRequestService {

    private static final Logger log = LoggerFactory.getLogger(JobLaunchRequestService.class);

    private final JobLauncher jobLauncher;
    // Autowire the specific job bean we will define in the next step
    private final Job bankCustomerBehaviorModelJob;

    @Autowired
    public JobLaunchRequestService(JobLauncher jobLauncher, Job bankCustomerBehaviorModelJob) {
        this.jobLauncher = jobLauncher;
        this.bankCustomerBehaviorModelJob = bankCustomerBehaviorModelJob;
    }

    public void launchModelingJob(IngestionEventPayload payload) throws Exception {
        log.info("Preparing to launch Spring Batch job for data from URI: {}", payload.getDataLocationURI());
        // Generate the dynamic path here using System.currentTimeMillis()
        String dynamicOutputPath = "/opt/data/staging/ingestion_" + payload.getBankId() + "_" + System.currentTimeMillis() + ".csv";


        // Create unique job parameters to ensure Spring Batch launches a new instance every time
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("bankId", payload.getBankId())
                .addString("batchId", payload.getBatchId())
                .addString("inputUri", payload.getDataLocationURI()) // This is key input parameter
                .addString("ingestion.output.path", dynamicOutputPath)
                .addLong("timestamp", System.currentTimeMillis()) // Unique timestamp to force new run
                .toJobParameters();

        jobLauncher.run(bankCustomerBehaviorModelJob, jobParameters);
        // log.info("Successfully launched job instance for batch {}", payload.getBatchId());
    }

    /**
     * Launches the modeling job manually via a REST API call.
     * Requires the bankId and input URI as explicit parameters.
     */
    public void launchModelingJobManually(String bankId, String inputUri) throws Exception {
        log.info("Manual launch requested for bank: {}, input URI: {}", bankId, inputUri);

        // We must ensure unique JobParameters so Spring Batch runs a new instance
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("bankId", bankId)
                .addString("inputUri", inputUri)
                // Use a different, mandatory parameter like a run ID to ensure uniqueness for manual runs
                .addLong("manualRunId", System.currentTimeMillis()) 
                .toJobParameters();

        jobLauncher.run(bankCustomerBehaviorModelJob, jobParameters);
        log.info("Successfully launched manual job instance for bank {}", bankId);
    }
}
