# Spring Boot Integration Checklist — Protobuf

Goal: repurpose an existing JSON REST controller to serve and accept Protocol Buffers (gRPC or HTTP/1.1).

## 1. Dependencies & Build
- Add the protobuf plugin + gRPC stubs in `pom.xml`:
  ```xml
  <plugin>
    <groupId>org.xolstice.maven.plugins</groupId>
    <artifactId>protobuf-maven-plugin</artifactId>
    <version>0.6.1</version>
    <configuration>
      <protocArtifact>com.google.protobuf:protoc:3.25.3:exe:${os.detected.classifier}</protocArtifact>
    </configuration>
  </plugin>
  ```
- Include `com.google.protobuf:protobuf-java`, `io.grpc:grpc-stub`, `io.grpc:grpc-netty-shaded`.

## 2. Controller Changes (JSON → Protobuf)
| Area | JSON Default | Protobuf swap |
| --- | --- | --- |
| `@RequestBody` | `MyDto` from Jackson | `MyProtoOuterClass.MyMessage` generated class |
| `@RestController` | produces `application/json` | add `produces = "application/x-protobuf"` and `consumes = "application/x-protobuf"` |
| Message conversion | Jackson auto-registered | register `ProtobufHttpMessageConverter` bean |

```java
@Bean
public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
    return new ProtobufHttpMessageConverter();
}
```

## 3. Sample Endpoint
```java
@PostMapping(value = "/api/orders", consumes = "application/x-protobuf", produces = "application/x-protobuf")
public OrderResponse create(@RequestBody OrderRequest protoRequest) {
    Order domain = mapper.fromProto(protoRequest);
    Order saved = service.create(domain);
    return mapper.toProto(saved);
}
```

## 4. Client Compatibility
- HTTP clients: set `Content-Type` and `Accept` headers to `application/x-protobuf`.
- gRPC route: expose the same `.proto` service via `@GrpcService` if low latency or streaming is needed.

## 5. Observability & Tooling
- Add `protoc --decode` snippets to docs for debugging payloads.
- Capture metrics per content type via `MeterFilter` to ensure Proto traffic is visible.

