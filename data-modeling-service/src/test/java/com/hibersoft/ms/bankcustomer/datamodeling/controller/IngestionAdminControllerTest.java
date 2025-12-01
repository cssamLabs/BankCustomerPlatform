package com.hibersoft.ms.bankcustomer.datamodeling.controller;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import org.springframework.batch.core.JobExecution;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hibersoft.ms.bankcustomer.datamodeling.service.IngestionJobService;

import static org.junit.jupiter.api.Assertions.assertEquals;

// This annotation focuses the Spring context loading only on the web layer (Controller)
@ExtendWith(MockitoExtension.class)
public class IngestionAdminControllerTest {

   // InjectMocks automatically injects the Mocks into the controller under test
    @InjectMocks
    private IngestionAdminController controller;

    // Mock the service layer dependency
    @Mock
    private IngestionJobService jobService;

    private static final String API_BASE_URL = "/api/v1/ingestion";

    @Test
    public void testStartIngestionJobEndpoint() throws Exception {
        final String bankId = "BANK_B";
        final Long expectedJobId = 101L;

       
        // Mock the service layer response using a real JobExecution mock
        JobExecution mockExecution = mock(JobExecution.class);
        when(mockExecution.getId()).thenReturn(expectedJobId);
        when(jobService.triggerJob(bankId)).thenReturn(mockExecution);

        // ACT: Call the actual controller method directly
        ResponseEntity<Map<String, String>> response = controller.startIngestionJob(bankId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("STARTED", response.getBody().get("status"));
        assertEquals(String.valueOf(expectedJobId), response.getBody().get("jobExecutionId"));

        verify(jobService, times(1)).triggerJob(bankId);
    }

    @Test
    public void testGetJobStatusEndpoint() throws Exception {
        final Long jobId = 101L;
        final String expectedStatus = "COMPLETED";

        // Mock the service layer response for the status check
        when(jobService.getJobStatus(jobId)).thenReturn(expectedStatus);

        // ACT: Call the actual controller method directly
        ResponseEntity<Map<String, String>> response = controller.getJobStatus(jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.valueOf(jobId), response.getBody().get("jobExecutionId"));
        assertEquals(expectedStatus, response.getBody().get("status"));

        verify(jobService, times(1)).getJobStatus(jobId);
    }

    @Test
    public void testPauseSchedulerEndpoint() throws Exception {
        final String bankId = "BANK_C";

        // Mock the service response
        when(jobService.pauseScheduler(bankId)).thenReturn(true);
        // ACT: Call the actual controller method directly
        ResponseEntity<String> response = controller.pauseScheduler(bankId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Scheduler pause command sent for Bank ID: " + bankId, response.getBody());

        verify(jobService, times(1)).pauseScheduler(bankId);
    }

    // Helper method to create a minimal mock JobExecution object for the 'start' test
    private org.springframework.batch.core.JobExecution createMockJobExecution(Long id) {
        // We only need to return an object with a valid ID
        return new org.springframework.batch.core.JobExecution(id);
    }
}
