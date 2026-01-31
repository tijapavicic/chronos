# Chronos Spring Boot Server

A simple Spring Boot server with a ping endpoint and H2 database for simulation purposes.

## Features

-   **Ping Endpoint**: A simple health check-like endpoint.
-   **H2 Database**: In-memory database for data persistence (simulation).
-   **Documentation**: PUML sequence diagrams included.
-   **OpenAPI / Swagger UI**: Interactive API docs available via Springdoc OpenAPI.

## Prerequisites

-   Java 17 or higher
-   Maven 3.6+

## Getting Started

### Installation

1.  Clone the repository (if applicable) or navigate to the project directory:
    ```bash
    cd chronos
    ```

2.  Build the project:
    ```bash
    mvn clean install
    ```

### Running the Application

Run the application using the Maven plugin:
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`.

## API Endpoints

### Simulation

-   **GET** `/simulation/ping`
    -   Returns `200 OK`.
    -   Used to verify the server is running and reachable.

## Swagger / OpenAPI (Springdoc)

This project includes Springdoc OpenAPI. After starting the application, the OpenAPI UI and JSON are available at the following URLs:

-   Swagger UI (interactive): http://localhost:8080/swagger-ui/index.html
-   Raw OpenAPI JSON: http://localhost:8080/v3/api-docs

If you change the server context path or port, update the URLs accordingly.

### Disabling Swagger in Production

Swagger is enabled by default in non-production profiles. To disable it in production the project includes `src/main/resources/application-production.properties` which sets:

```properties
swagger.enabled=false
```

Set `--spring.profiles.active=production` when running the app in production or configure your deployment to use that profile.

### Optional: Protect Swagger UI with Basic Auth

You can enable HTTP Basic auth for the Swagger UI and OpenAPI JSON by setting the following properties (disabled by default):

```properties
swagger.security.enabled=true
swagger.security.username=youruser
swagger.security.password=yourpass
```

When enabled, access to `/swagger-ui/**` and `/v3/api-docs/**` will require the configured username and password.

## CI / GitHub Actions

A GitHub Actions workflow is included at `.github/workflows/ci.yml`.

- It runs on pushes and pull requests to `main`/`master`.
- It builds the project using JDK 17 and Maven, runs tests, starts the built jar briefly and fetches the generated OpenAPI JSON (`/v3/api-docs`) and uploads it as a workflow artifact named `openapi-json`.
- The workflow also generates a Java client SDK using OpenAPI Generator and uploads it as artifact `openapi-client`.
- It also renders PlantUML diagrams and uploads them as `diagrams` artifact.

## Validation provider

To ensure Jakarta Bean Validation is available at runtime, the project includes Hibernate Validator as a dependency. This eliminates startup warnings about no validation provider being found.

## H2 Database Console

The application includes the H2 Console for exploring the in-memory database.

-   **URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
-   **JDBC URL**: `jdbc:h2:mem:simulation`
-   **User**: `sa`
-   **Password**: *(empty)*


## Documentation

Sequence diagrams are located in the `docs` directory:
-   `docs/sequence.puml`: Sequence diagram for the ping flow.
