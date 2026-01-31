# Chronos Spring Boot Server

A simple Spring Boot server with a ping endpoint and H2 database for simulation purposes.

## Features

-   **Ping Endpoint**: A simple health check-like endpoint.
-   **H2 Database**: In-memory database for data persistence (simulation).
-   **Documentation**: PUML sequence diagrams included.

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

## H2 Database Console

The application includes the H2 Console for exploring the in-memory database.

-   **URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
-   **JDBC URL**: `jdbc:h2:mem:simulation`
-   **User**: `sa`
-   **Password**: *(empty)*

## Documentation

Sequence diagrams are located in the `docs` directory:
-   `docs/sequence.puml`: Sequence diagram for the ping flow.
