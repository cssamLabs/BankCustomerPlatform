package com.hibersoft.ms.bankcustomer.datamodeling.config;

// ... (imports) ...
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.orm.jpa.JpaTransactionManager; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import com.hibersoft.ms.bankcustomer.datamodeling.model.FactTransactionEntity;
import com.hibersoft.ms.bankcustomer.datamodeling.model.RawSourceData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    // Reader: Reads raw data from the dynamic bank table
    @Bean
    @StepScope
    public JdbcCursorItemReader<RawSourceData> sourceDataReader(
        DataSource dataSource,
        @Value("#{jobParameters['bankId']}") String bankId
    ) {
        log.info("Configuring source data reader dynamically for bankId: {}", bankId);
        String tableName = bankId.toLowerCase() + "_transactions";
        String sql = "SELECT bank_specific_account_id, transaction_date, amount, description, location_code FROM " + tableName;
        
        log.info("Reader SQL: {}", sql);

        return new JdbcCursorItemReaderBuilder<RawSourceData>()
                .dataSource(dataSource)
                .name("bankDataReader-" + bankId) // Unique name
                .sql(sql)
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

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
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
