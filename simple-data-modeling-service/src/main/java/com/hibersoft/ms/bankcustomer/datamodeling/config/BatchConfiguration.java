package com.hibersoft.ms.bankcustomer.datamodeling.config;

import com.hibersoft.ms.bankcustomer.datamodeling.model.FactTransactionEntity;
import com.hibersoft.ms.bankcustomer.datamodeling.model.RawSourceData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    // Reader: Reads raw data from bank_a_transactions
    @Bean
    public JdbcCursorItemReader<RawSourceData> sourceDataReader(DataSource dataSource) {
        log.info("Configuring source data reader with SQL: SELECT ... FROM bank_a_transactions");
        return new JdbcCursorItemReaderBuilder<RawSourceData>()
                .dataSource(dataSource)
                .name("bankADatabaseReader")
                .sql("SELECT bank_specific_account_id, transaction_date, amount, description, location_code FROM bank_a_transactions")
                .rowMapper(new BeanPropertyRowMapper<>(RawSourceData.class))
                .build();
    }

    // Processor: Simple pass-through with maximum logging
    @Bean
    public ItemProcessor<RawSourceData, FactTransactionEntity> processor() {
        return rawData -> {
            log.info("Processing RawSourceData: Account ID={}, Amount={}", rawData.getBankSpecificAccountId(), rawData.getAmount());
            FactTransactionEntity fact = new FactTransactionEntity();
            fact.setCustomerId("U_" + rawData.getBankSpecificAccountId()); // Simplified MDM
            fact.setDescriptionStandard(rawData.getDescription());
            fact.setLocationCode(rawData.getLocationCode());
            log.debug("Mapped to Fact Entity for Customer: {}", fact.getCustomerId());
            return fact;
        };
    }

    // Writer: Writes facts to FACT_TRANSACTIONS table (logging is handled by Spring Batch implicitly)
    @Bean
    public JpaItemWriter<FactTransactionEntity> writer(EntityManagerFactory entityManagerFactory) {
        log.info("Configuring JpaItemWriter for FACT_TRANSACTIONS");
        return new JpaItemWriterBuilder<FactTransactionEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    // Step: Defines the chunk size and flow
    @Bean
    public Step ingestionStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                              JdbcCursorItemReader<RawSourceData> reader, JpaItemWriter<FactTransactionEntity> writer,
                              ItemProcessor<RawSourceData, FactTransactionEntity> processor) {
        log.info("Configuring ingestionStep with chunk size 10");
        return new StepBuilder("ingestionStep", jobRepository)
                .<RawSourceData, FactTransactionEntity>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // Job: Orchestrates the step
    @Bean
    public Job ingestBankDataJob(JobRepository jobRepository, Step ingestionStep) {
        log.info("Building ingestBankDataJob");
        return new JobBuilder("ingestBankDataJob", jobRepository)
                .start(ingestionStep)
                .build();
    }
}

