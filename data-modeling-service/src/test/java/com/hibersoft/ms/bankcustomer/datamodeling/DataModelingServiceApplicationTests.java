package com.hibersoft.ms.bankcustomer.datamodeling;

// ... (imports) ...
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import javax.sql.DataSource;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hibersoft.ms.bankcustomer.datamodeling.config.BatchConfiguration;
import com.hibersoft.ms.bankcustomer.datamodeling.model.RawSourceData;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(BatchConfiguration.class)
@SpringBatchTest 
@Sql(scripts = "classpath:batch-schema-h2.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class DataModelingServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Test
    void testStartJobEndpoint_TriggersSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/ingestion/start/BANK_A"))
               .andExpect(status().isOk());
    }

    @Test
    void testStartAllEndpoint_TriggersSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/ingestion/start-all"))
               .andExpect(status().isOk());
    }

    @Test
    void testDynamicReaderSQL_withBankId() throws Exception {
        // Arrange
        String bankId = "BANK_B";
        JdbcCursorItemReader<RawSourceData> reader = new BatchConfiguration().sourceDataReader(dataSource, bankId);

        // Act & Assert
        Assertions.assertEquals("SELECT bank_specific_account_id, transaction_date, amount, description, location_code FROM bank_b_transactions", reader.getSql());
    }
}
