// src/app/models/segmentation.types.ts

/**
 * Represents a customer's aggregated spending profile expected by the Python API.
 * Values are strings to handle potential BigDecimal precision issues during transfer.
 */
export interface CustomerProfile {
  Utilities: string;
  Groceries: string;
  Transport: string;
  Shopping: string;
  Dining: string;
  Other: string;
}

/**
 * The response structure from the /predict-segment endpoint.
 */
export interface PredictionResponse {
  predictions: number[]; // K-Means cluster IDs (e.g., [0, 3, 1])
}

// You can add generic types here too, if you want to consolidate
// export interface AnalyticsMap { [key: string]: string; }
