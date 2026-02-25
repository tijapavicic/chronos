# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records for the **Chronos** project.

ADRs capture important architectural decisions made during development, along with
the context and consequences of each decision.

## Format

Each ADR follows the [MADR](https://adr.github.io/madr/) (Markdown Architectural Decision Records) template:

| Field        | Description                                                  |
|--------------|--------------------------------------------------------------|
| **Status**   | `Proposed` · `Accepted` · `Deprecated` · `Superseded by`    |
| **Context**  | The situation that motivated the decision                    |
| **Decision** | The chosen approach                                          |
| **Consequences** | Trade-offs, benefits and drawbacks                       |

## Index

| ID    | Title                                                                   | Status   |
|-------|-------------------------------------------------------------------------|----------|
| [ADR-001](ADR-001-spring-boot-framework.md)       | Use Spring Boot as the Application Framework     | Accepted |
| [ADR-002](ADR-002-h2-in-memory-database.md)       | Use H2 In-Memory Database for Development/Test   | Accepted |
| [ADR-003](ADR-003-springdoc-openapi.md)           | Use Springdoc OpenAPI for API Documentation      | Accepted |
| [ADR-004](ADR-004-spring-security-basic-auth.md)  | Protect Swagger UI with HTTP Basic Auth          | Accepted |
| [ADR-005](ADR-005-lombok-mapstruct.md)            | Use Lombok and MapStruct for Boilerplate Reduction | Accepted |
| [ADR-006](ADR-006-spring-cloud-refresh-scope.md)  | Use Spring Cloud RefreshScope for Runtime Config  | Accepted |
| [ADR-007](ADR-007-docker-containerisation.md)     | Containerise the Application with Docker          | Accepted |
| [ADR-008](ADR-008-uuid-primary-key.md)            | Use UUID as Primary Key for Entities              | Accepted |

