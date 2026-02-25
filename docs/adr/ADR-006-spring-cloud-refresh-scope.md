# ADR-006 – Use Spring Cloud RefreshScope for Runtime Configuration

| Property    | Value                        |
|-------------|------------------------------|
| **Status**  | Accepted                     |
| **Date**    | 2025-01-01                   |
| **Authors** | Chronos Team                 |

---

## Context

Chronos exposes a `RuntimePropertyController` that allows selected configuration properties
(e.g. `demo.message`) to be updated at runtime without restarting the application.
This pattern is useful during simulation runs where tuning parameters need to be adjusted
on a live instance without downtime.

Spring Boot's standard `@Value` and `@ConfigurationProperties` beans are singletons and
are only injected once at startup; they do not reflect changes made to the underlying property source.

## Decision

Adopt **Spring Cloud** (`spring-cloud-starter`, BOM `2023.0.6`) to unlock `@RefreshScope`:

- `DemoProperties` is annotated with `@RefreshScope` and `@ConfigurationProperties(prefix = "demo")`.
- The `RuntimePropertyController` (and `RuntimePropertySourceUpdater`) write new key-value pairs
  into a custom `MutablePropertySource` and then trigger a `RefreshScopeRefreshedEvent`
  (via `ContextRefresher`) to reload all `@RefreshScope` beans.
- The Actuator `/actuator/refresh` endpoint is also available as a secondary trigger.

The Spring Cloud BOM is imported via `<dependencyManagement>` to guarantee version alignment
between `spring-cloud-context` and the Spring Boot version in use.

## Alternatives Considered

| Alternative                         | Reason Rejected                                                       |
|-------------------------------------|-----------------------------------------------------------------------|
| Restart application on config change | Causes downtime; not acceptable for long-running simulation jobs    |
| Spring Cloud Config Server           | Heavyweight; requires a dedicated config service deployment           |
| Custom `ApplicationListener`         | Re-implements what `@RefreshScope` + `ContextRefresher` already provide cleanly |
| Environment variable overrides       | Requires OS-level access and a restart to take effect                 |

## Consequences

**Positive**
- Properties can be changed on a live instance without restart or redeployment.
- The mechanism is self-contained (no external config server needed).
- `@RefreshScope` integrates cleanly with Spring Boot's existing DI and property resolution model.

**Negative / Trade-offs**
- Adding `spring-cloud-starter` increases the dependency graph and startup classpath.
- All beans that should respond to property changes must be explicitly annotated with `@RefreshScope`; forgetting this annotation silently leaves a bean using stale values.
- `ContextRefresher.refresh()` is a relatively heavy operation that briefly locks the context; avoid calling it on hot paths.

