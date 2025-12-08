# DataEngineeringPortal

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.0.2.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Build and Run Docker Image

1. **Rebuild the Docker image:**
   bash

   ```
   docker build . -t data-engineering-portal:latest
   ```
2. **Stop and remove the old container:**
   bash

   ```
   docker stop data-engineering-portal
   docker rm data-engineering-portal
   ```
3. **Run the new container:**
   bash

   ```
   docker run -d -p 80:80 --name data-engineering-portal data-engineering-portal:latest
   ```

```
docker-compose up --build -d data-engineering-portal
```


### Summary of Flexibility:

* **Local Dev:** **Uses** `proxy.conf.json`, runs on 4200.
* **Docker Desktop:** **Uses** `nginx.conf` **with proxy passes to internal service names.**
* **GCP:** **Requires dynamic environment variable injection in the Angular build to set the API Gateway URL as the base URL, and a simple** `nginx.conf` **that only serves static files.**


To handle dynamic environments like GCP, Docker Desktop, and local dev without rebuilding images constantly, we must use an environment variable that the frontend code can access at runtime.

This is the most flexible approach for the three environments you need.

1. Update the Angular `Api.ts` Service to Read from the Environment
2. Configure `environment.ts` for Local Dev

**`src/environments/environment.ts`**

typescript

```
export const environment = {
  production: false,
  apiUrl: '' // Empty string for local development using proxy.conf.json
};
```


3. Configure `environment.prod.ts` for Docker/GCP

This file defines the default production environment URL.

* **For Docker Desktop testing:** **Use the empty string** `''` **again, because the Nginx reverse proxy handles the routing using relative paths.**
* **For GCP:** **This is where you would** *dynamically inject* **the actual API Gateway URL (**`https://bankingplatform.hibersoft.net`) during your CI/CD pipeline build step.

**`src/environments/environment.prod.ts`**

typescript

```
export const environment = {
  production: true,
  // Default to empty string for Nginx reverse proxy in Docker Desktop testing
  // In a real CI/CD pipeline, this value is replaced dynamically with the GCP API Gateway URL
  apiUrl: '' 
};
```


4. Finalize `nginx.conf` (for Docker Desktop)

Keep the `nginx.conf` designed for Docker Desktop that acts as a reverse proxy, as this allows `environment.prod.ts` to remain `apiUrl: ''` for your local Docker testing.
