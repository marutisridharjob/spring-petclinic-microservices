
# Spring PetClinic Microservices: Developer Guide

This guide provides all the necessary information for developers to build, run, test, and monitor the Spring PetClinic microservices application.

## 1. Introduction

This project is a microservices-based version of the classic Spring PetClinic application. It is designed to showcase how to build a distributed system using the Spring Boot and Spring Cloud frameworks. The application is composed of several small, independent services that work together to provide the full functionality of the PetClinic.

## 2. Prerequisites

Before you can build and run this project, you will need to have the following tools installed on your system:

*   **Java Development Kit (JDK)**: Version 21 or higher.
*   **Docker**: To run the microservices in containers.
*   **Node.js and npm**: Required for building the frontend of the `api-gateway`. The project is configured to use Node.js version 20.11.0 and npm version 10.2.4.

## 3. Building the Project

To build the entire project and create the necessary Docker images, run the following command from the root directory of the project:

```sh
./gradlew clean build
```

This command will compile the code, run the tests, and build the Docker images for each microservice using the Jib plugin.

## 4. Running the Services

All the microservices are configured to run in Docker containers. The `docker-compose.yml` file in the root of the project orchestrates the startup of all the services.

To start all the services, run the following command:

```sh
docker-compose up -d
```

This will start all the services in detached mode. You can view the logs for all the services using the following command:

```sh
docker-compose logs -f
```

### Service Ports

The following table lists all the services and the ports they are exposed on:

| Service             | Port   | URL                             |
| ------------------- | ------ | ------------------------------- |
| API Gateway         | 8080   | http://localhost:8080           |
| Config Server       | 8888   | http://localhost:8888           |
| Discovery Server    | 8761   | http://localhost:8761           |
| Admin Server        | 9090   | http://localhost:9090           |
| Tracing Server (Zipkin) | 9411   | http://localhost:9411           |
| Grafana             | 3030   | http://localhost:3030           |
| Prometheus          | 9091   | http://localhost:9091           |

## 5. Dependency Management

The project uses a multi-module Gradle build. The root `build.gradle` file defines the versions of all the major dependencies in the `ext` block and applies common plugins and dependencies to all subprojects. This ensures consistency across all the microservices.

When adding a new dependency to a microservice, you should first check if the version is already managed by the root `build.gradle`. If it is, you can add the dependency to the `dependencies` block of the subproject's `build.gradle` file without specifying a version.

If you need to add a new dependency with a version that is not managed by the root project, you should add it to the `dependencyManagement` block in the root `build.gradle` file to ensure it is used consistently across all modules.

## 6. Testing

### Automated Tests

The project includes a suite of unit and integration tests for each microservice. To run all the tests, you can use the following Gradle command:

```sh
./gradlew test
```

### Manual Testing

You can manually test the application using your web browser or a command-line tool like `curl`.

#### Browser Testing

1.  **Open the application**: Navigate to http://localhost:8080 in your browser.
2.  **View veterinarians**: Click on the "Veterinarians" tab.
3.  **Find an owner**: Click on the "Find Owners" tab, enter a last name (e.g., "Franklin"), and click "Find Owner".
4.  **View owner and pet details**: Click on the owner's name to see their details and the details of their pets.

#### Command-Line Testing (curl)

You can use `curl` to interact with the API gateway.

*   **Get all vets**:
    ```sh
    curl http://localhost:8080/api/gateway/vets
    ```
*   **Get a specific owner**:
    ```sh
    curl http://localhost:8080/api/gateway/owners/1
    ```

## 7. Observability

The project includes a comprehensive observability stack that allows you to monitor the health and performance of the microservices.

*   **Spring Boot Admin**: http://localhost:9090
    *   Provides a dashboard for managing and monitoring your Spring Boot applications.
*   **Zipkin**: http://localhost:9411
    *   Provides distributed tracing to visualize the flow of requests between services.
*   **Grafana**: http://localhost:3030
    *   Provides dashboards for visualizing metrics collected by Prometheus. The default login is `admin`/`admin`.
*   **Prometheus**: http://localhost:9091
    *   Collects metrics from the microservices.

## 8. Troubleshooting

### Grafana is unreachable

If you are unable to access Grafana at http://localhost:3030, it is likely due to a port mapping issue in the `docker-compose.yml` file. Ensure that the port mapping for the `grafana-server` is `3030:3000` and restart the container:

```sh
docker-compose up -d --force-recreate grafana-server
```
