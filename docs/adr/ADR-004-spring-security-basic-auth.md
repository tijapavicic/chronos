# ADR-004 – Protect Swagger UI with HTTP Basic Authentication

| Property    | Value                        |
|-------------|------------------------------|
| **Status**  | Accepted                     |
| **Date**    | 2025-01-01                   |
| **Authors** | Chronos Team                 |

---

## Context

Once Springdoc OpenAPI is enabled (ADR-003), the Swagger UI and raw OpenAPI spec are accessible at
well-known paths (`/swagger-ui/**`, `/v3/api-docs/**`).
In non-local environments these endpoints must not be publicly reachable, since they reveal the full API surface.

The `spring-boot-starter-security` dependency is already on the classpath (pulled in to satisfy other requirements),
so a lightweight security arrangement is available at no additional cost.

## Decision

Implement a **feature-flagged HTTP Basic Auth** guard for the Swagger UI using Spring Security:

- A `SecurityConfig` `@Configuration` class is activated only when `swagger.security.enabled=true`
  (via `@ConditionalOnProperty`).
- When active, `/swagger-ui/**` and `/v3/api-docs/**` require the `ROLE_SWAGGER` authority.
- Credentials (`swagger.security.username` / `swagger.security.password`) are externalised as properties
  and default to `swagger`/`swagger` for local development.
- Passwords are stored as BCrypt hashes (`BCryptPasswordEncoder`).
- A complementary `SecurityPermitAllConfig` is activated when the flag is **false**, permitting all requests
  (pure local development mode).
- Actuator `/health` and `/info` remain publicly accessible; other actuator endpoints require `ROLE_ACTUATOR`.
- The H2 console (`/h2-console/**`) is always permitted and CSRF + frame-options protections are disabled
  for it (local dev only; blocked in production via the feature flag).

## Alternatives Considered

| Alternative                  | Reason Rejected                                                            |
|------------------------------|----------------------------------------------------------------------------|
| OAuth2 / OIDC                | Over-engineered for a documentation guard; requires an external IdP        |
| API Gateway / reverse proxy  | Valid at infra level but adds deployment complexity for dev environments    |
| Disabling Swagger entirely in production | Acceptable but loses convenience; Basic Auth is a lighter middle ground |
| IP allowlist at network level | Infrastructure-dependent; not portable across environments                 |

## Consequences

**Positive**
- Zero external dependencies; feature flag keeps local development frictionless.
- BCrypt hashing prevents plain-text credential exposure.
- Per-role security rules give fine-grained control over actuator and Swagger paths independently.

**Negative / Trade-offs**
- HTTP Basic Auth transmits credentials on every request; requires HTTPS in non-local environments.
- In-memory `UserDetailsService` is not suitable for multi-instance deployments without a shared session store.
- Credentials are still stored in properties files; must be injected via secrets manager in production.

