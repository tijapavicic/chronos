# ADR-003 – Use Springdoc OpenAPI for API Documentation

| Property    | Value                        |
|-------------|------------------------------|
| **Status**  | Accepted                     |
| **Date**    | 2025-01-01                   |
| **Authors** | Chronos Team                 |

---

## Context

Chronos exposes REST endpoints for simulation control, facility management, and runtime property updates.
Consumers (frontend teams, integration partners, QA) need accurate, always-up-to-date API documentation
without maintaining a separate spec file by hand.

## Decision

Adopt **Springdoc OpenAPI** (`springdoc-openapi-starter-webmvc-ui` version `2.1.0`) to generate and serve
OpenAPI 3 documentation automatically from Spring MVC annotations.

This provides:
- Swagger UI at `/swagger-ui/index.html` for interactive exploration.
- Raw OpenAPI JSON at `/v3/api-docs` for tooling integration (Postman import, code generation).
- Enriched documentation via `@Operation`, `@ApiResponse`, and `@Tag` annotations directly on controllers.

The `OpenApiConfig` Spring `@Configuration` class is used to customise the API info block (title, version, description).

## Alternatives Considered

| Alternative                  | Reason Rejected                                                         |
|------------------------------|-------------------------------------------------------------------------|
| SpringFox (Swagger 2)        | Unmaintained; does not support Spring Boot 3 / Jakarta EE namespace     |
| Manual OpenAPI YAML/JSON     | Drift risk; requires manual synchronisation with code changes           |
| Redoc standalone             | Presentation-only; no interactive "try it out" capability               |

## Consequences

**Positive**
- Documentation stays in sync with the code automatically.
- Swagger UI reduces friction for API consumers and QA testers.
- OpenAPI JSON enables Postman collection generation and client code generation via OpenAPI Generator.

**Negative / Trade-offs**
- Swagger UI is a potential attack surface in production; it must be disabled or secured (see ADR-004).
- Annotation verbosity increases on controller classes.
- Springdoc version must be kept compatible with the Spring Boot version in use.

