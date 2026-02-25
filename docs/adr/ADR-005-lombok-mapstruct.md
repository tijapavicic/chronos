# ADR-005 – Use Lombok and MapStruct for Boilerplate Reduction

| Property    | Value                        |
|-------------|------------------------------|
| **Status**  | Accepted                     |
| **Date**    | 2025-01-01                   |
| **Authors** | Chronos Team                 |

---

## Context

Java domain models and DTOs require significant repetitive code: constructors, getters/setters,
`equals`/`hashCode`/`toString`, and object-to-object mapping methods.
Maintaining this by hand creates noise, increases merge conflicts, and introduces subtle bugs
(e.g., missing field in a mapper method).

## Decision

Adopt two compile-time annotation processors:

### Lombok (`org.projectlombok:lombok` `1.18.30`)

Generate boilerplate on domain entities and DTOs using:
- `@Data` – getters, setters, `equals`, `hashCode`, `toString`.
- `@Builder` – fluent builder pattern.
- `@NoArgsConstructor` / `@AllArgsConstructor` – JPA-required no-arg constructor + convenience all-args constructor.

Lombok is declared with `scope=compile` and excluded from the final JAR via the Spring Boot Maven plugin
`<excludes>` configuration.

### MapStruct (`org.mapstruct:mapstruct` / `mapstruct-processor` `1.5.5.Final`)

Generate type-safe, compile-time bean-mapping code (e.g., `Facility` → `FacilityDto`).
The annotation processor is wired in the `maven-compiler-plugin` alongside the Lombok processor,
ensuring both run in the correct order during `javac`.

## Alternatives Considered

| Alternative              | Reason Rejected                                                        |
|--------------------------|------------------------------------------------------------------------|
| Manual boilerplate       | Verbose, error-prone, costly to maintain across many entities          |
| ModelMapper              | Reflection-based at runtime; slower and harder to debug than MapStruct |
| Java Records (for DTOs)  | Immutability conflicts with some JPA/Hibernate requirements            |
| Immutables               | Less adoption, steeper learning curve than Lombok                      |

## Consequences

**Positive**
- Entities and DTOs are concise; business logic is not buried in boilerplate.
- MapStruct mappers are fully type-checked at compile time – mapping errors surface before runtime.
- No runtime reflection overhead for either library.

**Negative / Trade-offs**
- IDE plugins (IntelliJ Lombok plugin) must be installed for correct code navigation and compilation within the IDE.
- Processor order in `maven-compiler-plugin` is important; incorrect order causes `Cannot find symbol` errors.
- Lombok's `@Data` on JPA entities can generate problematic `equals`/`hashCode` based on all fields including mutable ones; care is needed.

