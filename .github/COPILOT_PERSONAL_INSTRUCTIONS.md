GitHub Copilot — Personal Instructions for this project

Purpose
-------
These are personal instructions intended for GitHub Copilot (or any code assistant) when working on this Spring Boot 3.x / Java 17 project (Chronos). Put these in your Copilot personal settings or keep as a repo reference so Copilot suggestions better align with the project.

Project context
---------------
- Java: 17 (use language features conservatively; prefer readability over clever constructs).
- Framework: Spring Boot 3.x (Spring Framework 6 / Jakarta packages — imports use `jakarta.*`).
- Build: Maven (pom.xml present). Use existing project dependencies and annotation processors (Lombok, MapStruct).
- Testing: JUnit 5 (JUnit Jupiter), `MockMvc` for controller slice tests, `TestRestTemplate` for integration where used, and Mockito for mocking.
- Conventions: package root `com.example.chronos`. Follow existing package layout (controller, service, dto, model, exception, config, runtime, mapper).

Coding style & guidelines
-------------------------
- Use clear, descriptive names for classes, methods, variables; prefer `facilityService` to `svc` etc.
- Keep methods short (< 60 lines) and single-responsibility.
- Use Spring Boot idiomatic patterns:
  - Constructor injection for beans (@RequiredArgsConstructor / explicit constructor).
  - `@ConfigurationProperties` for grouping related properties, `@Bean` for explicit beans.
  - `@ControllerAdvice` for centralized exception handling and return consistent `ErrorResponse`.
- Use `jakarta` packages for annotations (e.g., `jakarta.validation.*`).
- Prefer `ResponseEntity<T>` for controllers to set explicit HTTP status and headers.

Null handling & Immutability
---------------------------
- Prefer immutable DTOs where practical (final fields, builders). Use Lombok's `@Builder` and `@Jacksonized` if needed for deserialization.
- Validate inbound JSON using `@Valid` and field constraints; reject invalid input with clear ErrorResponse.

Serialization / ObjectMapper
----------------------------
- Use `Jackson2ObjectMapperBuilder` via Spring to configure a single ObjectMapper bean.
- Register Java Time module and disable WRITE_DATES_AS_TIMESTAMPS. Include NON_NULL inclusion.

Exceptions & HTTP mapping
-------------------------
- Create custom unchecked exceptions extending `ApplicationException` (e.g., `BadRequestException`, `ResourceNotFoundException`, `DatabaseTimeoutException`).
- Map exceptions to HTTP codes centrally in `@ControllerAdvice`. Keep mapping explicit and test it with MockMvc.
- Use correlation-id header `X-Correlation-Id` and include it in ErrorResponse.

MapStruct & DTO mapping
-----------------------
- Use MapStruct for object-to-object mapping. Ensure `mapstruct-processor` is present as annotationProcessor in pom.xml.
- Keep mappers interface-based, `@Mapper(componentModel = "spring")` where Spring injection is desired.

Testing
-------
- Unit tests: JUnit5 + Mockito. Keep tests small and deterministic.
- Controller tests: use `MockMvcBuilders.standaloneSetup()` for advice/controller slice tests; provide the Jackson converter configured similarly to app ObjectMapper.
- Integration tests: use `@SpringBootTest(webEnvironment = RANDOM_PORT)` and `TestRestTemplate` for end-to-end flows requiring the servlet container.
- Parameterized tests: use `@ParameterizedTest` + `@MethodSource` or `@ValueSource` where appropriate.

Security
--------
- When suggesting endpoints in tests, be explicit about credentials if security is enabled (`TestRestTemplate.withBasicAuth(...)`) or set test properties to disable/enable security.
- Keep any auto-generated passwords or dev credentials out of committed code.

Performance & Resilience
-----------------------
- Prefer streaming and pagination for potentially large datasets.
- Timeouts: surface database/query timeouts as `DatabaseTimeoutException` with optional duration — map to `408` vs `503` based on configured threshold.

Formatting & imports
--------------------
- Use the project's existing import/order conventions. Keep imports minimal and avoid wildcard imports.
- Use 4-space indentation (match repo style).

Commit & PR message hints
-------------------------
- Commit summary: short imperative line (e.g. "Add DatabaseTimeoutException and map to 503")
- Body: explain why, mention files changed, and list simple reproduction/test commands.

Example prompts to give Copilot (short)
-------------------------------------
- "Create a MapStruct mapper to map Facility to FacilityDTO and back, use builder pattern and handle nulls." 
- "Add a controller advice mapping DatabaseTimeoutException to 503 Service Unavailable when timeout >= 3 minutes, 408 otherwise." 
- "Write a MockMvc standalone test that asserts a ResourceNotFoundException returns 404 with ErrorResponse containing correlationId." 

Example detailed prompt (copy-paste)
-----------------------------------
"In this Spring Boot 3 / Java 17 project (package `com.example.chronos`):
- Add a custom unchecked exception `DatabaseTimeoutException` that accepts an optional `Duration` timeout.
- Update the global controller advice to map this exception to HTTP 408 if timeout < 3 minutes, otherwise 503.
- Add two MockMvc tests verifying both cases. Use `MockMvcBuilders.standaloneSetup()` and include our JSON message converter from the app ObjectMapper."

Do / Don't
----------
- Do: Prefer explicit, small PRs. Suggest tests for every behavioral change.
- Do: Use clear exception types and map them in a centralized place.
- Don't: Add new top-level frameworks or large dependencies without a brief rationale.

Where to look in this repo
--------------------------
- `src/main/java/com/example/chronos/controller` — controllers & advices
- `src/main/java/com/example/chronos/exception` — exception types
- `src/main/java/com/example/chronos/config` — config (ObjectMapper, Security)
- `src/test/java/com/example/chronos` — tests and examples

If you are unsure
-----------------
If a prompt is ambiguous about API shape, preferred status codes, or property names, prefer to ask a short clarifying question. 
If you cannot ask, follow current repo conventions (e.g. `swagger.enabled`, `X-Correlation-Id`, `ErrorResponse` shape). 

-- End of personal Copilot instructions --
