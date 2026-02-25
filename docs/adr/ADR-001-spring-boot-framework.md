# ADR-001 – Use Spring Boot as the Application Framework

| Property    | Value                        |
|-------------|------------------------------|
| **Status**  | Accepted                     |
| **Date**    | 2025-01-01                   |
| **Authors** | Chronos Team                 |

---

## Context

Chronos is a Java-based simulation/scheduling backend that needs to:

- Expose REST endpoints for simulation control and facility management.
- Integrate with a relational database for entity persistence.
- Support security, health-checks, and runtime configuration refresh.
- Be runnable both locally and inside a Docker container.

A production-grade, opinionated framework was required to reduce configuration overhead and accelerate delivery.

## Decision

We adopt **Spring Boot 3.x** (currently `3.4.1`) as the single foundational framework, leveraging:

- `spring-boot-starter-web` – embedded Tomcat, MVC, REST.
- `spring-boot-starter-data-jpa` – JPA/Hibernate ORM.
- `spring-boot-starter-security` – authentication and authorisation.
- `spring-boot-starter-actuator` – production-ready health/info endpoints.
- `spring-cloud-starter` – `@RefreshScope` and externalized config refresh.

The Java version is pinned to **Java 17** (LTS).

## Alternatives Considered

| Alternative     | Reason Rejected                                                    |
|-----------------|--------------------------------------------------------------------|
| Quarkus         | Smaller adoption in the existing team; less mature JPA ecosystem   |
| Micronaut       | Compile-time DI model adds learning curve; Spring familiarity preferred |
| Jakarta EE (bare) | Too much manual configuration; no opinionated auto-configuration |
| Plain Java / Spark | Not suitable for enterprise-grade feature set required          |

## Consequences

**Positive**
- Large ecosystem, extensive documentation, and widespread team familiarity.
- Auto-configuration drastically reduces boilerplate setup.
- Native support for profiles enables clean dev/staging/production configuration separation.
- Actuator provides `/health`, `/info`, and `/refresh` out of the box.

**Negative / Trade-offs**
- Spring Boot's opinionated defaults occasionally require explicit overrides (e.g., disabling security for H2 console).
- Startup time is longer than reactive or native-compiled alternatives.
- Classpath size is larger compared to micro-framework alternatives.

