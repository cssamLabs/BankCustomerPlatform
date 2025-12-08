# Databricks notebook source
import joblib
import pandas as pd
from flask import Flask, request, jsonify, abort
from flask_cors import CORS
import threading # Import threading
import os
# Assumes train_model_logic function is available 
from train_model import train_and_save_model, fetch_data_for_training, FEATURES 
import logging

app = Flask(__name__)
# Initialize CORS and define the allowed origins
CORS(app, resources={r"/api/v1/segmentation/*": {"origins": ["https://*.hibersoft.net","http://localhost", "http://localhost:80", "http://localhost:4200"]}})

API_PREFIX = "/api/v1/segmentation"

# Load the pre-trained model and scaler (handle potential file absence at startup if retraining in place)
try:
    model = joblib.load("kmeans_model.joblib")
    scaler = joblib.load("scaler.joblib")
    logging.info("Model and scaler loaded successfully.")
except FileNotFoundError:
    logging.warning("Model or scaler files not found. Prediction endpoint will not work until training run.")
    model = None
    scaler = None

@app.route('{API_PREFIX}/predict-segment', methods=['POST'])
def predict():
    if model is None or scaler is None:
        return jsonify({"error": "Model not ready for predictions. Please trigger training first."}), 503

    data = request.json
    if not data or not isinstance(data, list):
        return jsonify({"error": "Invalid input format, expected a list of customer profiles."}), 400

    # the prediction logic  ...
    df = pd.DataFrame(data)
    if not all(feature in df.columns for feature in FEATURES):
         return jsonify({"error": "Missing required features."}), 400

    data_scaled = scaler.transform(df[FEATURES])
    predictions = model.predict(data_scaled)
    
    return jsonify({"predictions": predictions.tolist()})


@app.route('{API_PREFIX}/trigger-training', methods=['POST'])
def trigger_training():
    """
    Starts model training in a background thread and returns immediately.
    """
    # Use threading to run the function in the background
    thread = threading.Thread(target=run_training_job_in_background)
    thread.daemon = True # Allows program to exit even if thread is running
    thread.start()

    return jsonify({"status": "STARTED", "message": "Model retraining started in the background. Check logs for progress."}), 202


def run_training_job_in_background():
    """Helper function to run the training process."""
    try:
        logging.info("Background training job starting...")
        df = fetch_data_for_training()
        if not df.empty:
            train_and_save_model(df)
            logging.info("Background training job completed.")
            # OPTIONAL: You may need logic here to reload the 'model' and 'scaler' variables 
            # in the main app process if you want the new model to be used immediately without service restart.
            # This is complex in a WSGI environment and usually requires a service restart/reload strategy.
        else:
            logging.warning("Background training job failed: No data found.")
    except Exception as e:
        logging.error(f"Error during background training: {e}")

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)

