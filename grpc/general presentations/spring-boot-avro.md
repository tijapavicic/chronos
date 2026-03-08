# Spring Boot Integration Checklist — Avro

Focus: adapt an existing JSON REST pipeline to publish/consume Avro payloads (HTTP or Kafka).

## 1. Dependencies
- Add `org.apache.avro:avro`, `io.confluent:kafka-avro-serializer` (if Schema Registry), and optionally `org.springframework.kafka:spring-kafka`.
- Generate Java classes from `.avsc` using the Avro Maven plugin or `avro-maven-plugin`.

## 2. Message Conversion for REST
```java
@Bean
public HttpMessageConverters avroConverters() {
    return new HttpMessageConverters(new AvroHttpMessageConverter());
}
```
- Implement `AvroHttpMessageConverter` wrapping Avro binary/JSON encoders (or use community starter).
- Update controller annotations: `consumes = "application/avro"`, `produces = "application/avro"`.

## 3. Controller Delta
| Concern | JSON default | Avro variant |
| --- | --- | --- |
| DTO | Lombok POJO | Generated `SpecificRecord` class |
| Validation | `@Valid` works via bean validation | prefer schema-level defaults; use adapter if Bean Validation needed |
| Content negotiation | `application/json` | register `application/avro`, optionally `application/avro+binary` |

```java
@PostMapping(value = "/api/payments", consumes = "application/avro")
public PaymentConfirmation submit(@RequestBody PaymentRequest request) {
    service.handle(request);
    return buildConfirmation(request);
}
```

## 4. Kafka Usage
- Configure serializers:
  ```properties
  spring.kafka.producer.value-serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
  spring.kafka.properties.schema.registry.url=https://schema-registry:8081
  spring.kafka.properties.specific.avro.reader=true
  ```
- Use the same generated Avro classes in producers and consumers; no manual JSON mapping layer needed.

## 5. Migration Tips from JSON
- Keep JSON endpoint as compatibility fallback; add `/avro` suffix while clients migrate.
- Use `Accept` header to serve JSON vs Avro from the same controller method via `@RequestMapping(produces = {"application/json", "application/avro"})`.
- Document Schema Registry compatibility strategy (backward or full).

