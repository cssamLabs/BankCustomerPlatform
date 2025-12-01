package com.hibersoft.ms.bankcustomer.datamodeling.service;

import com.hibersoft.ms.bankcustomer.datamodeling.model.IngestionEventPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobLaunchRequestServiceTest {

    // InjectMocks automatically injects the Mocks into the service under test
    @InjectMocks
    private JobLaunchRequestService jobLaunchRequestService;

    // Mock dependencies of the JobLaunchRequestService
    @Mock
    private JobLauncher jobLauncher;
    @Mock
    private Job bankCustomerBehaviorModelJob;

    @Test
    public void testLaunchModelingJob_FromKafkaPayload_LaunchesWithCorrectParams() throws Exception {
        // Arrange
        IngestionEventPayload payload = new IngestionEventPayload();
        payload.setBankId("BANK_A");
        payload.setBatchId("B123");
        payload.setDataLocationURI("s3://test-bucket/data.csv");
        
        // Mock the jobLauncher.run() method return
        when(jobLauncher.run(eq(bankCustomerBehaviorModelJob), any(JobParameters.class)))
                .thenReturn(mock(JobExecution.class));

        // Act
        jobLaunchRequestService.launchModelingJob(payload);

        // Assert
        // Use ArgumentCaptor to capture the JobParameters that the launcher received
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher, times(1)).run(eq(bankCustomerBehaviorModelJob), captor.capture());
        
        JobParameters capturedParams = captor.getValue();
        
        // Verify that the captured parameters match the payload data
        assertEquals("BANK_A", capturedParams.getString("bankId"));
        assertEquals("B123", capturedParams.getString("batchId"));
        assertEquals("s3://test-bucket/data.csv", capturedParams.getString("inputUri"));
        // assertTrue(capturedParams.getParameters().containsKey("triggerTimestamp"), "The triggerTimestamp parameter should be present");
        // assertNotNull(capturedParams.getLong("triggerTimestamp"), "The timestamp value should not be null");
    }

    @Test
    public void testLaunchModelingJobManually_FromRestController_LaunchesWithCorrectParams() throws Exception {
        // Arrange
        final String manualBankId = "BANK_B_MANUAL";
        final String manualInputUri = "/local/path/file.csv";

        // Mock the jobLauncher.run() method return
        when(jobLauncher.run(eq(bankCustomerBehaviorModelJob), any(JobParameters.class)))
                .thenReturn(mock(JobExecution.class));

        // Act
        jobLaunchRequestService.launchModelingJobManually(manualBankId, manualInputUri);

        // Assert
        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher, times(1)).run(eq(bankCustomerBehaviorModelJob), captor.capture());

        JobParameters capturedParams = captor.getValue();

        // Verify that the captured parameters match the manual input data
        assertEquals(manualBankId, capturedParams.getString("bankId"));
        assertEquals(manualInputUri, capturedParams.getString("inputUri"));
        assertNull(capturedParams.getString("batchId")); // Should be null in manual run
        assertNotNull(capturedParams.getLong("manualRunId")); // Should have a dynamic run ID for uniqueness
    }
}
