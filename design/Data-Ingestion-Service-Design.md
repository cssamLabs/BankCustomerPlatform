# Data Ingestion Service Design

## Components

1. **Scheduler**: Manages the timing of data extraction (e.g., a nightly job). This can use Spring's built-in `@Scheduled` annotation or an external orchestrator like Apache Airflow.
2. **Job Orchestrator:** **The core of the service, implemented using** **Spring Batch\*\***. It defines the overall ETL job workflow, including individual steps for different banks or data types.\*\*
3. **Item Reader:** **Custom components (e.g.,** `JdbcCursorItemReader` **or** `JdbcPagingItemReader`) that read data efficiently from the source bank databases.
4. **Item Writer:** Components that write the raw extracted data into the staging area or data lake (e.g., writing to an S3 bucket or a local staging DB table).
5. **Job Repository:** A database (PostgreSQL) used by Spring Batch to store metadata about job executions, including status, start/end times, and failure points. This allows for reliable job restarts.
6. **Kafka Producer:** A client to send administrative events (notifications) to other microservices upon job completion or failure.

## Functions

- **Extraction:** Securely connects to multiple, heterogeneous bank databases to retrieve new or updated customer behavior data.
- **Validation:** Performs basic validation on the raw data (e.g., ensuring key fields are present) before staging.
- **Staging:** Stores the raw, immutable data in a cost-effective data lake or staging database.
- **Monitoring & Auditing:** Logs all job executions, processing statistics, and errors using the Job Repository.
- **Notification:** Alerts downstream services (the Data Modelling Service) that a specific bank's data batch is ready for processing

## Endpoints (REST API)

The service is primarily event-driven/scheduled, but administrative endpoints are crucial for operational control and monitoring:


| Endpoint                                    | Method | Description                                                                                           |
| ------------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------- |
| `/api/v1/ingestion/start/{bankId}`          | `POST` | Manually triggers the data extraction job for a specific bank ID (e.g., for ad-hoc re-runs).          |
| `/api/v1/ingestion/status/{jobId}`          | `GET`  | Retrieves the status of a running or completed ingestion job (e.g.,`COMPLETED`, `FAILED`, `STARTED`). |
| `/api/v1/ingestion/schedule/pause/{bankId}` | `POST` | Pauses the automatic scheduler for a specific bank's ingestion pipeline.                              |
| `/actuator/health`                          | `GET`  | Standard Spring Boot health check for Kubernetes liveness/readiness probes.                           |

## Pipelines & Data Flow

The data flows through a structured, multi-step pipeline within the microservice:

1. **Trigger:** **The scheduler (or manual API call) initiates a Spring Batch Job.**
2. **Read (Extract):** **The** `Item Reader` **executes a parameterized SQL query to extract data chunks from the source bank database.**
3. **Process (Minimal Transform):** **The data chunk is lightly processed (e.g., basic field mapping) and wrapped into a suitable Java object.**
4. **Write (Load - Staging):** **The** `Item Writer` **writes the chunk of raw data to the data lake/staging area. The data remains in its raw format.**
5. **Completion Event (Kafka):** **Upon successful job completion, the service publishes a notification to a Kafka topic (e.g.,** `topic.admin.batch-complete` **with** `bankId`, `batchId`, and `location-of-data` **in the payload). This is the key handoff to the Data Modelling Service.**

This design decouples the ingestion process from the transformation logic, allowing each microservice to scale and evolve independently.

## Specific Data Payloads (schemas)

### **Specific data payloads (schemas) that will be sent via Kafka between the Data Ingestion and Data Modelling services**

**Kafka Topic: `topic.admin.ingestion-events`**

This topic is used to signal status changes and data availability.

#### Payload 1: `IngestionJobStarted` Event

This message is sent when the Data Ingestion Service successfully starts an extraction job.


| Field Name  | Data Type     | Description                                | Example                                |
| ----------- | ------------- | ------------------------------------------ | -------------------------------------- |
| `eventId`   | UUID          | Unique identifier for this event message.  | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| `timestamp` | UTC Timestamp | When the job started.                      | `2025-11-26T18:30:00Z`                 |
| `eventType` | String        | Indicator of the event type.               | `"INGESTION_STARTED"`                  |
| `bankId`    | String        | The unique identifier for the source bank. | `"BANK_A_LTD"`                         |
| `jobId`     | String        | The internal Spring Batch Job Instance ID. | `"BankA_Ingest_Job_20251126"`          |

#### Payload 2: `IngestionJobComplete` Event

This message is sent when the extraction is finished and the raw data is staged. This is the primary trigger for the Data Modelling service.


| Field Name        | Data Type     | Description                                                         | Example                                            |
| ----------------- | ------------- | ------------------------------------------------------------------- | -------------------------------------------------- |
| `eventId`         | UUID          | Unique identifier for this event message.                           | `b8eec123-1c0c-4ff9-cc7e-7cc7cd450b22`             |
| `timestamp`       | UTC Timestamp | When the job completed successfully.                                | `2025-11-26T20:00:00Z`                             |
| `eventType`       | String        | Indicator of the event type.                                        | `"INGESTION_COMPLETE"`                             |
| `bankId`          | String        | The unique identifier for the source bank.                          | `"BANK_A_LTD"`                                     |
| `batchId`         | String        | A specific identifier for this data batch.                          | `"20251126-001"`                                   |
| `dataLocationURI` | String        | URI pointing to the raw data files in the staging area (Data Lake). | `"s3://bank-data-lake/raw/BANK_A_LTD/2025/11/26/"` |
| `recordCount`     | Integer       | The total number of records successfully ingested.                  | `450123`                                           |

#### Payload 3: `IngestionJobFailed` Event

This message is sent if the Spring Batch job fails.


| Field Name     | Data Type     | Description                                | Example                                                     |
| -------------- | ------------- | ------------------------------------------ | ----------------------------------------------------------- |
| `eventId`      | UUID          | Unique identifier for this event message.  | `c9fed345-2d1d-5gg0-dd8f-8dd8de561c33`                      |
| `timestamp`    | UTC Timestamp | When the failure occurred.                 | `2025-11-26T19:15:00Z`                                      |
| `eventType`    | String        | Indicator of the event type.               | `"INGESTION_FAILED"`                                        |
| `bankId`       | String        | The unique identifier for the source bank. | `"BANK_A_LTD"`                                              |
| `jobId`        | String        | The internal Spring Batch Job Instance ID. | `"BankA_Ingest_Job_20251126"`                               |
| `errorMessage` | String        | High-level error description.              | `"JDBC connection timeout during step 'readTransactions'."` |

#### How this works in practice:

1. The **Data Modelling Service** **will be a Spring Boot Kafka Consumer listening to** `topic.admin.ingestion-events`.
2. **When it receives an** `INGESTION_COMPLETE` **event, it reads the** `bankId` **and** `dataLocationURI` **from the payload.**
3. **It then uses Spring Batch to start its own ETL process, using the** `dataLocationURI` **as its input source.**

This asynchronous, event-driven contract via Kafka topics ensures robust decoupling: the Ingestion Service only cares about extracting data and sending a notification, while the Data Modelling Service is simply triggered by the availability of new data, regardless of _how_ it was extracted.

## Intergration Test Suite

the Ingestion Service REST API using Spring Boot's testing framework, specifically focusing on `@WebMvcTest` and `MockMvc` to test the web layer in isolation. This approach avoids starting the full Spring Batch environment but ensures the controller correctly receives, processes, and delegates HTTP requests.

We will use **Mockito** to mock the underlying service layer (`IngestionJobService`), allowing us to verify that the correct service methods are called and that the API responses are correct.


#### Ingestion Service REST API Test Suite (`IngestionAdminControllerTest.java`)


Create this test class in `src/test/java/com/hibersoft/ms/bankcustomer/ingestion/controller/IngestionAdminControllerTest.java`.** **

The test suite `IngestionAdminControllerTest.java` uses `@WebMvcTest` to focus testing on the controller layer and `@MockBean` to mock the `IngestionJobService`. `MockMvc` is used to simulate HTTP requests.** **

The tests cover several endpoints:** **

* **`testStartIngestionJobEndpoint()`****: Verifies that a POST request to** `/start/{bankId}` **with a bank ID returns an HTTP 200 status and a JSON response containing "STARTED" status and a** `jobExecutionId`. It mocks the `jobService.triggerJob()` **method to return a dummy** `JobExecution` **object.**
* **`testGetJobStatusEndpoint()`****: Tests that a GET request to** `/status/{jobId}` **with a job ID returns an HTTP 200 status and a JSON response with the job status. It mocks the** `jobService.getJobStatus()` **method to return "COMPLETED".**
* **`testPauseSchedulerEndpoint()`****: Checks that a POST request to** `/schedule/pause/{bankId}` **with a bank ID returns an HTTP 200 status and a specific string response. It mocks the** `jobService.pauseScheduler()` **method to return** `true`.** **
