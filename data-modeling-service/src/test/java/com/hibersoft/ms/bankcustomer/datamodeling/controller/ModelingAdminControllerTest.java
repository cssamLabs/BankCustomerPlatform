package com.hibersoft.ms.bankcustomer.datamodeling.controller;

import com.hibersoft.ms.bankcustomer.datamodeling.service.JobLaunchRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


// This annotation focuses the Spring context loading only on the web layer (Controller)
@ExtendWith(MockitoExtension.class)
public class ModelingAdminControllerTest {

    // InjectMocks automatically injects the Mocks into the controller under test
    @InjectMocks
    private ModelingAdminController controller;

    // Mock dependencies of the controller
    @Mock
    private JobLaunchRequestService jobLaunchRequestService;
    @Mock
    private JobExplorer jobExplorer;

    private static final String API_BASE_URL = "/api/v1/modeling";

    @Test
    public void testTriggerManualJobEndpoint() throws Exception {
        final String bankId = "BANK_A_LTD";
        final String inputUri = "/path/to/data.csv";

        // ACT: Call the actual controller method directly, no mockMvc needed
        ResponseEntity<Map<String, String>> response = controller.triggerManualJob(bankId, inputUri);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("status"));
        assertEquals("SUCCESS", response.getBody().get("status"));

        verify(jobLaunchRequestService, times(1)).launchModelingJobManually(bankId, inputUri);

    }

    @Test
    public void testGetJobStatusEndpoint_JobFound() throws Exception {
        final Long jobId = 105L;
        final String expectedStatus = "COMPLETED";

        // Create a mock JobExecution object with realistic data
        JobExecution mockExecution = new JobExecution(jobId);
        mockExecution.setStatus(BatchStatus.COMPLETED);
        mockExecution.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        mockExecution.setEndTime(LocalDateTime.of(2023, 1, 1, 10, 5));
        mockExecution.setExitStatus(new ExitStatus("COMPLETED", "All steps finished"));

        // Mock the JobExplorer response
        when(jobExplorer.getJobExecution(jobId)).thenReturn(mockExecution);

         // ACT: Call the actual controller method directly
        ResponseEntity<Map<String, String>> response = controller.getJobStatus(jobId);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("COMPLETED", response.getBody().get("status"));

        verify(jobExplorer, times(1)).getJobExecution(jobId);
    }
    
    @Test
    public void testGetJobStatusEndpoint_JobNotFound() throws Exception {
        final Long jobId = 999L;

        // Mock the JobExplorer response for a missing job (returns null)
        when(jobExplorer.getJobExecution(jobId)).thenReturn(null);

        // ACT: Call the actual controller method directly
        ResponseEntity<Map<String, String>> response = controller.getJobStatus(jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("JOB_NOT_FOUND", response.getBody().get("status"));
        
        verify(jobExplorer, times(1)).getJobExecution(jobId);
    }
}
