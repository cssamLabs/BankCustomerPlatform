# `customer-segmentation-service`

This Python microservice provides AI capabilities for your banking platform. It serves two main functions:

1. **Prediction Endpoint (`POST /api/v1/segmentation/predict-segment`):** **Takes a customer's spending data and returns a K-Means cluster ID (e.g., segment 0, 1, 2, or 3), identifying their lifestyle segment (e.g., "Frequent Online Shopper" vs. "Utilities Only").**
2. **Training Trigger (`POST /api/v1/segmentation/trigger-training`):** **Kicks off an asynchronous job that retrains the underlying ML model using the latest data from the PostgreSQL database, ensuring the AI model stays current**

## How to Use the Predictor from the UI

1.  Input the Spending Profile

You will see the form you built using the `SegmentPredictorComponent`.

- **Enter a sample spending amount (e.g., 500) into each category field (**`Utilities`, `Groceries`, `Transport`, etc.). These are the exact features the K-Means model expects.

2.  Get the Prediction

- **Click the** **"Get Segment" button.**

3.  Whats happening

- **The Angular application (running in Nginx) makes an HTTP** `POST` **request to** `/api/v1/segmentation/predict-segment`.
- routes this request to the Python container at `http://customer-segmentation-app:5000`
- **The Flask** `app.py` **receives the request, loads the pre-trained** `kmeans_model.joblib` **file in memory, scales the input data, runs the prediction algorithm, and returns a JSON response:** `{"predictions": [1]}`.
- **The Angular UI parses this response and displays "Segment ID 1" in the result box.**

This user interaction flow completes the cycle of your AI microservice architecture.

## Deployment

Step 1: Rebuild the Docker Image

Navigate to your Python project directory and build the image. This process should now copy your valid model files into the image.

bash

```
docker build . -t hs-bank-customer-data-segmentation:latest
```

Step 2: Deploy only the Segmentation App (using Docker Compose)

Go to your main project directory (where `docker-compose.yml` is located) and deploy only this specific service using its service name:

bash

```
docker compose up --build -d data-segmentation-app
```

Verification

Once the container starts, check its logs immediately:

bash

```
docker logs data-segmentation-app
```
