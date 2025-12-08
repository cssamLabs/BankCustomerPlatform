# AI/ML Use Case: Customer Lifestyle Segmentation

This use case uses **unsupervised learning (Clustering)** to group customers into distinct "lifestyle segments" based on their spending patterns.

### Why this is a good use case:

- **Uses Existing Data:** **It leverages the categorized transaction data we already have (**`amount`, `category`, `location`).
- **Provides Comparative Insight:** **You can compare which segments are dominant in which banks, offering comparative performance metrics.**
- **Actionable Advice:** **The results directly lead to business recommendations (e.g., "Bank A is missing the 'Luxury Spender' segment; they should launch a premium credit card program").**
- **Achievable Complexity:** **K-Means clustering is a standard algorithm that is relatively simple to implement with Python's Scikit-learn library.**

### Data Science Approach: K-Means Clustering

We will analyze spending patterns across categories for each unified customer ID.

1. **Feature Engineering:** **For each unique** `unified_customer_id`, we will aggregate the total spending per category (Utilities, Groceries, Transport, Shopping, Dining, Other) into a single customer profile vector.
2. **Clustering:** **The K-Means algorithm will identify natural groupings in this data (e.g., "The Frugal Saver", "The Big Spender", "The Diner", "The Online Shopper").**
3. **Inference:** **When a new transaction comes in, we can classify the customer and understand which segment they belong to.**

### Integration into the Platform

We will add a new microservice: `customer-segmentation-service` (Python/FastAPI/Scikit-learn).

1. **New Column:** **Add a** `customer_segment` **column to the** `FACT_TRANSACTIONS` **table.**
2. **Modelling Service Calls ML Service:** **During the batch process, after MDM enrichment and categorization, the data modelling service will call the ML service API to get the segment ID for that customer and save it to the fact table.**



Asynchronous Training Trigger

Running a long-running, CPU-intensive data training job synchronously within a web server process (like Gunicorn or Flask's server) is a **major anti-pattern in microservice architectures** and can cause performance issues or server timeouts.** **

The request should be handled **asynchronously**.

**`train_model.py` (Offline Script):**



* **Purpose:** **The** *creation* **of the machine learning model artifacts. It connects to the database, aggregates data, trains the K-Means model, and saves the resulting** `kmeans_model.joblib` **and** `scaler.joblib` **files to your disk.**
* **When it runs:** **This runs manually or on an offline schedule (e.g., weekly retraining)** *before* **you build your Docker image.**
*
