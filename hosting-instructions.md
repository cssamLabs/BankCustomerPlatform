# Establishing Cloud Host

To use a subdomain like `bankingplatform.hibersoft.net` for your application hosted on Google Cloud Platform (GCP), while the main domain is hosted in AWS, you need to configure DNS records in AWS to point traffic for that specific subdomain to the correct GCP services.

Here is the step-by-step process to establish your subdomain and link it to your GCP infrastructure:

Prerequisites

1. **GCP Project:** **You have an active GCP project where your services (e.g., Cloud Run, App Engine, or API Gateway) will run.**
2. **AWS Route 53:** **Your main domain** `hibersoft.net` **is managed by AWS Route 53 (or wherever your DNS is hosted).**

Step 1: Configure a Public IP Address in GCP

Before you can point a domain name to your GCP application, you need a stable IP address in GCP to receive the traffic.

- **If you are using** **Cloud Run with an API Gateway\*\***, the API Gateway will provide a stable hostname.\*\*
- **If you are using** **Google Kubernetes Engine (GKE) or Compute Engine\*\***, you will likely need a Global External IP Address configured in your VPC network settings.\*\*

You need to know the destination hostname or IP address provided by your chosen GCP service (e.g., the API Gateway URL or GKE Load Balancer IP).

Step 2: Create a DNS Record in AWS Route 53

You need to log into your AWS account and create a new record in the hosted zone for `hibersoft.net` that specifically handles the traffic for `bankingplatform.hibersoft.net`.

1. **Log in** **to the AWS Management Console and navigate to** **Route 53\*\***.\*\*
2. **Select** **the hosted zone for** `hibersoft.net`.
3. **Click** **"Create record".**
4. **Configure the record with the following details:**
   - **Record name:** **Enter** `bankingplatform` **(this creates the full name** `bankingplatform.hibersoft.net`).
   - **Record type:** **Choose** `A` **(for an IP address) or** `CNAME` **(if GCP gives you a hostname, like a Cloud Run URL). A** `CNAME` **is usually simpler for managed GCP services.**
   - **Value:** **Enter the destination IP address or hostname from your GCP service (from Step 1).**
   - **TTL (Time to Live):** **Keep the default or set it lower for quick testing.**

Step 3: Configure SSL/TLS Certificates in GCP

Since this is a banking platform, you must secure traffic with HTTPS.

GCP services like Cloud Run, App Engine, and the API Gateway automatically manage free SSL certificates for custom domains. Once you configure the domain mapping within the GCP service settings (e.g., in the Cloud Run "Custom domains" tab), GCP will provision the certificate and provide you with a set of verification `CNAME` or `TXT` records.

Step 4: Verification (DNS Propagation)

Go back to AWS Route 53 and add the verification records provided by GCP (if necessary).

DNS changes can take a few minutes to an hour to propagate globally. You can verify the new DNS record is pointing correctly using a tool like `dig`:

bash

```
dig bankingplatform.hibersoft.net

```

Once the records point correctly, GCP will automatically finalize the certificate provisioning, and your platform will be live at `bankingplatform.hibersoft.net`.

## Deploy the entire platform to GCP using Cloud Run and API Gateway

### Step 0. Login and create the repo

gcloud auth login

gcloud projects list

gcloud config set project bankingplatform

gcloud services enable artifactregistry.googleapis.com

**Create a new Docker repository** **named** `banking-repo` **in a region like** `us-central1` **(ensure this region is consistent for all deployments):**
bash

```
gcloud artifacts repositories create banking-repo --repository-format=docker --location=us-central1 --description="Banking Platform Microservices"

```

Step 1. Push Docker Images to GCP Artifact Registry

bash

```
PROJECT_ID=$(gcloud config get-value project)
GCP_REPO_URL=us-central1-docker.pkg.dev/$PROJECT_ID/banking-repo

```

We need to get your newly named images (`bankingplatform/...:latest` or `:1.0.0`) from your local machine into a secure GCP registry before deploying them.
bash

```
# Authenticate Docker for GCP
gcloud auth configure-docker us-central1-docker.pkg.dev

```

#### Tag and Push the images (repeat for all 5 services)

bash

```
docker tag hs-bank-customer-data-analytics:0.0.1-SNAPSHOT $GCP_REPO_URL/data-analytics-app:1.0.0
docker push $GCP_REPO_URL/data-analytics-app:1.0.0

docker tag hs-bank-customer-data-generation:0.0.1-SNAPSHOT $GCP_REPO_URL/data-generation-app:1.0.0
docker push $GCP_REPO_URL/data-generation-app:1.0.0

docker tag hs-bank-customer-data-segmentation:latest $GCP_REPO_URL/data-segmentation-app:1.0.0
docker push $GCP_REPO_URL/data-segmentation-app:1.0.0

docker tag hs-bank-customer-simple-datamodeling:0.0.1-SNAPSHOT $GCP_REPO_URL/simple-data-modeling-app:1.0.0
docker push $GCP_REPO_URL/simple-data-modeling-app:1.0.0

docker tag hs-bank-customer-data-portal:latest $GCP_REPO_URL/bank-data-portal:1.0.0
docker push $GCP_REPO_URL/bank-data-portal:1.0.0

```

Remove 

#### Step 2: Deploy to Cloud SQL (Database Setup)

Create database Instance
bash

```
gcloud sql instances create banking-prod --database-version=POSTGRES_15 --region=us-central1 --cpu=1 --memory=4GiB

```

You must now:

* **Configure the database username/password (**`gcloud sql users create...`).
* **Retrieve the connection name:** `gcloud sql instances describe banking-prod` **(Look for** `connectionName`).
* **Create your schema/tables using a local client like DBeaver or psql.**

#### Configure Database User Credentials and Schema

1. Set Passwords/Users: Ensure you have configured the youruser and yourpassword credentials that your services expect in the GCP console.
2. Apply Schema: You need to export the schema from your local PostgreSQL database and import it into the Cloud SQL instance.
3. Use pg_dump locally to export only the schema (not the data):
   bash

   ```
   pg_dump -h localhost -U youruser -s datamodelingdb > schema.sql

   ```
4. Use the gcloud sql databases import command to load that schema file into the GCP instance:
   bash

   Step 1: Create a Google Cloud Storage (GCS) Bucket
   Create a GCS bucket in the same region as your Cloud SQL instance (us-central1).
   bash

   ```
   gcloud storage buckets create gs://hs-banking-platform --location=us-central1
   ```

   Step 2: Upload Your Schema File to the GCS Bucket
   Upload your local schema.sql file to the bucket you just created.

   bash

       ```
       gcloud storage cp schema.sql gs://hs-banking-platform/schema.sql

       ```

   Step 4: Grant GCS Read Permissions to Cloud SQL
   Find the Cloud SQL Service Account Email

   ```
   gcloud sql instances describe banking-postgres-prod --format="value(serviceAccountEmailAddress)"

   ```

   Step 5: Grant the "Storage Object Viewer" Role to the Service Account

   ```
   gcloud storage buckets add-iam-policy-binding gs://hs-banking-platform --member="user:chandima.samaraweera@gmail.com" --role="roles/storage.objectViewer"

   ```

   Step 6: Run the Correct Import Command
   Now use the correct command structure to import the schema from the GCS bucket into your Cloud SQL instance.
   bash

   ```
   gcloud sql import sql banking-prod gs://hs-banking-platform/schema.sql --database=datamodelingdb --user=youruser
   ```



### Step 3: Deploy Backend Microservices to Cloud Run

Deploy your services using the images from the Artifact Registry. Crucially, **do not allow unauthenticated access** for these backend services; they will be accessed via the API Gateway.

bash

```
# Enable the Cloud Run API
gcloud services enable run.googleapis.com

PROJECT_ID=$(gcloud config get-value project)
GCP_REPO_URL=us-central1-docker.pkg.dev/$PROJECT_ID/banking-repo

# Deploy Analytics Service (Java)
gcloud run deploy data-analytics-app --image $GCP_REPO_URL/data-analytics-app:1.0.0 --region us-central1 --no-allow-unauthenticated --platform managed --set-env-vars SPRING_PROFILES_ACTIVE=prod,DB_USER=youruser,DB_PASS=yourpassword --add-cloudsql-instances banking-prod 

gcloud run deploy data-analytics-app --image $GCP_REPO_URL/data-analytics-app:1.0.0 --region us-central1 --no-allow-unauthenticated --platform managed --add-cloudsql-instances banking-postgres-prod --set-env-vars SPRING_PROFILES_ACTIVE=prod,DB_USER=youruser,DB_PASS=yourpassword, SPRING_DATASOURCE_URL="jdbc:postgresql:///datamodelingdb?cloudSqlInstance=bankingplatform:us-central1:banking-prod&socketFactory=com.google.cloud.sql.postgres.SocketFactory"

gcloud run deploy data-generation-app \
    --image us-central1-docker.pkg.dev/bankingplatform/banking-repo/data-generation-app:1.0.0 \
    --region us-central1  \
    --allow-unauthenticated \
    --platform managed \
    --add-cloudsql-instances banking-prod \
    --set-env-vars 'SPRING_PROFILES_ACTIVE=prod'


# Deploy Segmentation Service (Python)
gcloud run deploy segmentation-service --image $GCP_REPO_URL/banking-repo/segmentation-service:1.0.0 --region us-central1 --no-allow-unauthenticated --platform managed --set-env-vars CLOUD_SQL_CONNECTION_NAME="<your-connection-name-from-step2>",DB_USER="...",DB_PASS="..."


# Repeat for ingestion-service and modeling-service
```

Use code with caution.

Step 4: Configure the API Gateway

The API Gateway provides the *single public URL* for your entire application (`https://bankingplatform.hibersoft.net`).

We need to provide IAM permissions first (from previous instructions), then define the gateway using an OpenAPI spec file (`api_config.yaml`).

**`api_config.yaml`:**

yaml

```
swagger: '2.0'
info:
  title: banking-platform-gateway
  version: 1.0.0
x-google-endpoints:
- name: "bankingplatform.hibersoft.net"
  allowCors: true # Enables central CORS management

paths:
  /api/v1/analytics/{path}:
    get:
      x-google-backend:
        # Use the actual GCP Cloud Run URL (e.g., https://analytics-service-xxxx-uc.a.run.app)
        address: https://<ANALYTICS_SERVICE_URL>.run.app{path} 
      responses: { '200': { description: 'A successful response' } }
  
  /api/v1/segmentation/{path}:
    post:
      x-google-backend:
        address: https://<SEGMENTATION_SERVICE_URL>.run.app{path}
      responses: { '200': { description: 'A successful response' } }
  # ... Add paths for ingestion/modeling services ...
```

Use code with caution.

* **ACTION:** **You need the actual Cloud Run URLs from the deployment outputs in Step 3 to complete this file.**

Once the `api_config.yaml` is updated, deploy the gateway:

bash

```
gcloud api-gateway api-configs create banking-api-config --api=banking-platform-gateway --openapi-spec=api_config.yaml --async
gcloud api-gateway gateways create banking-gateway --api=banking-platform-gateway --api-config=banking-api-config --location=us-central1
```

Use code with caution.

The output of the last command gives you the final **GCP Gateway URL** (`https://banking-gateway-xxxx.gateway.dev`).

Step 5: Map Subdomain in AWS Route 53

Finally, go to AWS Route 53 and create a **`CNAME` record** for `bankingplatform.hibersoft.net` that points to that **GCP Gateway URL** (`banking-gateway-xxxx.gateway.dev`).

Your backend services are now live on GCP.
