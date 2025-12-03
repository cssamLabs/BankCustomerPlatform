# Batch-Centric Data Modelling MicroServices

## Services

### 1. Data Ingestion Service (DB Ingestion using Spring Boot)

This service is responsible for extracting data from the source databases in scheduled batches.

- **Technology:** Spring Boot, Spring Batch, JDBC Connectors, SQL.
- **Functionality:**

  - Scheduled Extraction: Periodically (e.g., nightly, hourly) connects to source bank databases.
  - Data Staging: Extracts data using optimized SQL queries, applies basic schema mapping, and loads the raw data into a data lake or a staging relational database.
  - Metadata Publication: Publishes an event to a Kafka topic (topic.admin.batch-started) once a batch extraction is complete and ready for processing.
  - Endpoints: Administrative endpoints to manually trigger an extraction job if needed.

### 2. Master Data Management (MDM) Service (Spring Boot)

Manage static reference data and a unified customer ID system.

- **Technology**: Spring Boot, PostgreSQL, REST APIs.
- **Functionality:** Maintains the Customer and Bank dimension tables used by the ETL process for enrichment. It must ensure the unified Customer_ID is mapped correctly to disparate bank IDs.

### 3. Data Cleaning, Harmonization, and Modelling Service (Spring Batch ETL Jobs)

This single, integrated service orchestrates the entire ETL pipeline using Spring Batch's job processing framework.

- **Technology**: Spring Boot, **Spring Batch**, SQL, Analytical Data Warehouse (e.g., Snowflake, BigQuery).
- **Functionality:**

  - Orchestration: Listens for "batch-ready" events from the Ingestion service and triggers the sequence of batch jobs.
  - Cleaning & Harmonization (Transformation): Executes complex SQL transformations (often ELT style: loading raw, transforming in the warehouse) to clean data, standardize formats (e.g., unit conversions, date formats), and join raw data with MDM reference data.
  - Modelling (Loading): Structures the harmonized data into the final dimensional model (star schema) within the data warehouse. It manages incremental loads, ensuring only new or updated records are processed efficiently.
- **Endpoints:** Primarily administrative for triggering, monitoring, and restarting failed ETL jobs.

## Summary of the Batch-Centric Data Flow:

1. **Schedule:** A scheduler triggers the **DB Ingestion Service** at 1 AM.
2. **Extract**: Ingestion service pulls data from Bank A and Bank B databases into a staging area.
3. **Notification**: Ingestion service sends a `batch-complete` message via Kafka.
4. **Process**: The **Data Modelling Service** consumes the message and starts a **Spring Batch ETL** job.
5. **Transform & Load**: Spring Batch executes SQL queries, cleanses, harmonizes using MDM lookups, and loads the data into the analytical Data Warehouse facts and dimensions.
6. **Ready**: Data is ready for the Analytics Services to query for daily reporting and analysis.

This approach provides reliability, auditability, and efficiency necessary for database-sourced, batch-oriented data analytics projects using chosen Java stack.

## Detail Designs of Services

1. [Data Ingestion Service](/design/Data-Ingestion-Service-Design.md)
2. [Data Modelling Service](/design/Data-Modelling-Service-Design.md)
