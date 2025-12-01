# Data Modelling Service Design (Batch Processing)

With the communication contract established, we can now detail the design of the **Data Modelling Service** components that will consume these events and perform the batch ETL process using **Spring Batch**, **Spring Boot**, and **SQL** for the bank customer behavior use case.

The Data Modelling Service is a Spring Boot application hosting a suite of robust Spring Batch jobs. It consumes the `INGESTION_COMPLETE` event, identifies the data location, and orchestrates the transformation and loading into the analytical data warehouse.

## Components

1. **Kafka Listener:** **A** `@KafkaListener` **component that subscribes to** `topic.admin.ingestion-events` **and handles the** `IngestionJobComplete` **payload.**
2. **Job Launcher/Orchestrator:** **A class that translates the incoming Kafka message payload into the parameters required to launch a specific Spring Batch job instance (e.g., passing the** `bankId` **and** `dataLocationURI` **as job parameters).**
3. **ETL Jobs (Spring Batch):** **A set of defined jobs for each stage of modeling:**
   - `cleanseAndHarmonizeJob`
   - `enrichWithMDMJob`
   - `loadDimensionalModelJob`
4. **Item Readers/Writers/Processors:** **Specific Spring Batch components (readers for S3/staging area, processors for business logic/MDM lookups, writers for the data warehouse).**
5. **Data Warehouse Connector:** **JDBC connectivity to the target data warehouse (e.g., Snowflake, BigQuery).**

## Functions

- **Event Consumption:** **Listens for ingestion completion events.**
- **Job Parameterization:** **Dynamically configures ETL jobs based on which bank's data is ready.**
- **Data Cleansing:** **Implements business rules to clean bank data (e.g., standardizing transaction codes, handling null values).**
- **Harmonization & Enrichment:** **Joins raw data with Master Data (from the MDM service's database) to enrich transactions with customer/product context.**
- **Dimensional Modelling:** **Transforms normalized data into a star schema optimized for BI and analytics.**
- **Auditing:** **Reports ETL metrics (rows processed, errors, duration) back into the internal monitoring system.**

## Endpoints (REST API)

Primarily administrative for monitoring the ETL processes it manages:


| Endpoint                              | Method | Description                                                              |
| ------------------------------------- | ------ | ------------------------------------------------------------------------ |
| `/admin/model/status/{jobId}`         | `GET`  | Retrieves the status of an ongoing data modelling ETL job.               |
| `/admin/model/trigger-reconciliation` | `POST` | Manually triggers a data reconciliation job for previous days if needed. |
| `/actuator/metrics`                   | `GET`  | Provides metrics on ETL execution times, throughput, and error rates.    |

## Pipelines (Spring Batch Job Example)

A single Spring Batch job might look like this:

**Job: `BankCustomerBehaviorModelJob`**

1. **Step 1: `readStageAndCleanse`**
   - **Reader:** **Reads raw data from the** `dataLocationURI` **provided in the Kafka payload.**
   - **Processor:** **Validates data types, applies cleansing rules.**
   - **Writer:** **Writes cleaned data to a temporary staging table in the data warehouse.**
2. **Step 2: `enrichWithMDM`**
   - **Reader:** **Reads from the temporary cleaned data table.**
   - **Processor:** **Calls the MDM service API or joins with the MDM database tables to append** `Customer_ID`, `Location_ID`, etc.
   - **Writer:** **Writes the fully enriched dataset to a new temporary table.**
3. **Step 3: `loadDimensionalModel`**
   - **Tasklet (SQL based):** **Executes a large SQL** `INSERT INTO ... SELECT ...` **statement that transforms the enriched data into the final** `Fact_Transactions` **table, adhering to the star schema structure. This uses ELT principles (Transform within the data warehouse engine).**
4. **Completion Notification:** **Sends a final Kafka message (**`topic.admin.model-complete`) signaling that the data warehouse is up-to-date and ready for the Analytics Microservices to consume.

## Services

`JobLaunchRequestService`

`public void launchModelingJob(IngestionEventPayload payload)`

The Execution Flow

1. External Trigger: Data Ingestion Service Completes
   The process begins when the separate ingestion-service project successfully finishes extracting data from a source bank database and stages the data (e.g., puts a file into an S3 bucket).
2. Kafka Event Published
   The ingestion-service then publishes a message to the topic.admin.ingestion-events Kafka topic with the INGESTION_COMPLETE payload we defined:

   ```
   json { "eventId": "...", "eventType": "INGESTION_COMPLETE", "bankId": "BANK_A_LTD", "dataLocationURI": "s3://bank-data-lake/raw/BANK_A_LTD/2025/11/26/data.csv", "batchId": "20251126-001", ... }
   ```
3. Data Modelling Service (DMS) Listens
   The datamodeling-service is deployed and running as a Spring Boot application with the @EnableKafka annotation active. The IngestionEventConsumer class is constantly listening to that topic.
4. The @KafkaListener Invokes the Consumer
   Spring for Apache Kafka automatically deserializes the incoming JSON message into our Java object (IngestionEventPayload) and invokes the annotated method:

   ```
   java // Inside IngestionEventConsumer.java @KafkaListener(topics = "topic.admin.ingestion-events", groupId = "data-modeling-consumer-group") public void listen(IngestionEventPayload payload) { // This method is triggered by Spring Kafka if ("INGESTION_COMPLETE".equals(payload.getEventType())) { // Calls the service layer method jobLaunchRequestService.launchModelingJob(payload); } // ... }
   ```
5. The Consumer Invokes the Service Method
   The IngestionEventConsumer acts as a thin wrapper, immediately delegating the actual execution logic to the injected JobLaunchRequestService bean by calling its public method:
   java

   ```
   // Inside JobLaunchRequestService.java
   public void launchModelingJob(IngestionEventPayload payload) throws Exception {
   // This is where the execution lands// ... builds job parameters using the 'payload' data ...// The JobLauncher is the Spring Batch orchestration component
   jobLauncher.run(bankCustomerBehaviorModelJob, jobParameters);
   }
   ```
6. The JobLauncher Starts the Batch Job
   The `jobLauncher.run()` method is the mechanism provided by Spring Batch to execute the defined `bankCustomerBehaviorModelJob` bean (which resides in your `BatchConfiguration` class) using the specific parameters extracted from the Kafka payload.
   The method launchModelingJob is executed whenever a new "ingestion complete" event arrives on the Kafka topic.
