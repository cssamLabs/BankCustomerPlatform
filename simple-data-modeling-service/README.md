### Rebuild the Java Docker Image

bash

```
docker build . -t simple-data-modeling-service:latest
```

Test on Docker Desktop

Run all your services together using Docker Compose. Ensure your `docker-compose.yml` uses the service names correctly.

bash

```
docker compose up --build -d
```
