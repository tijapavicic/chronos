# ADR-009 – HTTP Traffic Alternatives for Large Data Transfers

| Property    | Value      |
|-------------|------------|
| **Status**  | Accepted   |
| **Date**    | 2026-03-06 |
| **Authors** | <> Team    |

---

## Context

Certain use cases require transferring significant volumes of data between
clients and the backend — for example: bulk smeagol imports, facility snapshot exports, etc..

Open questions include:

- Which **HTTP transfer mechanism** to use depending on data volume.
- Which **data format** is appropriate at different scales.
- The trade-offs between simplicity, performance, and interoperability.

---

## Data Volume Reference Table

| Tier        | Row Count (approx.) | Typical Size (JSON) | Typical Size (XML) |
|-------------|--------------------:|--------------------:|--------------------|
| **Tiny**    | < 1 000             | < 100 KB            | < 200 KB           |
| **Small**   | 1 000 – 50 000      | 100 KB – 5 MB       | 200 KB – 10 MB     |
| **Medium**  | 50 000 – 500 000    | 5 MB – 50 MB        | 10 MB – 100 MB     |
| **Large**   | 500 000 – 1 000 000 | 50 MB – 100 MB      | 100 MB – 200 MB    |
| **X-Large** | > 1 000 000         | > 100 MB            | > 200 MB           |

> **Rule of thumb:** XML is typically **1.5 – 2.5×** larger than equivalent JSON due to
> verbose tag markup. For the same 1 million-row dataset, JSON may occupy ~120 MB while
> XML occupies ~240–300 MB.

---

## HTTP Transfer Mechanisms

### 1. Standard REST / Single Request–Response

**Best for:** Tiny → Small (< 5 MB, < 50 000 rows)

- Simple `POST` / `GET` with a JSON body or response.
- The entire payload is buffered in memory on both client and server.
- Default Spring Boot request size limit is `1 MB`; must be raised via
  `spring.servlet.multipart.max-request-size` / `server.tomcat.max-http-form-post-size` for
  larger payloads.
- **Timeout risk** grows rapidly above ~5 MB on slow networks.

```
GET  /api/facilities?page=0&size=1000   → 200 OK  (paged JSON)
POST /api/smeagols/import            → 201 Created
```

---

### 2. Pagination / Cursor-Based Streaming

**Best for:** Small → Medium (50 000 – 500 000 rows)

- Server exposes `page` + `size` (offset) **or** a `cursor` (keyset) parameter.
- Client issues multiple sequential requests, each returning a manageable chunk.
- Keeps individual response sizes under ~1 MB.
- Spring Data provides `Pageable` out of the box.
- Cursor-based pagination is preferred for large datasets because it avoids
  expensive `OFFSET` queries.

```
GET /api/jobs?cursor=<last-seen-id>&size=500
```

---

### 3. HTTP Chunked Transfer Encoding (Streaming Response)

**Best for:** Medium → Large (5 MB – 100 MB, up to 1 000 000 rows)

- The server writes rows to the HTTP response incrementally using
  `StreamingResponseBody` (Spring MVC) or `Flux<T>` (Spring WebFlux).
- The `Transfer-Encoding: chunked` header tells the client that the response
  length is unknown; data arrives in frames.
- Memory footprint on the server stays constant regardless of total payload size.
- Ideal for exporting CSV, NDJSON, or Protobuf streams.

```java
// Spring MVC example
@GetMapping(value = "/export", produces = MediaType.APPLICATION_NDJSON_VALUE)
public StreamingResponseBody exportJobs() {
    return outputStream -> {
        jobRepository.streamAll().forEach(job -> {
            outputStream.write(toNdJson(job));
        });
    };
}
```

---

### 4. Server-Sent Events (SSE)

**Best for:** Real-time / incremental push of medium datasets (up to ~500 000 rows)

- Unidirectional server → client push over a persistent HTTP/1.1 or HTTP/2 connection.
- Client reconnects automatically on failure.
- Suitable for live smeagol progress feeds or incremental report delivery.
- Not designed for bulk binary or columnar data.

```
GET /api/smeagols/{id}/progress   →   text/event-stream
```

---

### 5. Multipart File Upload / Download

**Best for:** Large file exchange (> 10 MB) where the payload is a well-defined file

- Client sends `multipart/form-data`; server streams parts directly to disk/object-store.
- Avoids loading the entire payload in JVM heap.
- Suitable for importing pre-generated CSV / Parquet / Avro files.
- Combine with pre-signed S3/object-store URLs for files > 100 MB to bypass the
  application tier entirely.

---

### 6. gRPC / Bi-directional Streaming

**Best for:** X-Large (> 1 000 000 rows), service-to-service, binary payloads

- Uses HTTP/2 multiplexing + Protobuf serialisation.
- Native bi-directional streaming — client and server can both stream concurrently.
- ~3–10× smaller payloads than equivalent JSON; ~5–10× faster parse time.
- Requires a gRPC gateway or Envoy proxy if browser clients need access.
- Spring Boot supports gRPC via `grpc-spring-boot-starter`.

---

### 7. WebSocket

**Best for:** Bidirectional, high-frequency, real-time data (e.g., live dashboards, tick feeds)

- Full-duplex persistent connection; low overhead per message.
- Less suited to bulk sequential exports; better for interactive/live scenarios.
- Complexity: manual reconnect logic, load-balancer sticky sessions required.

---

## Protocol Comparison Matrix

| Mechanism                  | Max Practical Size | Direction       | Buffering      | Complexity | Spring Support        |
|----------------------------|--------------------|-----------------|----------------|------------|-----------------------|
| REST single request        | < 5 MB             | Client → Server | Full (memory)  | Low        | `@RestController`     |
| Pagination / Cursor        | Unlimited (paged)  | Server → Client | Per-page       | Low–Medium | `Pageable`, Keyset    |
| Chunked streaming          | Unlimited          | Server → Client | None (stream)  | Medium     | `StreamingResponseBody`, `Flux` |
| SSE                        | ~500 MB practical  | Server → Client | None (stream)  | Medium     | `SseEmitter`, `Flux`  |
| Multipart upload           | Limited by storage | Client → Server | Partial        | Low–Medium | `MultipartFile`       |
| gRPC streaming             | Unlimited          | Bidirectional   | None (stream)  | High       | `grpc-spring-boot-starter` |
| WebSocket                  | Unlimited          | Bidirectional   | None (stream)  | High       | `spring-websocket`    |

---

## Data Format Recommendation by Volume

### Under 1 000 000 rows — JSON

**Recommendation: JSON**

- Human-readable; native to REST/HTTP ecosystems.
- Universally supported by browsers, Postman, curl.
- Spring Boot serialises/deserialises via Jackson with zero configuration.
- Acceptable overhead at this scale.

| Format | Pros                                     | Cons                                  |
|--------|------------------------------------------|---------------------------------------|
| JSON   | Native REST support, tooling everywhere  | Verbose for repeated keys, text-only  |
| XML    | Schema validation (XSD), namespace-aware | 1.5–2.5× larger, slower parse, verbose |

**Verdict (< 1 M rows):** Prefer **JSON**. XML is acceptable only when a strict
contract/schema (XSD) or legacy integration mandates it.

---

### Over 1 000 000 rows — Binary Columnar Formats

**Recommendation: Apache Parquet or Apache Avro**

At millions of rows, text-based formats (JSON, XML, CSV) become impractical:

| Problem with text formats at scale         | Impact                                       |
|--------------------------------------------|----------------------------------------------|
| Large payload size                         | Network saturation, higher egress cost       |
| Full payload must be parsed before use     | High peak memory, long time-to-first-row     |
| No schema enforcement at transport level   | Data quality issues surface late             |
| Poor compressibility of repeated keys      | JSON/XML compress poorly column-by-column    |

#### Recommended Formats

| Format            | Size vs JSON | Streaming | Schema | Best Use Case                          |
|-------------------|:------------:|:---------:|:------:|----------------------------------------|
| **Parquet**       | ~10–20×      | Columnar  | Yes    | Analytics, bulk exports, data lakes    |
| **Avro**          | ~5–10×       | Row-based | Yes    | Kafka pipelines, row-oriented streaming |
| **Protocol Buffers (Protobuf)** | ~5–10× | Row-based | Yes | gRPC service-to-service              |
| **MessagePack**   | ~2–3×        | Row-based | No     | Drop-in JSON replacement, simple APIs  |
| **CSV (gzipped)** | ~3–5×        | Row-based | No     | Simple bulk ETL, broad tool support    |

> **Parquet** is the top recommendation for analytical/export workloads over 1 M rows.
> **Avro** is preferred when data flows through Kafka or requires schema evolution.
> **Protobuf** is preferred for live service-to-service gRPC streaming.

#### Example — Serving Parquet from Spring Boot

```java
@GetMapping(value = "/export/parquet", produces = "application/octet-stream")
public ResponseEntity<StreamingResponseBody> exportParquet() {
    StreamingResponseBody body = outputStream -> {
        try (ParquetWriter<GenericRecord> writer = buildParquetWriter(outputStream)) {
            jobRepository.streamAll().forEach(job -> writer.write(toAvroRecord(job)));
        }
    };
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"jobs.parquet\"")
        .body(body);
}
```

---

## Decision

| Data Volume              | Preferred HTTP Mechanism              | Preferred Data Format          |
|--------------------------|---------------------------------------|--------------------------------|
| < 5 MB / < 50 K rows     | REST single request                   | JSON                           |
| 5 MB – 50 MB / < 500 K   | Chunked streaming or Pagination       | JSON (NDJSON for streaming)    |
| 50 MB – 100 MB / < 1 M   | Chunked streaming                     | JSON (NDJSON) or gzipped CSV   |
| > 100 MB / > 1 M rows    | gRPC streaming or Multipart + S3      | Parquet, Avro, or Protobuf     |
| Real-time push            | SSE or WebSocket                      | JSON or MessagePack            |

---

## Alternatives Considered

| Approach                        | Reason Not Preferred at Large Scale                                 |
|---------------------------------|---------------------------------------------------------------------|
| Plain JSON REST for > 1 M rows  | Memory pressure, network saturation, slow parse                     |
| XML for any bulk transfer       | 1.5–2.5× overhead vs JSON; extremely slow SAX/DOM parsing at scale  |
| CSV (uncompressed) for > 1 M    | Larger than binary formats; no schema; fragile with delimiter escaping |
| GraphQL subscriptions           | Not natively suited for bulk data export pipelines                  |

---

## Consequences

**Positive**
- Clear protocol-per-tier guidance reduces guesswork during feature development.
- Using streaming mechanisms prevents OOM errors on bulk export/import endpoints.
- Binary columnar formats (Parquet/Avro) reduce network cost by 5–20× at scale.
- Schema-enforced formats (Avro/Protobuf) catch data contract violations early.

**Negative / Trade-offs**
- Binary formats require additional libraries (`parquet-avro`, `kafka-avro-serializer`).
- gRPC requires HTTP/2 infrastructure and a proto contract; not browser-native.
- Streaming responses make error handling more complex (error can occur mid-stream
  after headers are already sent).
- Developer tooling for Parquet/Avro is less ubiquitous than JSON viewers/editors.

---

## References

- [RFC 7230 – HTTP/1.1 Transfer Codings (Chunked)](https://datatracker.ietf.org/doc/html/rfc7230#section-4.1)
- [Spring `StreamingResponseBody` docs](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/StreamingResponseBody.html)
- [Apache Parquet format](https://parquet.apache.org/docs/)
- [Apache Avro specification](https://avro.apache.org/docs/current/spec.html)
- [gRPC on Spring Boot (`grpc-spring-boot-starter`)](https://github.com/grpc-ecosystem/grpc-spring)
- [NDJSON (Newline-Delimited JSON)](https://ndjson.org/)

