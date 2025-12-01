# (Batch-Centric) Data Modelling MicroServices

## A. Architecture and Design

Architecture, Design, and Implementation of Batch-Centric Data Modelling Micro Services in Java stack. Using Springboot, Kafka, SpringBatch.

We are using domain of customer behavior data of multi banking.

In a multi-bank customer behavior data modelling scenario, the primary goal is to aggregate and harmonize data from diverse financial institutions to create a unified customer profile for analytics. This model would likely follow a dimensional modelling approach, using a data warehouse to store structured, query-optimized data. The key lies in creating a centralized data model that can integrate disparate datasets while preserving customer privacy.

[Data Modelling Micro Services Architecture](/design/Batch-Data-Modelling-Micro-Services-Architecture.md)

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

## Settingup for Local Run

### Prerequisites

1. **Install Docker and Docker Compose** **on your local machine.**
2. **Ensure you have a `Dockerfile`** **in your project root (we created one earlier).**
3. **Update `application.properties`** **to use the Docker Compose service name for the database connection.**

### 1. Update `application.properties`

We must update the application properties to connect to the PostgreSQL container using its *service name* (`postgres-db`) which acts as its hostname within the Docker network.** **

properties

```
# Docker Compose DB Configuration (use the service name as the host)
spring.datasource.url=jdbc:postgresql://postgres-db:5432/datamodelingdb
spring.datasource.username=youruser
spring.datasource.password=yourpassword

# Kafka Configuration (Assuming Kafka is also run via Docker Compose)
spring.kafka.consumer.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=data-modeling-consumer-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false 

# Spring Batch Configuration
spring.batch.initialize-schema=always
spring.batch.job.enabled=false 

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

2. Create `docker-compose.yml`

Create this file in the root directory of your `datamodeling-service` project. This file defines the services, networks, and volumes for your local environment.** **

yaml

```
version: '3.8'

services:
  datamodeling-app:
    # Use the image name we defined in the Dockerfile build step earlier
    image: hibersoft/data-modeling-service:latest 
    container_name: datamodeling-app
    # Map local port 8080 to the container's 8080
    ports:
      - "8080:8080"
    environment:
      # Pass the host IP for the Kafka producer (if the app acts as a producer too)
      SPRING_KAFKA_PRODUCER_BOOTSTRAP_SERVERS: kafka:9092
    depends_on:
      - postgres-db
      - kafka
    # Mount a local directory to a container path for simulating staged data input/output
    volumes:
      - ./local_data_staging:/opt/data/staging 
    networks:
      - bank-network

  postgres-db:
    image: postgres:14-alpine
    container_name: postgres-db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: datamodelingdb
      POSTGRES_USER: youruser
      POSTGRES_PASSWORD: yourpassword
    # Use a volume for data persistence even if the container is removed
    volumes:
      - postgres-data-volume:/var/lib/postgresql/data
    networks:
      - bank-network
  
  # Placeholder for Kafka and Zookeeper (you would need full configs for these)
  kafka:
    image: confluentinc/cp-kafka:7.0.0
    networks:
      - bank-network
    # ... full configuration required ...

  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.0
    networks:
      - bank-network
    # ... full configuration required ...


# Define volumes and networks
volumes:
  postgres-data-volume:
  local_data_staging:

networks:
  bank-network:
    driver: bridge
```

3. Running the Environment

   1. **Build your application's Docker image locally:**
      bash

      ```
      docker build -t hibersoft/data-modeling-service:latest .
      ```
   2. **Create a local staging directory:**
      bash

      ```
      mkdir local_data_staging
      # Place a dummy input.csv file here if you want to test manual runs
      ```
   3. **Start the services using Docker Compose:**
      bash

      ```
      docker-compose up -d
      ```

Your `datamodeling-app` container will now connect to the `postgres-db` container. When you trigger a job (via the Kafka consumer or a manual REST call), it will read from and write to the mounted `./local_data_staging` folder on your host machine, simulating the cloud storage requirement.

## Deploy with Docker Compose

We have a standard `docker-compose.yml` file that defines services for `postgres-db`, `zookeeper`, `kafka`, and the new `datamodeling-service`.

### 1. Build the Docker Image

First, build the application's Docker image using the Spring Boot Gradle plugin:

bash

```
docker login

./gradlew bootBuildImage
```

This will create an image named something like `com.hibersoft.ms.bankcustomer/datamodeling:0.0.1-SNAPSHOT` (based on your `group` and `version` in `build.gradle`).

2. Deploy the Services

Navigate to the directory containing your `docker-compose.yml` file and start all services:

bash

```
docker compose up -d
```

Wait 30-60 seconds for all containers to start up, including the PostgreSQL database and Kafka.

3. Verify Container Status

Check that all containers are running:

bash

```
docker compose ps
```

You should see healthy statuses for `postgres-db`, `zookeeper`, `kafka`, and `datamodeling-service`. The data modelling service will connect to PostgreSQL and create all necessary tables (if `ddl-auto` is set to `update` or `create`).

#### Docker Images Management

Restart (and implicitly update) only the `datamodeling-app` service:

This command starts the `datamodeling-app` using the newly built image.

bash

```
docker-compose up -d datamodeling-app
```

*The other services (`postgres-db`, `kafka`, `zookeeper`) are already running, so Docker ignores them.*

#### Database Preperation

**Run the SQL CREATE TABLE command** **to create the** `bank_a_transactions` **table:**Once you are in the `psql` **shell (the prompt will be** `datamodelingdb=#` **or similar), paste the following SQL command and press Enter:**

sql

```
CREATE TABLE IF NOT EXISTS bank_a_transactions (
    bank_specific_account_id VARCHAR(255) PRIMARY KEY,
    transaction_date VARCHAR(255),
    amount VARCHAR(255),
    description VARCHAR(255),
    location_code VARCHAR(255)
);
```

*You should see an output like `CREATE TABLE`.*

**

sql

```
INSERT INTO bank_a_transactions (bank_specific_account_id, transaction_date, amount, description, location_code) VALUES 
('ACC123', '2023-01-01 10:00:00', '150.00', 'Coffee Shop', 'L1'),
('ACC124', '2023-01-01 11:00:00', '20.00', 'Gas Station', 'L2');
```

Use code with caution.

**You should see an output like** `INSERT 0 2`.

**Verify the data was inserted** **(optional):**
sql

```
SELECT * FROM bank_a_transactions;
```

Use code with caution.

**Run the PostgreSQL Schema Script.**Spring Batch provides standard SQL scripts for various databases. You need the PostgreSQL version. Since you likely don't have the file locally, you can paste the required SQL commands directly into the `psql` **shell:**Paste the following SQL commands into your `psql` **shell and press Enter. This creates all the required** `BATCH_...` **tables:**

sql

```


CREATE TABLE BATCH_STEP_EXECUTION  (
    STEP_EXECUTION_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
    VERSION BIGINT ,
    STEP_NAME VARCHAR(100) NOT NULL ,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    START_TIME TIMESTAMP ,
    END_TIME TIMESTAMP DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    COMMIT_COUNT BIGINT ,
    READ_COUNT BIGINT ,
    FILTER_COUNT BIGINT ,
    WRITE_COUNT BIGINT ,
    READ_SKIP_COUNT BIGINT ,
    WRITE_SKIP_COUNT BIGINT ,
    PROCESS_SKIP_COUNT BIGINT ,
    ROLLBACK_COUNT BIGINT ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    LAST_UPDATED TIMESTAMP,
    -- Ensure this column name matches exactly what the Java code selected previously
    CREATE_TIME TIMESTAMP -- This must exist and be named exactly this
);

-- Standard Spring Batch Schema for PostgreSQL (V5+)
CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806;
CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806;
CREATE SEQUENCE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806;

CREATE TABLE BATCH_JOB_INSTANCE  (
    JOB_INSTANCE_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
    VERSION BIGINT ,
    JOB_NAME  VARCHAR(100) ,
    JOB_KEY VARCHAR(32)
);

CREATE TABLE BATCH_JOB_EXECUTION  (
    JOB_EXECUTION_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
    VERSION BIGINT  ,
    JOB_INSTANCE_ID BIGINT NOT NULL,
    START_TIME TIMESTAMP DEFAULT NULL ,
    END_TIME TIMESTAMP DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    CREATE_TIME TIMESTAMP ,
    LAST_UPDATED TIMESTAMP ,
    JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL
);

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
    JOB_EXECUTION_ID BIGINT NOT NULL ,
    PARAMETER_NAME VARCHAR(100) NOT NULL ,
    PARAMETER_TYPE VARCHAR(100) NOT NULL ,
    PARAMETER_VALUE VARCHAR(2500) ,
    IDENTIFYING CHAR(1) NOT NULL
);

CREATE TABLE BATCH_STEP_EXECUTION  (
    STEP_EXECUTION_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
    VERSION BIGINT ,
    STEP_NAME VARCHAR(100) NOT NULL ,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    START_TIME TIMESTAMP ,
    END_TIME TIMESTAMP DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    COMMIT_COUNT BIGINT ,
    READ_COUNT BIGINT ,
    FILTER_COUNT BIGINT ,
    WRITE_COUNT BIGINT ,
    READ_SKIP_COUNT BIGINT ,
    WRITE_SKIP_COUNT BIGINT ,
    PROCESS_SKIP_COUNT BIGINT ,
    ROLLBACK_COUNT BIGINT ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    LAST_UPDATED TIMESTAMP 
);

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT 
);

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT 
);

-- Constraints and Indexes
ALTER TABLE BATCH_JOB_INSTANCE ADD CONSTRAINT JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY);
ALTER TABLE BATCH_JOB_EXECUTION ADD CONSTRAINT JOB_EXEC_INST_FK FOREIGN KEY (JOB_INSTANCE_ID) REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID);
ALTER TABLE BATCH_JOB_EXECUTION_PARAMS ADD CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID);
ALTER TABLE BATCH_STEP_EXECUTION ADD CONSTRAINT STEP_EXEC_JOB_EXEC_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID);
ALTER TABLE BATCH_STEP_EXECUTION_CONTEXT ADD CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID) REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID);
ALTER TABLE BATCH_JOB_EXECUTION_CONTEXT ADD CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID);

```

*You should see several `CREATE TABLE` and `ALTER TABLE` outputs.*

#### 2.  Test with Postman

The service is running on `http://localhost:8080` (or whatever port you configured). We will use Postman (or `curl`) to interact with the admin endpoints.

1. Ingest Initial Data (Pre-requisite)

The batch jobs need data to process. You need to simulate inserting some raw data into the source PostgreSQL table (`bank_a_transactions`) in the Docker container first.

You can use a PostgreSQL client or `docker exec` to manually insert some rows into the `bank_a_transactions` table.

2. Manually Trigger the Ingestion Job (Endpoint 1)

Use a `POST` request to the administration API to start the data ingestion job manually.

* **URL:** `http://localhost:8080/api/v1/ingestion/start/BANK_A`
* **Method:** `POST`

This should return a status `{"status": "STARTED", "jobExecutionId": "..."}`. The job runs, reads the raw data, stages it as a CSV file in the `/opt/data/staging` directory inside the Docker container, and sends a Kafka event.

3. Check Job Status (Endpoint 2)

Use a `GET` request to check the status of the job you just started (use the `jobExecutionId` returned in the previous step, e.g., 1).

* **URL:** `http://localhost:8080/api/v1/ingestion/status/1`
* **Method:** `GET`

This should return a status like `{"jobExecutionId": "1", "status": "COMPLETED"}` once finished.

4. Trigger the Modeling Job (Endpoint 3)

The previous job produced a CSV file and a Kafka event. To test the modelling job (which processes that CSV data), you typically rely on the Kafka consumer.

However, we can manually trigger the *modeling* job using its specific API endpoint. You need the full path of the file generated in Step 2 (check the Docker logs for the ingestion service to find the exact path, e.g., `/opt/data/staging/ingestion_BANK_A_1764540631933.csv`).

* **URL:** `http://localhost:8080/api/v1/modeling/trigger-manual-job?bankId=BANK_A&inputUri=file:///opt/data/staging/ingestion_BANK_A_TIMESTAMP.csv` **(Replace TIMESTAMP)**
* **Method:** `POST`

This job runs, enriches the data, and writes the final rows to the `FACT_TRANSACTIONS` table in the PostgreSQL database.
