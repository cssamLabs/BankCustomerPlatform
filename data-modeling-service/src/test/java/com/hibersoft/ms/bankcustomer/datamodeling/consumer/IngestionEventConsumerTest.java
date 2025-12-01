package com.hibersoft.ms.bankcustomer.datamodeling.consumer;

import com.hibersoft.ms.bankcustomer.datamodeling.model.IngestionEventPayload;
import com.hibersoft.ms.bankcustomer.datamodeling.service.JobLaunchRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngestionEventConsumerTest {

    // InjectMocks automatically injects the Mocks into the consumer under test
    @InjectMocks
    private IngestionEventConsumer ingestionEventConsumer;

    // Mock the service dependency
    @Mock
    private JobLaunchRequestService jobLaunchRequestService;

    @Test
    public void testListen_WhenIngestionCompleteEventReceived_ShouldLaunchJob() throws Exception {
        // Arrange
        IngestionEventPayload completePayload = new IngestionEventPayload();
        completePayload.setEventType("INGESTION_COMPLETE");
        completePayload.setBankId("BANK_A_LTD");
        completePayload.setDataLocationURI("/test/uri/data.csv");

        // We use doNothing() because the service method we are testing for does not return a value
        doNothing().when(jobLaunchRequestService).launchModelingJob(completePayload);

        // Act
        // Simulate an event coming in from Kafka
        ingestionEventConsumer.listen(completePayload);

        // Assert
        // Verify that the job launch service was called exactly once with the correct payload
        verify(jobLaunchRequestService, times(1)).launchModelingJob(completePayload);
    }

    @Test
    public void testListen_WhenIngestionFailedEventReceived_ShouldNotLaunchJob() throws Exception {
        // Arrange
        IngestionEventPayload failedPayload = new IngestionEventPayload();
        failedPayload.setEventType("INGESTION_FAILED");
        failedPayload.setBankId("BANK_B_LTD");
        failedPayload.setErrorMessage("DB Connection Failed");

        // Act
        // Simulate a failed event coming in from Kafka
        ingestionEventConsumer.listen(failedPayload);

        // Assert
        // Verify that the job launch service was NEVER called, because the event type was FAILED
        verify(jobLaunchRequestService, never()).launchModelingJob(any(IngestionEventPayload.class));
        
        // You might add verification here that a notification service was called to log the failure
    }
}
