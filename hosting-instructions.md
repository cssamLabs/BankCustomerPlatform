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
