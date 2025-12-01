package com.hibersoft.ms.bankcustomer.datamodeling.config;


import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import com.hibersoft.ms.bankcustomer.datamodeling.model.RawSourceData;
import com.hibersoft.ms.bankcustomer.datamodeling.listener.DynamicOutputPathStepListener;




@Configuration
@EnableBatchProcessing
public class IngestionBatchConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    public IngestionBatchConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
    }

    @Bean
    public Job ingestBankDataJob(Step ingestDataStep) {
        return new JobBuilder("ingestBankDataJob", jobRepository)
                .start(ingestDataStep)
                .build();
    }

    @Bean
    public Step ingestDataStep(JdbcCursorItemReader<RawSourceData> reader, FlatFileItemWriter<RawSourceData> writer, DynamicOutputPathStepListener pathListener) {
        return new StepBuilder("ingestDataStep", jobRepository)
                .<RawSourceData, RawSourceData>chunk(1000, transactionManager)
                .reader(reader)
                .writer(writer)
                .listener(pathListener)
                .build();
    }

    // --- ITEM READER (Reads from source DB table) ---
    @Bean
    public JdbcCursorItemReader<RawSourceData> sourceDataReader() {
        return new JdbcCursorItemReaderBuilder<RawSourceData>()
                .dataSource(dataSource)
                .name("sourceDataReader")
                .sql("SELECT bank_specific_account_id, transaction_date, amount, description, location_code FROM bank_a_transactions")
                .rowMapper(new BeanPropertyRowMapper<>(RawSourceData.class))
                .build();
    }

    // --- ITEM WRITER (Writes raw data to a flat file staging area) ---
    // Use the original SpEL expression, which is correct for runtime execution
    // @Value("#{jobParameters['bankId']}") String bankId
    @Bean
    @StepScope // Mark the bean as Step Scope
    public FlatFileItemWriter<RawSourceData> stagingWriter() {
        // String outputPath = "/opt/data/staging/ingestion_" + bankId + "_" +
        // System.currentTimeMillis() + ".csv";
        // Use a static, non-null placeholder path during bean creation
        // String outputPath = "file:./target/temp_placeholder.csv";
        String outputPath = "/opt/data/staging/ingestion_" + System.currentTimeMillis() + ".csv";

        return new FlatFileItemWriterBuilder<RawSourceData>()
                .name("stagingFileWriter")
                // Add the .scope("step") builder method
                .resource(new FileSystemResource(outputPath))
                .delimited()
                .names(new String[] { "bankSpecificAccountId", "transactionDate", "amount", "description",
                        "locationCode" })
                .build();
    }
}