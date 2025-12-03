# (Batch-Centric) Data Modelling MicroServices

## A. Architecture and Design

Architecture, Design, and Implementation of Batch-Centric Data Modelling Micro Services in Java stack. Using Springboot, Kafka, SpringBatch.

We are using domain of customer behavior data of multi banking.

In a multi-bank customer behavior data modelling scenario, the primary goal is to aggregate and harmonize data from diverse financial institutions to create a unified customer profile for analytics. This model would likely follow a dimensional modelling approach, using a data warehouse to store structured, query-optimized data. The key lies in creating a centralized data model that can integrate disparate datasets while preserving customer privacy.

[Data Modelling Micro Services Architecture](../design/Batch-Data-Modelling-Micro-Services-Architecture.md)

## B.  Project Setup

1. Project Setup with Spring Initializr

We will use Spring Initializr  from start.spring.io and configure as  [Spring Batch Processing](https://spring.io/guides/gs/batch-processing)

Configurations


| Field           | Value                                                                   |
| --------------- | ----------------------------------------------------------------------- |
| **Project**     | Maven Project or Gradle Project (Maven is a standard enterprise choice) |
| **Language**    | Java                                                                    |
| **Spring Boot** | 4.0.1 (SNAPSHOT)                                                        |
| **Group**       | `com.hibersoft.ms.bankcustomer.datamodeling`                            |
| **Artifact**    | `data-modeling-service`                                                 |
| **Packaging**   | Jar                                                                     |
| **Java**        | 21                                                                      |

#### Add Required Dependencies:

Click the "Add Dependencies" button and select the following:** **

* **`Spring Web`****: For creating the REST admin endpoints and general microservice functionality.**
* **`Spring for Apache Kafka`****: To consume the ingestion events from the** `topic.admin.ingestion-events` **topic.**
* **`Spring Batch`****: The core framework for orchestrating the ETL jobs (Cleansing, Harmonization, Loading).**
* **`PostgreSQL Driver`****: To connect to the MDM database (for enrichment) and potentially the data warehouse/job repository.**
* **`Spring Data JPA`****: Optional, but helpful for interacting with the MDM database using standard repositories.**
* **`Lombok`****: A utility library to reduce boilerplate code (constructors, getters/setters).**
* **`Spring Boot Actuator`****: For monitoring the application's health, metrics, and job status.**

Once configured, click the **Generate** button to download the boilerplate project as a ZIP file.

2. Initial Configuration

Configure connectivity in the `application.properties` (or `application.yml`) file.** **

properties

```
# Kafka Configuration
spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=data-modeling-consumer-group
spring.kafka.consumer.auto-offset-reset=earliest
# You might want to disable auto commit for reliable batch processing
spring.kafka.consumer.enable-auto-commit=false 

# Database Configuration (for Spring Batch Job Repository and MDM lookup)
spring.datasource.url=jdbc:postgresql://localhost:5432/datamodelingdb
spring.datasource.username=youruser
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update

# Enable Spring Batch features
spring.batch.initialize-schema=always
spring.batch.job.enabled=false # Jobs will be launched manually via the Kafka listener trigger

# Expose Actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

3. Key Classes to Implement

core logic based on the design:

* **`IngestionEventConsumer.java`****: A class annotated with** `@KafkaListener` **to receive the** `IngestionJobComplete` **events.**
* **`JobLaunchRequestService.java`****: A service that uses the Spring** `JobLauncher` **to start a specific Spring Batch job based on the event payload.**
* **`BatchConfiguration.java`****: A configuration class annotated with** `@EnableBatchProcessing` **to define the ETL jobs (Steps 1-3 from the previous design).**
* **`DataCleansingProcessor.java` / `MDMEnrichmentProcessor.java`****: Classes implementing** `ItemProcessor` **or** `Tasklet` **logic for the transformation steps.**

This setup provides the foundational environment required to implement batch-centric data modelling service.

## End-to-End Test Flow

### 1. Ingestion Service Test (`IngestionBatchConfigurationTest`)

In this test, we simulate the *Ingestion Service's* behavior:

* **It reads test data we manually inserted into an in-memory H2 database using** `JdbcTemplate`.
* **It writes this data to an** **`output.csv`** **file in a temporary directory (e.g.,** `./test_output/BANK_TEST_test_output.csv`).
* **The test verifies that this `output.csv` file is correctly created and filled.**

The listener then theoretically sends a Kafka message pointing to this file's location.

### 2. Data Modelling Service Test (`ModelingBatchConfigurationTest`)

This is where the second part happens. This test effectively simulates the *Modelling Service* picking up the data:

* **It manually inserts data directly into the** `STAGING_TRANSACTIONS` **table (bypassing the input CSV reader for simplicity in this specific test's setup).**
* **It runs the SQL ELT process (the Tasklet).**
* **It verifies that the final** `FACT_TRANSACTIONS` **table is correctly populated.**

Step 2: Open Terminal in Project Root

Navigate to the root directory of your `datamodeling-service` project in your terminal. This is where your `build.gradle` file is located.

Step 3: Run All Tests (Unit and Integration)

The simplest Gradle command runs all tests within the project:

bash

```
./gradlew clean test
```

This command does two things:

1. `clean`: Deletes previous build artifacts and test results (`build/` **folder).**
2. `test`: Compiles the source code, compiles the test code, and executes all test classes whose names end in `Test.java` **(e.g.,** `*ControllerTest.java`, `*ServiceTest.java`, `*ConfigurationTest.java`).

Step 4: Analyze the Results

Gradle will output the results to the console.

* **If successful:** **You will see a** `BUILD SUCCESSFUL` **message and a summary showing all tests passed.**
* **If failures occur:** **The console output will clearly indicate which class and test method failed, along with the error type (e.g.,** `ClassNotFoundException`, `AssertionError`).

Step 5: Fix Refactoring Errors

If you encounter errors related to missing classes or incorrect packages (like `com.bank.ingestion.service` is not found), go back into your IDE and use the **Refactor -> Move** tool again to place those classes under the correct `com.hibersoft.ms.bankcustomer.datamodeling` base package, allowing the IDE to automatically fix the import statements.

Optional: Run Specific Test Suites

If you fixed an error in a specific test suite and want to run only that suite to verify the fix quickly:

bash

```
# 
Run only tests related to the Modeling Admin Controller
./gradlew test --tests 'ModelingAdminControllerTest'

# Run only tests related to the Ingestion Job Service
./gradlew test --tests 'IngestionJobServiceTest'
```
