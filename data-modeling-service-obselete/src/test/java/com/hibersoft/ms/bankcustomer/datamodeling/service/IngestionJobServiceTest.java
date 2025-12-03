package com.hibersoft.ms.bankcustomer.datamodeling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.explore.JobExplorer; 

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;

// Use MockitoExtension to initialize mocks
@ExtendWith(MockitoExtension.class)
public class IngestionJobServiceTest {

    // InjectMocks automatically injects the Mocks into the service under test
    @InjectMocks
    private IngestionJobService ingestionJobService;

    // Mock dependencies of IngestionJobService
    @Mock
    private JobLauncher jobLauncher;
    @Mock
    private Job ingestionJob;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobExplorer jobExplorer;

    private static final String TEST_BANK_ID = "TEST_BANK";

    @BeforeEach
    public void setUp() {
        // Common setup if needed
    }

    @Test
    public void testTriggerJob_SuccessfullyLaunchesJob() throws Exception {
        // Arrange
        JobExecution mockExecution = new JobExecution(1L);
        // Mock the behavior of jobLauncher.run() when called with any job and parameters
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(mockExecution);

        // Act
        JobExecution result = ingestionJobService.triggerJob(TEST_BANK_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        // Verify that jobLauncher.run was called exactly once
        verify(jobLauncher, times(1)).run(eq(ingestionJob), any(JobParameters.class));
    }

    @Test
    public void testGetJobStatus_JobFound() {
        // Arrange
        final Long executionId = 2L;
        JobExecution mockExecution = new JobExecution(executionId);
        mockExecution.setStatus(BatchStatus.COMPLETED);
        
        // Mock the behavior of jobRepository.getJobExecution()
        when(jobExplorer.getJobExecution(executionId)).thenReturn(mockExecution);

        // Act
        String status = ingestionJobService.getJobStatus(executionId);

        // Assert
        assertEquals("COMPLETED", status);
        verify(jobExplorer, times(1)).getJobExecution(executionId);
    }
    
    @Test
    public void testGetJobStatus_JobNotFound() {
        // Arrange
        final Long nonExistentId = 99L;
        // Mock the behavior when the job is not found (returns null)
        when(jobExplorer.getJobExecution(nonExistentId)).thenReturn(null);

        // Act
        String status = ingestionJobService.getJobStatus(nonExistentId);

        // Assert
        assertEquals("JOB_NOT_FOUND", status);
        verify(jobExplorer, times(1)).getJobExecution(nonExistentId);
    }
    
    @Test
    public void testPauseScheduler_ReturnsTrueAsPlaceholder() {
        // This tests the simple placeholder implementation
        boolean result = ingestionJobService.pauseScheduler(TEST_BANK_ID);
        assertEquals(true, result);
    }
}
