# 🚀 Large-Data Transfer — Protocol Design Guide

> **Diagram source**: [`se-large-data-transfer.mmd`](se-large-data-transfer-grpc.mmd)
> **ADR reference**: [`ADR-009 – HTTP Traffic Alternatives for Large Data Transfers`](../../docs/adr/ADR-009-http-traffic-large-data-transfer.md)
> **Date**: 2026-03-07 | **Authors**: Chronos Team

---

## 📐 Architecture Overview

This document covers the **end-to-end protocol design** for the 5-component pipeline that
transfers and persists **1 000 000 rows × 50 columns** of smeagol data:

```
SE Frontend  ──REST/JSON──▶  SE Backend  ──gRPC unary──▶  Calculation Engine
                 ▲                 ▲                               │
          REST response       SSE progress                  JDBC cursor
                               events                             │
                                  │                               ▼
                             SE Database  ◀──gRPC streaming──  Source Database
                           (JDBC batch insert)           (server-side cursor)
```

---

## 📦 Payload Size Reference

The raw dataset: **1 000 000 rows × 50 columns**

| Format          | Estimated Wire Size | Notes                                               |
|-----------------|--------------------:|-----------------------------------------------------|
| **JSON**        | ~120–150 MB         | Text, repeated keys per row, human-readable         |
| **XML**         | ~250–300 MB         | ~2× JSON — opening/closing tags per field           |
| **CSV (plain)** | ~80–100 MB          | Compact but no schema, delimiter fragility          |
| **CSV (gzip)**  | ~20–30 MB           | Good compression; still no schema enforcement       |
| **Avro**        | ~15–25 MB           | Row-binary + embedded schema, Snappy/Deflate        |
| **Protobuf**    | ~15–25 MB           | Row-binary, schema via `.proto`, native to gRPC     |
| **Parquet**     | ~8–15 MB            | Columnar, best compression ratio, analytics-native  |

> **Key takeaway**: XML is never appropriate for bulk transfers at this scale —
> it costs **2–3× more bandwidth** than JSON and is **5–10× slower** to parse (DOM).
> For service-to-service bulk streaming, **Protobuf via gRPC** is the correct choice.
> For file-based exports and analytics pipelines, **Parquet** is the correct choice.

---

## 🔄 Phase-by-Phase Protocol Decisions

### Phase 1 — SE Frontend → SE Backend
**Protocol: REST / HTTP + JSON**

| Attribute     | Value                                                        |
|---------------|--------------------------------------------------------------|
| Direction     | Client → Server                                              |
| Payload       | `{ system_id, smeagol_params }` ~1 KB                    |
| HTTP method   | `POST /api/smeagols`                                      |
| Content-Type  | `application/json`                                           |
| Justification | Payload is a tiny control message. REST/JSON is universally supported by browsers, Postman, curl. Zero meaningful overhead at < 5 MB. No value in adding gRPC complexity for a 1 KB message. |

---

### Phase 2 — SE Backend → Calculation Engine
**Protocol: gRPC / HTTP/2 + Protobuf (unary request)**

| Attribute     | Value                                                        |
|---------------|--------------------------------------------------------------|
| Direction     | Client → Server (unary)                                      |
| Payload       | `TriggerCalculation { string system_id }` ~100 B            |
| Transport     | HTTP/2 (gRPC)                                                |
| Serialisation | Protocol Buffers (Protobuf)                                  |
| Justification | gRPC is chosen here primarily because the **response** (Phase 4) requires server-streaming. A single gRPC channel handles both the trigger request and the streamed response. Protobuf enforces the data contract at compile time via the `.proto` file. HTTP/2 multiplexing also lets SSE progress events (Phase 6) co-exist on the same TCP connection without head-of-line blocking. |

**.proto definition (excerpt)**:
```protobuf
service CalculationService {
  // unary request, server-streaming response
  rpc TriggerCalculation (CalculationRequest)
      returns (stream CalculationResponse);
}

message CalculationRequest {
  string system_id = 1;
}

message CalculationResponse {
  repeated Row rows    = 1;
  int32 chunk_index    = 2;
  int64 total_rows     = 3;
}

message Row {
  string id              = 1;
  repeated double values = 2;  // 50 columns
}
```

---

### Phase 3 — Calculation Engine → Source Database
**Protocol: JDBC with server-side streaming cursor**

| Attribute     | Value                                                         |
|---------------|---------------------------------------------------------------|
| Direction     | CE reads from PostgreSQL                                      |
| Fetch size    | 5 000 rows per JDBC batch                                     |
| Memory        | O(fetch_size) — constant regardless of 1 M total rows        |
| Justification | Without a cursor, `SELECT *` loads all 1 M rows into the JDBC driver heap at once (~120–150 MB). A server-side cursor (PostgreSQL `autocommit=false` + `setFetchSize`) fetches 5 K rows at a time — JVM heap stays constant throughout. Spring Data JPA supports this via `Stream<T>` + `@QueryHints`. |

**Spring JPA streaming example**:
```java
@Query("SELECT r FROM SourceRow r WHERE r.systemId = :systemId")
@QueryHints(@QueryHint(name = HINT_FETCH_SIZE, value = "5000"))
@Transactional(readOnly = true)
Stream<SourceRow> streamBySystemId(@Param("systemId") String systemId);
```

---

### Phase 4 — Calculation Engine → SE Backend
**Protocol: gRPC Server-Streaming + Protobuf**

| Attribute     | Value                                                         |
|---------------|---------------------------------------------------------------|
| Direction     | Server → Client streaming (CE → BE)                          |
| Chunk size    | 5 000 rows / message · ~75 KB / chunk                        |
| Total chunks  | ~200 messages                                                 |
| Wire size     | ~15–25 MB compressed (Protobuf + Snappy/gzip)                |
| Back-pressure | HTTP/2 flow-control window (automatic)                       |
| Justification | gRPC server-streaming is optimal because: **(1)** rows are pipelined — BE persists chunk N while CE still fetches chunk N+1 from the DB; **(2)** HTTP/2 flow control prevents CE from overwhelming BE (consumer-driven pace); **(3)** the shared `.proto` schema prevents silent data contract drift; **(4)** neither side holds the full 1 M rows in memory at any point. |

**Why NOT REST/JSON for this hop?**

| Problem with REST/JSON at 1 M rows    | Impact                                         |
|---------------------------------------|------------------------------------------------|
| Full response buffered in memory      | ~120–150 MB heap spike in both CE and BE       |
| No back-pressure mechanism            | CE can OOM before BE reads first byte          |
| Jackson text serialisation overhead   | Adds 2–5 s extra latency per transfer          |
| BE cannot start persisting until done | Phase 5 cannot begin until Phase 4 completes  |

---

### Phase 5 — SE Backend → SE Database
**Protocol: JDBC Batch Insert (Spring `batchUpdate`)**

| Attribute        | Value                                                         |
|------------------|---------------------------------------------------------------|
| Batch size       | 5 000 rows per `executeBatch()` call                         |
| Total batches    | 200                                                           |
| Transaction scope| 1 `@Transactional` per chunk (5 000 rows)                    |
| Throughput       | ~50 000–100 000 rows/sec (PostgreSQL, local NVMe)            |
| Justification    | One JDBC round-trip per 5 000 rows vs 1 M individual `INSERT` statements reduces round-trips by 99.98%. Transacting per-chunk (not the full 1 M rows) bounds rollback scope: a failure in chunk 150 only rolls back rows 745 001–750 000 — not the entire dataset. A single 1 M-row transaction would hold a PostgreSQL lock for the entire transfer duration and risk OOM on rollback. |

**Spring `batchUpdate` example**:
```java
jdbcTemplate.batchUpdate(
    "INSERT INTO smeagol_results (id, system_id, col1, ..., col50) VALUES (?, ?, ..., ?)",
    rows,
    5000,
    (ps, row) -> {
        ps.setString(1, row.getId());
        ps.setString(2, systemId);
        // ... bind all 50 columns
    }
);
```

> **TimescaleDB note**: Partition the SE Database hypertable by `created_at` with a
> 1-day chunk interval. Batch inserts into the active (most-recent) chunk are
> append-only, avoiding B-tree index contention and enabling chunk-level compression.

---

### Phase 6 — SE Backend → SE Frontend (progress reporting)
**Protocol: Server-Sent Events (SSE)**

| Attribute     | Value                                                         |
|---------------|---------------------------------------------------------------|
| Direction     | Server → Client (unidirectional push)                        |
| Transport     | HTTP/1.1 or HTTP/2, `text/event-stream`                      |
| Event size    | ~100–200 B per event (JSON)                                  |
| Event rate    | 1 event per gRPC chunk ACK'd (~200 events total)             |
| Justification | SSE delivers server-push over a plain HTTP connection with zero extra infrastructure. The browser `EventSource` API handles reconnection automatically if the connection drops mid-transfer. Unlike WebSocket, SSE is unidirectional (server → client only), which is exactly what progress reporting requires. No need for a bidirectional channel here. |

**Spring `SseEmitter` example**:
```java
@GetMapping(value = "/api/smeagols/{id}/progress",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamProgress(@PathVariable String id) {
    SseEmitter emitter = new SseEmitter(300_000L); // 5-min timeout
    progressRegistry.register(id, emitter);
    return emitter;
}

// Called by the persistence layer after each 5K-row batch is committed:
emitter.send(SseEmitter.event()
    .name("progress")
    .data(Map.of("pct", 45, "rows_done", 450_000)));

// On completion:
emitter.send(SseEmitter.event()
    .name("done")
    .data(Map.of("job_id", jobId, "rows_persisted", 1_000_000)));
emitter.complete();
```

---

### Phase 7 — Optional Export (SE Backend → SE Frontend)
**Protocol: HTTP Chunked Transfer-Encoding**

| Attribute     | Value                                                          |
|---------------|----------------------------------------------------------------|
| Direction     | Server → Client streaming download                            |
| Transport     | HTTP/1.1 `Transfer-Encoding: chunked`                         |
| Format option A | `application/x-ndjson` — browser, curl, Postman             |
| Format option B | `application/octet-stream` (Parquet) — analytics pipelines  |
| Spring API    | `StreamingResponseBody`                                        |
| Justification | Chunked HTTP streaming writes rows directly to the response output stream — no full-payload heap allocation. NDJSON (Newline-Delimited JSON) is one row per line, trivially parsed by `jq`, Postman, or any stream reader. Parquet is 5–20× smaller than JSON and the native format for Spark, Pandas, DuckDB, and BigQuery. |

**Spring `StreamingResponseBody` example**:
```java
@GetMapping(value = "/api/smeagols/{id}/export", produces = "application/x-ndjson")
public ResponseEntity<StreamingResponseBody> exportNdJson(@PathVariable String id) {
    StreamingResponseBody body = outputStream -> {
        try (Stream<ResultRow> rows = resultRepository.streamByJobId(id);
             OutputStreamWriter writer =
                 new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            rows.forEach(row -> {
                writer.write(objectMapper.writeValueAsString(row));
                writer.write('\n');
                writer.flush(); // flush each line to avoid buffering
            });
        }
    };
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"result.ndjson\"")
        .body(body);
}
```

---

## 📊 Complete Protocol Summary Table

| Phase | From                | To                  | Protocol                  | Format         | ~Size        | Key Reason                               |
|------:|---------------------|---------------------|---------------------------|----------------|-------------:|------------------------------------------|
| 1     | SE Frontend         | SE Backend          | REST / HTTP               | JSON           | ~1 KB        | Tiny control message, universal tooling  |
| 2     | SE Backend          | Calculation Engine  | gRPC / HTTP/2             | Protobuf       | ~100 B       | Typed schema, enables server-streaming   |
| 3     | Calculation Engine  | Source DB           | JDBC streaming cursor     | Binary rows    | — (DB local) | Constant-memory streaming, 5 K fetch     |
| 4     | Calculation Engine  | SE Backend          | gRPC server-streaming     | Protobuf       | ~15–25 MB    | Zero buffering, back-pressure, pipeline  |
| 5     | SE Backend          | SE Database         | JDBC batch insert         | Binary rows    | — (DB local) | 200 round-trips vs 1 M INSERTs           |
| 6     | SE Backend          | SE Frontend         | SSE `text/event-stream`   | JSON events    | ~200 × 100 B | Push progress, auto-reconnect            |
| 7     | SE Backend          | SE Frontend         | HTTP chunked streaming    | NDJSON/Parquet | ~10–150 MB   | No heap buffering on export              |

---

## ⚠️ Anti-Patterns — What to Avoid at This Scale

| Anti-pattern                            | Why It Is Dangerous                                               |
|-----------------------------------------|-------------------------------------------------------------------|
| REST/JSON for Phase 4 (bulk response)   | ~120–150 MB heap spike; no back-pressure; CE/BE OOM risk          |
| XML for any bulk transfer               | 2–3× size overhead; DOM parser requires full load in memory       |
| Single `@Transactional` for 1 M rows   | Failure rolls back all rows; lock held for entire duration        |
| `List<Row>` collected in controller     | Full dataset in heap before first byte serialised                 |
| Polling for progress (1-sec interval)   | 200 HTTP requests vs 200 SSE pushes — wasteful and slow           |
| `SELECT *` without `setFetchSize`       | JDBC loads entire ResultSet into driver heap (~150 MB)            |
| Uncompressed Parquet over the wire      | Parquet with Snappy is 5–20× smaller — always enable compression  |

---

## 🔗 Related Resources

| Resource                                                              | Description                                    |
|-----------------------------------------------------------------------|------------------------------------------------|
| [ADR-009](../../docs/adr/ADR-009-http-traffic-large-data-transfer.md)         | Full protocol decision record with alternatives |
| [se-large-data-transfer.mmd](se-large-data-transfer-grpc.mmd)             | Mermaid sequence diagram (this doc's visual)   |
| [se-sequence-detailed.mmd](../../docs/mermaid/se-sequence-detailed.mmd)                 | Full SE platform smeagol sequence           |
| [se-flow-lvl-2.mmd](../../docs/mermaid/se-flow-lvl-2.mmd)                               | Level-2 component architecture                 |
| [RFC 7230 – Chunked Transfer Coding](https://datatracker.ietf.org/doc/html/rfc7230#section-4.1) | HTTP spec |
| [gRPC Core Concepts – Server Streaming](https://grpc.io/docs/what-is-grpc/core-concepts/#server-streaming-rpc) | gRPC docs |
| [Apache Parquet Format](https://parquet.apache.org/docs/)             | Columnar storage format                        |
| [NDJSON Specification](https://ndjson.org/)                           | Newline-Delimited JSON                         |
| [Spring StreamingResponseBody](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/StreamingResponseBody.html) | Spring API |
| [Spring SseEmitter](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/SseEmitter.html) | Spring SSE API |

