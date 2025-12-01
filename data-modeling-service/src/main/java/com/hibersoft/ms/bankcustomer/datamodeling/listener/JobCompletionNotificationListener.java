package com.hibersoft.ms.bankcustomer.datamodeling.listener;

import com.hibersoft.ms.bankcustomer.datamodeling.model.IngestionEventPayload;
import com.hibersoft.ms.bankcustomer.datamodeling.service.KafkaProducerService;
import com.hibersoft.ms.bankcustomer.datamodeling.model.CustomerMdmEntity; 

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final KafkaProducerService producerService;

    @Autowired
    public JobCompletionNotificationListener(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == org.springframework.batch.core.BatchStatus.COMPLETED) {
            // Build the payload with the output URI derived from job parameters
            IngestionEventPayload payload = new IngestionEventPayload();
            // ... populate payload details ...
            payload.setEventType("INGESTION_COMPLETE");
            
            producerService.sendIngestionCompleteEvent(payload);
        } else if (jobExecution.getStatus() == org.springframework.batch.core.BatchStatus.FAILED) {
            // Handle failure notification
        }
    }
}