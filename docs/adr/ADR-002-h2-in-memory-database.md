# ADR-002 – Use H2 In-Memory Database for Development and Testing

| Property    | Value                        |
|-------------|------------------------------|
| **Status**  | Accepted                     |
| **Date**    | 2025-01-01                   |
| **Authors** | Chronos Team                 |

---

## Context

Chronos requires a relational database to persist domain entities such as `Facility`.
During development and automated testing the team needs:

- Zero-infrastructure setup (no external DB process required).
- Fast test cycles with a clean schema on every run.
- A browsable console for ad-hoc SQL inspection (`/h2-console`).

A separate persistent datastore (e.g. PostgreSQL) will be used in staging and production environments.

## Decision

Use **H2** (`com.h2database:h2`) as an in-memory relational database for the `default` and `test` Spring profiles.

Key configuration choices:
- `spring.datasource.url=jdbc:h2:mem:chronosdb` – pure in-memory, per-JVM lifecycle.
- `spring.h2.console.enabled=true` – browser-based SQL console enabled for local development.
- Schema and seed data initialised via `data.sql` on application startup.
- H2 console path (`/h2-console`) is explicitly **permitted** in the security filter chain (see ADR-004).

## Alternatives Considered

| Alternative          | Reason Rejected                                                      |
|----------------------|----------------------------------------------------------------------|
| PostgreSQL (always)  | Requires Docker Compose or an installed PG instance; adds setup friction for developers |
| HSQLDB               | Fewer Spring Boot examples; H2 is the Spring Boot default recommendation |
| TestContainers PG    | Viable for integration tests but heavier; not needed for unit-level simulation |

## Consequences

**Positive**
- No external dependency for local development or CI.
- Schema is recreated fresh on each application start, preventing test pollution.
- H2 console accelerates data debugging without external tools.

**Negative / Trade-offs**
- H2 SQL dialect differs subtly from PostgreSQL; migration scripts must be validated against a real DB before promotion.
- In-memory storage is lost on restart; not suitable for any persistent simulation state.
- The H2 console endpoint must be carefully guarded or disabled in environments beyond local development.

