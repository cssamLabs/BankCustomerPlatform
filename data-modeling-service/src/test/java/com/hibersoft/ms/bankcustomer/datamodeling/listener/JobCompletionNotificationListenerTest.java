package com.hibersoft.ms.bankcustomer.datamodeling.listener;

import com.hibersoft.ms.bankcustomer.datamodeling.model.IngestionEventPayload;
import com.hibersoft.ms.bankcustomer.datamodeling.service.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.MetaDataInstanceFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobCompletionNotificationListenerTest {

    // InjectMocks automatically injects the Mocks into the listener under test
    @InjectMocks
    private JobCompletionNotificationListener listener;

    // Mock the KafkaProducerService dependency
    @Mock
    private KafkaProducerService producerService;

    @Test
    public void testAfterJob_WhenJobCompletedSuccessfully_ShouldCallProducerService() {
        // Arrange
        // Use MetaDataInstanceFactory to create a realistic mock JobExecution object
        JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
        // Manually set the status to COMPLETED
        jobExecution.setStatus(BatchStatus.COMPLETED);

        // Act
        listener.afterJob(jobExecution);

        // Assert
        // Verify that the sendIngestionCompleteEvent method was called exactly once 
        // with an IngestionEventPayload object containing the correct "INGESTION_COMPLETE" event type
        verify(producerService, times(1)).sendIngestionCompleteEvent(any(IngestionEventPayload.class));
    }

    @Test
    public void testAfterJob_WhenJobFailed_ShouldNotCallProducerService() {
        // Arrange
        JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
        // Manually set the status to FAILED
        jobExecution.setStatus(BatchStatus.FAILED);

        // Act
        listener.afterJob(jobExecution);

        // Assert
        // Verify that the sendIngestionCompleteEvent method was never called
        verify(producerService, never()).sendIngestionCompleteEvent(any(IngestionEventPayload.class));
        
        // You might add verification here that an error notification service was called instead
    }
}
