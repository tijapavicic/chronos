# Spring Boot Integration Checklist — XML

Scenario: legacy partners require XML while the service currently exposes JSON.

## 1. Dependencies
- Add `spring-boot-starter-web` (already includes Jackson XML) + `com.fasterxml.jackson.dataformat:jackson-dataformat-xml` or JAXB:
  ```xml
  <dependency>
      <groupId>com.fasterxml.jackson.dataformat</groupId>
      <artifactId>jackson-dataformat-xml</artifactId>
  </dependency>
  ```
- For JAXB, include `jakarta.xml.bind:jakarta.xml.bind-api` + `org.glassfish.jaxb:jaxb-runtime`.

## 2. MVC Configuration
```java
@Configuration
public class XmlConfig {
    @Bean
    MappingJackson2XmlHttpMessageConverter xmlConverter(ObjectMapper baseMapper) {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(baseMapper.getRegisteredModuleIds());
        return new MappingJackson2XmlHttpMessageConverter(xmlMapper);
    }
}
```
- Register converter order before JSON if most clients are XML; otherwise leave default.

## 3. Controller Adjustments
- Extend existing controller method with `consumes/produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE}`.
- Annotate DTOs with `@JacksonXmlProperty(localName = "...")` or JAXB `@XmlElement` to keep contract stable.

```java
@PostMapping(value = "/payments", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
public PaymentResponse create(@RequestBody PaymentRequest request) {
    return mapper.toResponse(service.create(mapper.toDomain(request)));
}
```

## 4. Testing & Tooling
- Add MockMvc tests verifying both JSON and XML payloads deserialize identically.
- Provide XSD (optional) in docs; validate incoming XML via `SchemaFactory` if partner contracts demand it.

