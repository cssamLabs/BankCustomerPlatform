package com.hibersoft.ms.bankcustomer.datamodeling.service;

import com.hibersoft.ms.bankcustomer.datamodeling.model.IngestionEventPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import java.util.List;
import java.util.stream.Collectors;

// Use MockitoExtension to initialize mocks
@ExtendWith(MockitoExtension.class)
public class KafkaProducerServiceTest {

    // InjectMocks automatically injects the Mocks into the service under test
    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    // Mock the KafkaTemplate dependency
    @Mock
    private KafkaTemplate<String, IngestionEventPayload> kafkaTemplate;

    private static final String EXPECTED_TOPIC = "topic.admin.ingestion-events";

    @Test
    public void testSendIngestionCompleteEvent_CallsKafkaTemplateSend() {
        // Arrange
        IngestionEventPayload testPayload = new IngestionEventPayload();
        testPayload.setBankId("TEST_BANK");
        testPayload.setEventType("INGESTION_COMPLETE");
        
        // Mock the return of the send operation (optional, as we are just verifying the call)
        // This avoids dealing with ListenableFuture intricacies for a simple verification test
        CompletableFuture<SendResult<String, IngestionEventPayload>> completedFuture = 
            CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(IngestionEventPayload.class))).thenReturn(completedFuture);

        // Act
        kafkaProducerService.sendIngestionCompleteEvent(testPayload);

        // Assert
        // Verify that kafkaTemplate.send was called exactly once with the correct topic name 
        // and the specific payload object we passed in.
        verify(kafkaTemplate, times(1)).send(eq(EXPECTED_TOPIC), eq(testPayload));
    }
}
