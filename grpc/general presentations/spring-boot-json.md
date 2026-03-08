# Spring Boot Integration Checklist — JSON (Baseline)

Purpose: document the default setup so comparisons with Protobuf/Avro/XML/Parquet are concrete.

## 1. Dependencies
- `spring-boot-starter-web` already ships with Jackson (`jackson-databind` + `jackson-dataformat-*`).
- For Java 17+ records or Kotlin data classes, ensure the relevant Jackson module is on the classpath (`jackson-module-parameter-names`, `jackson-module-kotlin`).

## 2. Controller Patterns
```java
@RestController
@RequestMapping("/api")
class OrderController {
    @PostMapping(value = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDto create(@Valid @RequestBody OrderDto body) {
        return mapper.toDto(service.create(mapper.toDomain(body)));
    }
}
```

## 3. Content Negotiation
- Add `?format=json` or `Accept: application/json` to force JSON when other formats exist.
- Register a `ContentNegotiationCustomizer` to prioritize JSON for browsers.

## 4. Tips When Introducing Other Formats
- Keep the DTOs immutable; map to/from other payload classes (Proto/Avro) via dedicated mappers.
- Centralize validation: JSON controllers should share service layer validation so Protobuf/Avro routes stay consistent.
- Document the canonical JSON schema (OpenAPI) to guide compatibility when other encodings are added.

