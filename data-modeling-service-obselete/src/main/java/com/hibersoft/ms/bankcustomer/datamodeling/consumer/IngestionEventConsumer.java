package com.hibersoft.ms.bankcustomer.datamodeling.consumer;

import com.hibersoft.ms.bankcustomer.datamodeling.model.IngestionEventPayload;
import com.hibersoft.ms.bankcustomer.datamodeling.service.JobLaunchRequestService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class IngestionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(IngestionEventConsumer.class);
    private static final String TOPIC_NAME = "topic.admin.ingestion-events";
    private static final String CONSUMER_GROUP_ID = "data-modeling-consumer-group";
    private static final String COMPLETE_EVENT_TYPE = "INGESTION_COMPLETE";

    private final JobLaunchRequestService jobLaunchRequestService;

    @Autowired
    public IngestionEventConsumer(JobLaunchRequestService jobLaunchRequestService) {
        this.jobLaunchRequestService = jobLaunchRequestService;
    }

    @KafkaListener(topics = TOPIC_NAME, groupId = CONSUMER_GROUP_ID,
                   containerFactory = "kafkaListenerContainerFactory") // Ensure you define this factory in config
    public void listen(IngestionEventPayload payload) {
        log.info("Received message from Kafka topic {}: {}", TOPIC_NAME, payload.getEventType());

        if (COMPLETE_EVENT_TYPE.equals(payload.getEventType())) {
            log.info("Processing complete event for bankId: {}, batchId: {}",
                     payload.getBankId(), payload.getBatchId());

            try {
                jobLaunchRequestService.launchModelingJob(payload);

            } catch (Exception e) {
                log.error("Failed to launch modeling job for payload: {}", payload, e);
            }
        } else if ("INGESTION_FAILED".equals(payload.getEventType())) {
             log.warn("Ingestion job failed for bank {}. Error: {}", payload.getBankId(), payload.getErrorMessage());
        }
    }
}