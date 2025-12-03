package com.hibersoft.ms.bankcustomer.datamodeling.config;

import com.hibersoft.ms.bankcustomer.datamodeling.model.RawBankTransaction;
import com.hibersoft.ms.bankcustomer.datamodeling.model.EnrichedBankTransaction;
import com.hibersoft.ms.bankcustomer.datamodeling.processor.CleansingItemProcessor;
import com.hibersoft.ms.bankcustomer.datamodeling.model.FactTransactionEntity;
import com.hibersoft.ms.bankcustomer.datamodeling.processor.EnrichmentItemProcessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.configuration.annotation.StepScope;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableBatchProcessing
public class ModelingBatchConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate; // Added for running custom SQL

    private static final Logger log = LoggerFactory.getLogger(ModelingBatchConfiguration.class);

    public ModelingBatchConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager, DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- JOB DEFINITION ---
    @Bean
    public Job bankCustomerBehaviorModelJob(Step cleanseAndLoadStagingStep, Step enrichAndLoadFactTableStep) {
        return new JobBuilder("bankCustomerBehaviorModelJob", jobRepository)
                .start(cleanseAndLoadStagingStep)
                .next(enrichAndLoadFactTableStep) // Second step is the SQL execution (ELT)
                .build();
    }

    // --- STEP 1: READ RAW FILE, CLEANSE, LOAD TO STAGING DB ---

    @Bean
    public Step cleanseAndLoadStagingStep(JdbcCursorItemReader<EnrichedBankTransaction> reader, 
                                          EnrichmentItemProcessor processor, 
                                          JdbcBatchItemWriter<FactTransactionEntity> factTransactionWriter,
                                          Tasklet enrichmentTasklet) {
       return new StepBuilder("cleanseAndLoadStagingStep", jobRepository)
                // The input type is EnrichedBankTransaction, output is FactTransactionEntity
                .<EnrichedBankTransaction, FactTransactionEntity>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(factTransactionWriter)
                .build();
    }

    // Step 1 Reader/Processor definitions remain the same as previous snippets...
    // @Bean
    // public FlatFileItemReader<RawBankTransaction> reader(@Value("#{jobParameters['inputUri']}") String inputUri) {
    //      // ... (implementation as before)
    //      return new FlatFileItemReaderBuilder<RawBankTransaction>()
    //             .name("rawBankTransactionReader")
    //             .resource(new FileSystemResource(inputUri))
    //             .delimited()
    //             .names(new String[]{"bankSpecificTransactionId", "bankSpecificAccountId", "transactionDate", "amount", "description", "locationCode"})
    //             .targetType(RawBankTransaction.class)
    //             .build();
    // }

    // --- ITEM READER (Reads enriched data from DB for modeling) ---
    @Bean
    @StepScope // Make it step scoped to handle dynamic SQL
    public JdbcCursorItemReader<EnrichedBankTransaction> reader(@Value("#{jobParameters['bankId']}") String bankId) {
        // This is the line causing the error
        // String sql = "SELECT * FROM enriched_transactions WHERE bank_id = '" + bankId + "' AND is_valid = TRUE"; 

        return new JdbcCursorItemReaderBuilder<EnrichedBankTransaction>()
                .dataSource(dataSource)
                .name("factDataReader")
                // Use a placeholder SQL that is safe during context load
                .sql("SELECT * FROM enriched_transactions WHERE 1=0") // Safe placeholder SQL
                .rowMapper(new BeanPropertyRowMapper<>(EnrichedBankTransaction.class))
                // Add .scope("step") if your Gradle version supports it
                .build();
    }
    
    @Bean
    public CleansingItemProcessor processor() {
        return new CleansingItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<FactTransactionEntity> factTransactionWriter() {
        return new JdbcBatchItemWriterBuilder<FactTransactionEntity>()
                .dataSource(dataSource)
                .sql("INSERT INTO FACT_TRANSACTIONS (bank_id, customer_id, transaction_time, amount_standard, description_standard, location_code, transaction_type, is_valid) VALUES (:bankId, :customerId, :transactionTime, :amountStandard, :descriptionStandard, :locationCode, :transactionType, :isValid)")
                .beanMapped() // Automatically maps the FactTransactionEntity properties to the SQL parameters
                .build();
    }

    // @Bean
    // public JdbcBatchItemWriter<EnrichedBankTransaction> writer() {
    //     // Writes the partially enriched data (with bankSpecificAccountId present) into a STAGING table
    //     return new JdbcBatchItemWriterBuilder<EnrichedBankTransaction>()
    //             .sql("INSERT INTO STAGING_TRANSACTIONS (bank_specific_account_id, amount_standard, transaction_time, description, is_valid) VALUES (:bankSpecificAccountId, :amountStandard, :transactionTime, :description, :isValid)")
    //             .dataSource(dataSource)
    //             .beanMapped()
    //             .build();
    // }

    // --- STEP 2: ENRICH AND LOAD FINAL FACT TABLE USING SQL (ELT Tasklet) ---
    // The complex logic moves from the Java processor to a SQL statement.

    @Bean
    public Step enrichAndLoadFactTableStep(Tasklet enrichmentTasklet) {
        return new StepBuilder("enrichAndLoadFactTableStep", jobRepository)
            .tasklet(enrichmentTasklet, transactionManager)
            .build();
    }

    @Bean
    // Use a standard property with a default value.
    public Tasklet enrichmentTasklet(@Value("${bank.id.default:BANK_A}") String bankId) {
        return (contribution, chunkContext) -> {
            // bankId is now available via configuration properties, either default or overridden at runtime

            log.info("Starting SQL ELT enrichment for bank {}...", bankId);
            
            // Use the bankId in your SQL statement securely (e.g., in the WHERE clause)
            String sql = "INSERT INTO FACT_TRANSACTIONS (...) SELECT ... WHERE BANK_ID = '" + bankId + "'";
            int rowsAffected = jdbcTemplate.update(sql);
            log.info("ELT Tasklet complete. Rows inserted into FACT_TRANSACTIONS: {}", rowsAffected);

            return RepeatStatus.FINISHED;
        };
    }
    
    // @Bean
    // public Tasklet enrichmentTasklet() {
    //     return (contribution, chunkContext) -> {
    //         // Retrieve the parameter safely at runtime
    //         String bankId = (String) chunkContext.getStepContext().getJobParameters().get("bankId");
            
    //         log.info("Starting SQL ELT enrichment for bank {}...", bankId);
            
    //         // Use the bankId in your SQL statement securely (e.g., in the WHERE clause)
    //         String sql = "INSERT INTO FACT_TRANSACTIONS (...) SELECT ... WHERE BANK_ID = '" + bankId + "'";
    //         int rowsAffected = jdbcTemplate.update(sql);
    //         log.info("ELT Tasklet complete. Rows inserted into FACT_TRANSACTIONS: {}", rowsAffected);

    //         return RepeatStatus.FINISHED;
    //     };
    // }
    // @Bean
    // @StepScope
    // public Tasklet enrichmentTasklet() {
    //     return (contribution, chunkContext) -> {
    //         log.info("Starting SQL ELT enrichment and loading into Fact table...");
            
    //         // This SQL handles the JOIN with the MDM_CUSTOMER_MAP table (aliased as mdm)
    //         // and inserts the final, fully enriched record into the FACT table.
    //         String sql = """
    //             INSERT INTO FACT_TRANSACTIONS (customer_id, bank_id, date_key, amount_standard, transaction_type, location_id)
    //             SELECT 
    //                 mdm.unified_customer_id,
    //                 s.bank_id,
    //                 DIM_DATE.date_key, -- Assumes a pre-existing date dimension table
    //                 s.amount_standard,
    //                 s.transaction_type,
    //                 DIM_LOCATION.location_id
    //             FROM 
    //                 STAGING_TRANSACTIONS s
    //             JOIN 
    //                 MDM_CUSTOMER_MAP mdm ON s.bank_specific_account_id = mdm.bank_specific_account_id
    //             JOIN
    //                 DIM_LOCATION ON s.location_code = DIM_LOCATION.location_code
    //             JOIN 
    //                 DIM_DATE ON DATE(s.transaction_time) = DIM_DATE.full_date
    //             WHERE
    //                 s.is_valid = true;
    //         """;
            
    //         int rowsAffected = jdbcTemplate.update(sql);
    //         log.info("ELT Tasklet complete. Rows inserted into FACT_TRANSACTIONS: {}", rowsAffected);

    //         // Optional: Add logic here to clear the staging table after a successful load

    //         return RepeatStatus.FINISHED;
    //     };
    // }
}
