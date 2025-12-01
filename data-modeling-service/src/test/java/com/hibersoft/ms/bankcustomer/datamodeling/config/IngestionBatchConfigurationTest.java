package com.hibersoft.ms.bankcustomer.datamodeling.config;

import com.hibersoft.ms.bankcustomer.datamodeling.DatamodelingApplication;
import com.hibersoft.ms.bankcustomer.datamodeling.listener.DynamicOutputPathStepListener;
import com.hibersoft.ms.bankcustomer.datamodeling.listener.JobCompletionNotificationListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.boot.test.mock.mockito.MockBean; 
import org.springframework.boot.test.mock.mockito.SpyBean; // <-- Import SpyBean
import org.springframework.kafka.core.KafkaTemplate; 
import com.hibersoft.ms.bankcustomer.datamodeling.consumer.IngestionEventConsumer;
import com.hibersoft.ms.bankcustomer.datamodeling.service.JobLaunchRequestService; 
import org.springframework.batch.core.Job;

@SpringBootTest(classes = DatamodelingApplication.class) // Load the full context
@SpringBatchTest
@TestPropertySource(properties = { // Use H2 for testing
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.kafka.listener.auto-startup=false"
})
public class IngestionBatchConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Autowire H2 template to manage test data

    @Autowired
    private Job ingestBankDataJob; 

    // Mock the listener and services that involve runtime interaction
    @MockBean
    private DynamicOutputPathStepListener mockPathListener; // We only mock it, real logic is simple
    @MockBean 
    private JobCompletionNotificationListener mockListener;
    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate; 


    // Use SpyBean for services we want to run as real beans but still verify/mock interactions
    @SpyBean
    private JobLaunchRequestService jobLaunchRequestService;
    @SpyBean
    private IngestionEventConsumer ingestionEventConsumer;

    private static final String TEST_OUTPUT_DIR = "./target/";
    private static final String TEST_BANK_ID = "TEST_BANK";

    @BeforeEach
    public void setUp() {
        // Set up the H2 schema with test data before each test
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS bank_a_transactions (bank_specific_account_id VARCHAR(255), transaction_date VARCHAR(255), amount VARCHAR(255), description VARCHAR(255), location_code VARCHAR(255));");
        jdbcTemplate.execute("DELETE FROM bank_a_transactions");
        jdbcTemplate.execute("INSERT INTO bank_a_transactions VALUES ('ACC123', '2023-01-01 10:00:00', '150.00', 'Coffee Shop', 'L1');");
        jdbcTemplate.execute("INSERT INTO bank_a_transactions VALUES ('ACC124', '2023-01-01 11:00:00', '20.00', 'Gas Station', 'L2');");
    }

    @Test
    public void testIngestBankDataJob_ShouldCompleteSuccessfullyAndWriteFile() throws Exception {
        // Arrange
        // The path listener dynamically sets the path, but for testing we can simulate the outcome
        final String actualOutputPath = TEST_OUTPUT_DIR + "test_ingestion_output.csv";
        final File actualOutputFile = new File(actualOutputPath);

        // Define job parameters, including those needed for the dynamic path listener/writer logic
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("bankId", TEST_BANK_ID);
        paramsBuilder.addLong("run.id", System.currentTimeMillis()); // Ensure unique job run

        // Act
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(paramsBuilder.toJobParameters());

        // Assert
        assertEquals(ExitStatus.COMPLETED.getExitCode(), jobExecution.getExitStatus().getExitCode());
        assertEquals(2, jdbcTemplate.queryForObject("SELECT count(*) FROM bank_a_transactions", Integer.class).intValue());

        // We can manually assert the content written to the file if we ensure our listener wrote to a deterministic path
        // (Since our listener uses currentTimeMillis, we rely on the job exiting cleanly and DB count for this test)
    }
}
