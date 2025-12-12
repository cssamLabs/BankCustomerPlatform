# Databricks notebook source
import pandas as pd
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler
import joblib
from sqlalchemy import create_engine
import os
import logging

logging.basicConfig(level=logging.INFO)

FEATURES = ['utilities', 'groceries', 'transport', 'shopping', 'dining', 'other']
NUM_CLUSTERS = 4 # Define how many lifestyle segments you want

# --- Database Configuration (use environment variables for security) ---
# DB_URL = os.environ.get("DATABASE_URL", "postgresql://youruser:yourpassword@127.0.0.1:5432/datamodelingdb")

def get_db_connection():
    # This must read from the environment variable provided by Docker Compose
    db_url = os.environ.get('DATABASE_URL') 
    
    if not db_url:
        # If the variable is somehow missed, this default might cause issues
        print("DATABASE_URL environment variable not found!")
        # A safer default for local development is often useful, but in Docker, 
        # the service name works better than localhost
        db_url = "postgresql://youruser:yourpassword@postgres-db:5432/datamodelingdb"

    engine = create_engine(db_url)
    return engine

def fetch_data_for_training():
    """
    Fetches categorized transaction data from PostgreSQL and aggregates it 
    into customer-level spending profiles using SQL.
    """
    logging.info("Fetching and aggregating data from database...")
    # This SQL query aggregates total spending per customer ID per category
    sql_query = f"""
    SELECT
        customer_id,
        SUM(CASE WHEN category = 'Utilities' THEN amount_standard ELSE 0 END) AS Utilities,
        SUM(CASE WHEN category = 'Groceries' THEN amount_standard ELSE 0 END) AS Groceries,
        SUM(CASE WHEN category = 'Transport' THEN amount_standard ELSE 0 END) AS Transport,
        SUM(CASE WHEN category = 'Shopping' THEN amount_standard ELSE 0 END) AS Shopping,
        SUM(CASE WHEN category = 'Dining' THEN amount_standard ELSE 0 END) AS Dining,
        SUM(CASE WHEN category = 'Other' THEN amount_standard ELSE 0 END) AS Other
    FROM
        fact_transactions
    GROUP BY
        customer_id
    HAVING
        SUM(amount_standard) > 0; -- Only customers with spending
    """
    engine = get_db_connection()
    df = pd.read_sql_query(sql_query, engine)
    
    logging.info(f"Fetched data for {len(df)} customers.")
    return df

def train_and_save_model(df):
    """
    Trains the K-Means model and a StandardScaler on the features.
    Saves both using joblib.
    """
    X = df[FEATURES]

    # 1. Scale the data (important for K-Means which uses distance metrics)
    logging.info("Training StandardScaler...")
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # 2. Train the K-Means model
    logging.info(f"Training K-Means model with {NUM_CLUSTERS} clusters...")
    kmeans = KMeans(n_clusters=NUM_CLUSTERS, random_state=42, n_init=10)
    kmeans.fit(X_scaled)

    # 3. Save the model and scaler to disk
    joblib.dump(kmeans, "kmeans_model.joblib")
    joblib.dump(scaler, "scaler.joblib")
    logging.info("Model and scaler saved successfully as .joblib files.")

def main():
    data_frame = fetch_data_for_training()
    if not data_frame.empty:
        train_and_save_model(data_frame)
    else:
        logging.warning("No data found for training. Model files not created.")

if __name__ == "__main__":
    main()
