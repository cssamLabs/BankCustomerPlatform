package com.hibersoft.ms.bankcustomer.simpledatamodeling;

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

import com.hibersoft.ms.bankcustomer.simpledatamodeling.config.BatchConfiguration;
import com.hibersoft.ms.bankcustomer.simpledatamodeling.model.RawSourceData;

@SpringBootTest
@ActiveProfiles("test") 
@AutoConfigureMockMvc
@SpringBatchTest
@Import(BatchConfiguration.class)
@Sql(scripts = "classpath:batch-schema-h2.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class SimpleDataModelingApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testStartJobEndpoint_TriggersSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/simple-ingestion/start/BANK_A"))
               .andExpect(status().isOk());
    }

    @Test
    void testStartAllEndpoint_TriggersSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/simple-ingestion/start-all"))
               .andExpect(status().isOk());
        // Additional assertions can be added to verify job launch logic if needed
    }
}
