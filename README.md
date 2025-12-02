
# Bank Customer Platform Monorepo

This repository hosts all microservices and the web frontend for the "Hibersoft MS Bank Customer Platform". The platform is designed to ingest raw banking data, model customer behavior using high-performance batch processing, analyze the results with GPU acceleration, and visualize the outcomes.

## Project Structure

This is a monorepo containing multiple independent projects:



| Service Name              | Description                                                                                          | Language/Framework     | Status         |
| ------------------------- | ---------------------------------------------------------------------------------------------------- | ---------------------- | -------------- |
| `data-modeling-service`   | Ingests raw transaction data, stages it, enriches it, and loads final facts into the data warehouse. | Java/Spring Boot/Batch | Functional     |
| `data-generation-service` | Generates simulated raw data for testing the ingestion pipeline.                                     | Java/Spring Boot/JPA   | In Development |
| `data-analytics-service`  | High-performance service for querying the data warehouse and generating insights.                    | Java/Spring Boot/JPA   | Planned        |
| `data-engineering-portal` | React/Next.js frontend for visualization and administration.                                         | JavaScript/React       | Planned        |

Getting Started (Local Development with Docker)

To run the entire platform locally, you must have Docker and Docker Compose installed.

Prerequisites

1. **Build the Docker Images:**
   Navigate to each service directory (`data-modeling-service`, `data-generation-service` **once built) and run the Spring Boot build command:**
   bash

   ```
   ./gradlew bootBuildImage
   ```

2. **Ensure Docker Login:**
   Make sure you are logged into Docker Hub: `docker login`

Deployment

From the root of this monorepo, deploy the entire stack using `docker-compose`:

bash

```
docker-compose up -d
```

Endpoints (Data Modelling Service)

The Data Modelling service is available at `http://localhost:8080`.

| Endpoint                           | Method | Description                                          | Example URL                                           |
| ---------------------------------- | ------ | ---------------------------------------------------- | ----------------------------------------------------- |
| `/api/v1/ingestion/start/{bankId}` | `POST` | Triggers the data ingestion job for a specific bank. | `http://localhost:8080/api/v1/ingestion/start/BANK_A` |
| `/actuator/health`                 | `GET`  | Health check for the service.                        | `http://localhost:8080/actuator/health`               |
