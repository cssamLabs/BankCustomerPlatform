package com.hibersoft.ms.bankcustomer.datamodeling;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DatamodelingApplication.class)
@TestPropertySource(properties = { // <-- Add this annotation
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driverClassName=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		// --- ADD THIS LINE TO PROVIDE A DEFAULT JOB PARAMETER VALUE FOR TESTS ---
		"jobParameters.bankId=test_default_bank"
// ----------------------------------------------------------------------
})
class DatamodelingApplicationTests {

	@Test
	void contextLoads() {
	}

}
