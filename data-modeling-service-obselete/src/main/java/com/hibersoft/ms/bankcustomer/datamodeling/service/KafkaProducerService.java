package com.hibersoft.ms.bankcustomer.datamodeling.service;

import com.hibersoft.ms.bankcustomer.datamodeling.model.IngestionEventPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "topic.admin.ingestion-events";

    private final KafkaTemplate<String, IngestionEventPayload> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, IngestionEventPayload> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendIngestionCompleteEvent(IngestionEventPayload payload) {
        log.info("Sending IngestionComplete event for batch {} to Kafka topic {}", payload.getBatchId(), TOPIC);
        kafkaTemplate.send(TOPIC, payload);
    }
}
