export const environment = {
  production: true,
  // Default to empty string for Nginx reverse proxy in Docker Desktop testing
  // In a real CI/CD pipeline, this value is replaced dynamically with the GCP API Gateway URL
  apiUrl: '',
};
