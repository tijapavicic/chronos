Copilot instructions — Spring Boot 3.x + Java 17 (best practices)
============================================

Purpose
-------
A short, practical guide for GitHub Copilot (or any assistant) when making suggestions for this Spring Boot 3.x / Java 17 project. Use these as the source of truth for coding style, conventions, testing, and prompts.

Quick checklist (what I expect from Copilot suggestions)
-------------------------------------------------------
- Use `jakarta.*` imports and Spring Boot 3.x idioms.
- Prefer constructor injection and small, well-named beans.
- Use `Jackson2ObjectMapperBuilder` to configure ObjectMapper; register JavaTimeModule and disable WRITE_DATES_AS_TIMESTAMPS.
- Create custom unchecked exceptions inheriting a single `ApplicationException` base and map them centrally in `@ControllerAdvice`.
- Use MapStruct for DTO <-> entity mapping and configure the annotation processor in Maven.
- Add unit and slice tests first (JUnit5 + Mockito + MockMvc); add integration tests with `@SpringBootTest` only when necessary.

Project assumptions
-------------------
- Java 17 is used; use modern constructs (records, var) sparingly and where they improve clarity.
- Spring Boot 3.x (Spring Framework 6): use `jakarta.*` packages (e.g., `jakarta.validation`).
- Maven is the build tool and the project uses Lombok and MapStruct annotation processors.
- Follow the existing package layout: `controller`, `service`, `dto`, `model`, `exception`, `config`, `mapper`, `runtime`.

Style & idioms
--------------
- Constructor injection for beans; prefer `@RequiredArgsConstructor` rather than field injection.
- Keep controllers thin: accept/validate DTOs and delegate to service layer.
- Services encapsulate business logic and throw domain-specific unchecked exceptions.
- DTOs: immutable where practical, use Lombok `@Builder` + `@Jacksonized` for JSON binding.
- Use `ResponseEntity<T>` for explicit status and headers. Use `@ResponseStatus` only for simple cases.
- Avoid wildcard imports and use explicit imports.
- Keep methods small and focused; split into helper methods for readability.

Configuration & ObjectMapper
----------------------------
- Register `Jackson2ObjectMapperBuilder` bean in `@Configuration`:
  - `.modules(new JavaTimeModule())`
  - `.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)`
  - `.serializationInclusion(JsonInclude.Include.NON_NULL)`
- Prefer consistent property naming (snake_case vs camelCase) — follow repo conventions.
- For custom serializers/deserializers, register them via `Jackson2ObjectMapperBuilder`.

Exception handling
------------------
- Create a base `ApplicationException extends RuntimeException`.
- Add specific exceptions: `ResourceNotFoundException`, `BadRequestException`, `ConflictException`, `DatabaseTimeoutException` (with optional Duration), `UnauthorizedException`, `ForbiddenException`.
- Centralize HTTP mapping in a `@ControllerAdvice` (or `GlobalControllerAdvice`) using a map/ordered lookup to determine status codes.
- Include `X-Correlation-Id` (header) in the ErrorResponse and in logs for traceability.

Mapping and DTOs
----------------
- Use MapStruct for entity/DTO conversions. Add `mapstruct-processor` to `maven-compiler-plugin` `annotationProcessorPaths`.
- Prefer `@Mapper(componentModel = "spring")` so mappers can be injected into services.
- In mappers, handle null-safe mappings and map nested objects carefully.

Security & actuator
-------------------
- Tests that hit secured endpoints should set credentials explicitly (e.g., `TestRestTemplate.withBasicAuth(...)`).
- Expose only necessary actuator endpoints in production; tests can override properties to expose `health`/`info` only.

Testing
-------
- Unit tests: JUnit 5 + Mockito. Keep them fast and deterministic.
- Controller tests (advice and mapping): use `MockMvcBuilders.standaloneSetup()` with a Jackson converter created from the application's ObjectMapper config.
- Integration tests: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` for full-stack tests.
- Parameterized tests: use `@ParameterizedTest` with `@MethodSource` for object-rich cases.

Performance & resilience
----------------------
- Stream large collections and use pagination (Pageable) for endpoints returning many items.
- Surface DB timeouts as `DatabaseTimeoutException` and map to `408` vs `503` based on business rules/timeout length.
- Prefer `@Transactional` boundaries in service layer and keep transactions short.

Logging & observability
-----------------------
- Include correlation id in logs. Use `MDC` if helpful.
- Keep structured logs (JSON) for production and include key fields (correlationId, userId, requestPath).
- Add metrics via Micrometer (timers for DB calls, counters for errors).

Common pitfalls to avoid
------------------------
- Mixing `jakarta` and `javax` imports — always prefer `jakarta` for Spring Boot 3.x.
- Returning raw entities in controllers (always use DTOs).
- Silent swallowing of exceptions — log and map to appropriate ErrorResponse.
- Long-running operations on controller threads — use async or background processing for heavy tasks.

Sample Copilot prompts
----------------------
- "Create a MapStruct mapper `FacilityMapper` to convert `Facility` <-> `FacilityDTO` and include null-safe mappings."
- "Add `DatabaseTimeoutException` with an optional `Duration` timeout and map it in GlobalControllerAdvice to return 408 if timeout < 3 minutes, otherwise 503."
- "Write a MockMvc standalone test asserting that `ResourceNotFoundException` from the service yields 404 and ErrorResponse includes `X-Correlation-Id`."

Detailed template prompt (copy-paste)
------------------------------------
"In `com.example.chronos` (Spring Boot 3, Java 17):
1) Add `DatabaseTimeoutException` that accepts `Duration timeout`.
2) Update `GlobalControllerAdvice` to map: if timeout < 3 minutes -> 408, else -> 503.
3) Add two MockMvc tests that simulate throwing this exception and assert status and ErrorResponse fields.
Use `MockMvcBuilders.standaloneSetup()` and the project's Jackson converter."

Where to put this file
----------------------
- Repo root as `copilot-instructions.md` or in `.github/` for repo-level reference. You can paste contents into Copilot's personal instruction UI (GitHub settings) for better in-editor suggestions.

If unsure
---------
- Ask one short question to clarify API shape, naming, or status code preference. When in doubt follow existing repo conventions (error codes, `X-Correlation-Id`, `swagger.enabled`).

Done — the new `copilot-instructions.md` has been created at the repository root.
