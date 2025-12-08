## Local Deployment

Build the Docker Image

Navigate to the root directory of your Angular project in your terminal and run the build command:\*\* \*\*

bash

```
docker build . -t data-engineering-portal-frontend:latest
```

Run the Container (Test Locally)\*\* \*\*

You can test the image locally before integrating it into your `docker-compose.yml`:\*\* \*\*

bash

```
docker run -d -p 8080:80 --name angular-portal-container data-engineering-portal-frontend:latest
```

## Cloud Deployment

To deploy your application to GCP, where microservice URLs are dynamic (e.g., in Cloud Run), you must modify your CORS configurations to avoid hardcoding specific URLs. The best practice is to manage CORS centrally using an API Gateway or by dynamically setting the `Access-Control-Allow-Origin` header in your services based on allowed patterns.\*\* \*\*

Strategy for GCP Deployment: The API Gateway Approach

The most scalable and secure approach is to use **Google Cloud API Gateway**. The API Gateway handles all incoming requests, manages security and CORS centrally, and then routes the traffic to the correct, dynamically assigned Cloud Run endpoints.\*\* \*\*

This keeps your backend services free of security code and allows for dynamic URLs.

1. Centralize CORS in the API Gateway\*\* \*\*

When you configure your API Gateway on GCP, you define the CORS policy in the OpenAPI configuration file. The Gateway then handles preflight (OPTIONS) requests and adds the correct headers to responses, ensuring your browser accepts the interaction.\*\* \*\*

2. Implement Dynamic Origin Validation in the Backend (Alternative)\*\* \*\*

If you must manage CORS within your application code, you need a method to dynamically check the incoming `Origin` header against a whitelist of allowed patterns (like `*.yourapp.com`) and then reflect that specific origin back in the `Access-Control-Allow-Origin` response header.\*\* \*\*

- **Spring Boot (`WebConfig.java`):** **You can modify your** `WebConfig` **to use** `allowedOriginPatterns` **instead of hardcoded** `allowedOrigins` **to support subdomains or dynamic URLs.**
  java

  ```
  @Configuration
  public class WebConfig implements WebMvcConfigurer {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
          registry.addMapping("/**")
                  // Use a pattern that matches your production domain in GCP
                  .allowedOriginPatterns("https://*.yourappdomain.com", "http://localhost:*")
                  .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                  .allowedHeaders("*")
                  .allowCredentials(true);
      }
  }
  ```
- **Python/Flask (`app.py`):** **The** `flask-cors` **library handles this similarly when configured correctly for dynamic origins.**

Recommended GCP Deployment Plan

1. **Containerize and Push to Artifact Registry:** **Instead of Docker Hub, push your images to GCP Artifact Registry, which is more secure for a banking platform.**
   - `docker tag yourusername/image:latest us-central1-docker.pkg.dev/your-gcp-project/repository/image:latest`
2. **Deploy to Cloud Run:** **Deploy each microservice as a private Cloud Run service.**
3. **Implement API Gateway:** **Create a Google Cloud API Gateway that sits in front of your Cloud Run services. Configure CORS and authentication policies here.**

This approach is robust, scalable, and adheres to security best practices for a cloud environment.
