# gRPC vs Kafka — How to Choose

> **Audience**: Backend developers, architects  
> **Date**: 2026-03-07  
> **Context**: SE Platform — choosing the right communication backbone per use-case

---

## TL;DR Decision Matrix

| Question | gRPC | Kafka |
|----------|------|-------|
| Do you need a **synchronous response** right now? | ✅ Yes | ❌ No |
| Is the caller **waiting** for the result? | ✅ Yes | ❌ No |
| Do you need **replay** of past events? | ❌ No | ✅ Yes |
| Do you need **fan-out** to multiple consumers? | ❌ Hard | ✅ Yes |
| Is the payload **large bulk data**? | ✅ Server-streaming | ✅ With chunking |
| Do you need **back-pressure** at the transport layer? | ✅ HTTP/2 built-in | ⚠️ Consumer-lag only |
| Are services in the **same cluster / mesh**? | ✅ Ideal | ⚠️ Overkill |
| Do you need **event sourcing / audit log**? | ❌ No | ✅ Yes |
| Do you need **exactly-once delivery**? | ❌ No | ✅ Transactions |
| Is the consumer **temporarily offline**? | ❌ Request fails | ✅ Message waits |

---

## Core Conceptual Difference

```
gRPC  =  a FUNCTION CALL over the network
          caller blocks (or streams) and waits for callee to respond
          tight coupling by design — both sides must be up

Kafka =  a SHARED LEDGER of immutable events
          producer writes and forgets
          consumer reads at its own pace, any time
          loose coupling by design — producer and consumer never talk directly
```

---

## 1. gRPC — When and Why

### What it is

gRPC is an RPC framework built on **HTTP/2 + Protocol Buffers**.  
It has four call patterns:

| Pattern | Description | Use-case |
|---------|-------------|----------|
| **Unary** | 1 request → 1 response | REST replacement, trigger |
| **Server-streaming** | 1 request → N response chunks | Bulk data download, live feed |
| **Client-streaming** | N request chunks → 1 response | Bulk upload, file ingest |
| **Bidirectional streaming** | N request chunks ↔ N response chunks | Chat, real-time telemetry |

### Choose gRPC when

- You need a **synchronous answer** — the caller must wait for the result
- You are calling **one specific service** — point-to-point
- You need **strong schema contracts** enforced at compile time
- Payload is large and streaming avoids heap buffering (server-streaming)
- Services run in the **same Kubernetes cluster** with mTLS service mesh
- You need **fine-grained error codes** (gRPC status codes vs HTTP 4xx/5xx)
- Latency matters — Protobuf is ~10× smaller than JSON, HTTP/2 multiplexing

### gRPC anti-patterns — do NOT use when

- You need **fan-out** (one event → many consumers) — use Kafka
- The consumer may be **offline** — request will fail with UNAVAILABLE
- You need **event replay** — gRPC has no message store
- You need **audit trail** — gRPC leaves no durable record
- Crossing **public internet / browser** — use REST or WebSocket instead

---

### gRPC Code Examples (Java / Spring Boot)

#### Proto definition

```protobuf
// calculation_service.proto
syntax = "proto3";

package com.company.calc;

option java_package         = "com.company.calc.grpc";
option java_outer_classname = "CalculationProto";

service CalculationService {

    // Unary — trigger job, get job_id back immediately
    rpc TriggerCalculation (TriggerRequest) returns (TriggerResponse);

    // Server-streaming — stream 1M rows back in chunks of 5000
    rpc StreamResults (ResultsRequest) returns (stream CalculationChunk);
}

message TriggerRequest {
    string system_id = 1;
    string job_id    = 2;
}

message TriggerResponse {
    string job_id = 1;
    string status = 2;  // ACCEPTED | REJECTED
}

message ResultsRequest {
    string job_id = 1;
}

message CalculationChunk {
    repeated Row rows       = 1;
    int32        chunk_index = 2;
    int64        total_rows  = 3;
}

message Row {
    string          id     = 1;
    repeated double values = 2;  // 50 columns
}
```

#### gRPC Server — Calculation Engine (CE)

```java
@GrpcService
public class CalculationServiceImpl
        extends CalculationServiceGrpc.CalculationServiceImplBase {

    private final SourceDataRepository sourceRepo;

    // ── Unary ──────────────────────────────────────────────────────────────
    @Override
    public void triggerCalculation(TriggerRequest request,
                                   StreamObserver<TriggerResponse> responseObserver) {
        log.info("Received trigger for system={} job={}", 
                 request.getSystemId(), request.getJobId());

        // Kick off async processing — return accepted immediately
        calculationExecutor.submit(() -> runCalculation(request.getJobId()));

        responseObserver.onNext(TriggerResponse.newBuilder()
                .setJobId(request.getJobId())
                .setStatus("ACCEPTED")
                .build());
        responseObserver.onCompleted();
    }

    // ── Server-Streaming ───────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public void streamResults(ResultsRequest request,
                              StreamObserver<CalculationChunk> responseObserver) {

        int chunkIndex  = 0;
        int chunkSize   = 5_000;
        List<Row> batch = new ArrayList<>(chunkSize);

        // JDBC streaming cursor — setFetchSize(5000) set in repository
        try (Stream<SourceRow> rows = sourceRepo.streamByJobId(request.getJobId())) {

            Iterator<SourceRow> it = rows.iterator();
            while (it.hasNext()) {
                batch.add(toProtoRow(it.next()));

                if (batch.size() == chunkSize) {
                    sendChunk(responseObserver, batch, chunkIndex++);
                    batch.clear();
                }
            }
            // flush remaining rows
            if (!batch.isEmpty()) {
                sendChunk(responseObserver, batch, chunkIndex++);
            }

        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription(e.getMessage()).asException());
            return;
        }

        responseObserver.onCompleted();
        log.info("Streamed {} chunks for job={}", chunkIndex, request.getJobId());
    }

    private void sendChunk(StreamObserver<CalculationChunk> observer,
                           List<Row> rows, int index) {
        observer.onNext(CalculationChunk.newBuilder()
                .addAllRows(rows)
                .setChunkIndex(index)
                .build());
    }
}
```

#### gRPC Client — SE Backend (BE)

```java
@Service
@RequiredArgsConstructor
public class CalculationEngineClient {

    @GrpcClient("calculation-engine")   // resolves via k8s service DNS
    private CalculationServiceGrpc.CalculationServiceStub asyncStub;

    private final SimResultsRepository simResultsRepo;
    private final ProgressEmitter      progressEmitter;

    public void triggerAndPersist(String systemId, String jobId) {

        TriggerRequest request = TriggerRequest.newBuilder()
                .setSystemId(systemId)
                .setJobId(jobId)
                .build();

        // Server-streaming — BE persists each chunk as it arrives
        asyncStub.streamResults(
                ResultsRequest.newBuilder().setJobId(jobId).build(),
                new StreamObserver<>() {

                    @Override
                    public void onNext(CalculationChunk chunk) {
                        // Runs in gRPC thread — chunk N persisted while CE sends chunk N+1
                        simResultsRepo.batchInsert(jobId, chunk.getRowsList());

                        progressEmitter.emit(jobId,
                                chunk.getChunkIndex(),
                                chunk.getTotalRows());
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Stream failed for job={}: {}", jobId, t.getMessage());
                        progressEmitter.emitError(jobId, t.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Stream complete for job={}", jobId);
                        progressEmitter.emitDone(jobId);
                    }
                });
    }
}
```

#### gRPC application.yml (Spring Boot)

```yaml
grpc:
  client:
    calculation-engine:
      address: "discovery:///calculation-engine"   # k8s DNS
      negotiation-type: tls
      keep-alive-time: 60s
      keep-alive-timeout: 10s
      max-inbound-message-size: 10MB

  server:
    port: 9090
    security:
      certificate-chain: classpath:certs/server.crt
      private-key: classpath:certs/server.key
```

---

## 2. Kafka — When and Why

### What it is

Kafka is a **distributed, durable, ordered, partitioned event log**.  
Producers append events to **topics**. Consumers read from topics at their own pace.  
Events are retained for a configurable retention period (hours, days, forever).

```
Producer ──► Topic [partition 0] ──► Consumer Group A (all partitions)
                  [partition 1] ──► Consumer Group B (all partitions)
                  [partition 2] ──► Consumer Group C (all partitions)

One producer write → N independent consumer groups each get their own copy
```

### Choose Kafka when

- The consumer may be **temporarily offline** — event waits in the log
- You need **fan-out** — multiple services react to the same event
- You need **event replay** — re-read history from offset 0
- You need **audit trail** — Kafka topic is the immutable source of truth
- You need **exactly-once** end-to-end semantics (Kafka transactions)
- You need **decoupling** — producer does not know or care about consumers
- Event **ordering** within a partition is required
- You are building **event sourcing** or **CQRS** patterns

### Kafka anti-patterns — do NOT use when

- You need an **immediate synchronous answer** — use gRPC
- The message is a **tiny 100-byte RPC trigger** between two online services
- You need **request/reply** correlation — awkward in Kafka (reply topics + correlation IDs)
- The team has no Kafka operations experience — operational overhead is real
- You need **sub-millisecond latency** — Kafka batching adds latency by design

---

### Kafka Code Examples (Java / Spring Boot + Spring Kafka)

#### Producer — SE Backend publishes smeagol-completed event

```java
@Service
@RequiredArgsConstructor
public class SmeagolEventPublisher {

    private final KafkaTemplate<String, SmeagolCompletedEvent> kafkaTemplate;

    public void publishCompleted(String jobId, long rowsPersisted, long durationMs) {

        SmeagolCompletedEvent event = SmeagolCompletedEvent.builder()
                .jobId(jobId)
                .rowsPersisted(rowsPersisted)
                .durationMs(durationMs)
                .completedAt(Instant.now())
                .build();

        // key = jobId ensures all events for one job go to the same partition
        // = guaranteed ordering per job
        kafkaTemplate.send("smeagol.completed", jobId, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish completed event for job={}", jobId, ex);
                    } else {
                        log.info("Published completed event for job={} offset={}",
                                jobId,
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
```

#### Consumer — Analytics Service reacts to smeagol-completed

```java
@Component
@RequiredArgsConstructor
public class SmeagolCompletedConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(
            topics          = "smeagol.completed",
            groupId         = "analytics-service",
            containerFactory = "batchKafkaListenerContainerFactory"  // batch mode
    )
    public void onSmeagolCompleted(
            @Payload List<SmeagolCompletedEvent> events,
            Acknowledgment ack) {

        log.info("Processing batch of {} completed events", events.size());

        try {
            analyticsService.processBatch(events);
            ack.acknowledge();   // manual commit — only after successful processing
        } catch (Exception e) {
            log.error("Batch processing failed — will retry", e);
            // do NOT ack — Kafka will redeliver the batch
            throw e;
        }
    }
}
```

#### Consumer — Notification Service also reacts (fan-out, independent group)

```java
@Component
public class SmeagolNotificationConsumer {

    @KafkaListener(
            topics  = "smeagol.completed",
            groupId = "notification-service"    // different group = independent offset
    )
    public void onCompleted(SmeagolCompletedEvent event) {
        notificationService.sendEmail(event.getJobId(), event.getRowsPersisted());
    }
}
```

#### Kafka configuration — application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: kafka-broker-1:9092,kafka-broker-2:9092,kafka-broker-3:9092

    producer:
      key-serializer:   org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all                  # wait for all ISR replicas to acknowledge
      retries: 3
      properties:
        enable.idempotence: true            # exactly-once producer semantics
        max.in.flight.requests.per.connection: 1

    consumer:
      key-deserializer:   org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      group-id: analytics-service
      auto-offset-reset: earliest           # replay from beginning if new group
      enable-auto-commit: false             # manual ack — never lose a message
      max-poll-records: 500                 # batch size per poll

    listener:
      ack-mode: MANUAL_IMMEDIATE
      concurrency: 3                        # 3 consumer threads = 3 partitions
      type: BATCH
```

#### Topic configuration (via AdminClient bean)

```java
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic smeagolCompletedTopic() {
        return TopicBuilder.name("smeagol.completed")
                .partitions(12)               // 12 partitions = 12 parallel consumers max
                .replicas(3)                  // 3 replicas = survive 2 broker failures
                .config(TopicConfig.RETENTION_MS_CONFIG,
                        String.valueOf(Duration.ofDays(30).toMillis()))
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
                .build();
    }
}
```

---

## 3. Side-by-Side Comparison

### Architecture pattern

```
gRPC  (synchronous RPC):

  SE Backend ──gRPC──► Calculation Engine
       ▲                      │
       └──── response ────────┘
  BE blocks or streams until CE responds.
  If CE is down → BE gets UNAVAILABLE immediately.


Kafka  (async event):

  SE Backend ──► [smeagol.completed topic] ──► Analytics Service
                                               ──► Notification Service
                                               ──► Audit Service
  BE writes and moves on. Consumers read when ready.
  If a consumer is down → it catches up from its last offset when it restarts.
```

### Performance characteristics

| Metric | gRPC | Kafka |
|--------|------|-------|
| **Latency (p50)** | < 1 ms (LAN) | 5–50 ms (batching) |
| **Latency (p99)** | 5–20 ms | 50–200 ms |
| **Throughput** | Millions req/s (server-streaming) | Millions events/s per partition |
| **Payload size** | Any (streaming for large) | Default max 1 MB per message |
| **Fan-out** | Manual (call N services) | Built-in (N consumer groups) |
| **Replay** | None | From any offset, any time |
| **Durability** | None (in-memory RPC) | Configurable (days/forever) |
| **Ordering** | Per-stream | Per-partition |

### Delivery guarantees

| Guarantee | gRPC | Kafka |
|-----------|------|-------|
| **At-most-once** | Default (no retry) | `acks=0` |
| **At-least-once** | With retry interceptor | `acks=all` + manual commit |
| **Exactly-once** | ❌ Not supported | ✅ Idempotent producer + transactions |

### Operational complexity

| Aspect | gRPC | Kafka |
|--------|------|-------|
| **Infrastructure** | None beyond services | ZooKeeper/KRaft + brokers + Schema Registry |
| **Observability** | gRPC interceptors, Micrometer | Kafka metrics, consumer lag, Burrow |
| **Schema evolution** | Proto field numbers (safe) | Confluent Schema Registry + Avro/Protobuf |
| **Debugging** | grpcurl, grpc-gateway | kafka-console-consumer, kafdrop, akhq |
| **Local dev** | Two services + Docker | Kafka + Zookeeper + topic setup |

---

## 4. Hybrid Pattern — gRPC + Kafka Together

The most powerful production pattern uses **both**:

```
SE Frontend
    │
    │  REST/JSON  ~1 KB trigger
    ▼
SE Backend ──────────────────────────────────────────────────────────────┐
    │                                                                     │
    │  gRPC server-streaming                                              │  Kafka publish
    │  (synchronous, large data, back-pressure)                          │  (async notification)
    ▼                                                                     ▼
Calculation Engine                                              [smeagol.completed]
    │                                                                     │
    │  JDBC cursor                                                        ├──► Analytics Service
    ▼                                                                     ├──► Notification Service
Source Database                                                           └──► Audit / SIEM Service
    │
    │  1M rows streamed
    ▼
SE Database (TimescaleDB)
```

### Rule: gRPC for the DATA PATH, Kafka for EVENTS

```
Data path  (large, synchronous, caller waits):
    CE → BE: gRPC server-streaming
    Result:  1M rows persisted in ~18 seconds, zero heap buffering

Event path (small, async, multiple consumers):
    BE → Kafka: SmeagolCompletedEvent { job_id, rows_persisted, duration_ms }
    Consumers:  Analytics, Notification, Audit — each independently, any time
```

#### Publishing after gRPC stream completes

```java
// In CalculationEngineClient.onCompleted():
@Override
public void onCompleted() {
    log.info("Stream complete for job={}", jobId);

    // 1. notify FE via SSE (synchronous push)
    progressEmitter.emitDone(jobId);

    // 2. publish to Kafka (async fan-out to downstream services)
    eventPublisher.publishCompleted(
            jobId,
            totalRowsPersisted.get(),
            System.currentTimeMillis() - startTime
    );
}
```

---

## 5. Decision Flowchart

```
START: I need service A to communicate with service B
                │
                ▼
    Does A need an answer right now?
    ├── YES ──► Is the payload large (over 5 MB or streaming)?
    │               ├── YES ──► gRPC server-streaming
    │               └── NO  ──► gRPC unary  OR  REST/JSON
    │
    └── NO  ──► Will multiple services react to this event?
                    ├── YES ──► Kafka (fan-out)
                    └── NO  ──► Is durability / replay needed?
                                    ├── YES ──► Kafka
                                    └── NO  ──► Consider gRPC async (fire-and-forget stub)
                                                OR Kafka for consistency
```

---

## 6. Quick Reference — Spring Dependencies

```xml
<!-- pom.xml -->

<!-- gRPC -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>3.1.0.RELEASE</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.62.2</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.62.2</version>
</dependency>

<!-- Protobuf compiler plugin -->
<plugin>
    <groupId>com.github.os72</groupId>
    <artifactId>protoc-jar-maven-plugin</artifactId>
    <version>3.11.4</version>
    <executions>
        <execution>
            <goals><goal>run</goal></goals>
            <configuration>
                <protocVersion>3.25.3</protocVersion>
                <includeStdTypes>true</includeStdTypes>
            </configuration>
        </execution>
    </executions>
</plugin>

<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Avro / Schema Registry (optional — use with Confluent) -->
<dependency>
    <groupId>io.confluent</groupId>
    <artifactId>kafka-avro-serializer</artifactId>
    <version>7.6.0</version>
</dependency>
```

---

## 7. Anti-Patterns Summary

| Anti-pattern | Problem | Fix |
|---|---|---|
| Using Kafka for synchronous request/reply | Needs reply topic + correlation ID, high latency, complex | Use gRPC unary |
| Using gRPC to fan-out to 5 services | N stub calls, tight coupling, cascading failures | Use Kafka |
| REST for 1M-row response | 120 MB heap spike, no back-pressure, BE waits | Use gRPC server-streaming |
| Kafka message over 1 MB default limit | Broker rejects, consumer crash | Chunk data, store in S3, send reference |
| Single large `@Transactional` for 1M inserts | PG lock for full duration, OOM on rollback | Batch + per-chunk transaction |
| `enable-auto-commit: true` in Kafka consumer | Message lost if consumer crashes after poll, before process | Set `enable-auto-commit: false`, manual ack |
| No `setFetchSize` on JDBC cursor | Driver loads entire ResultSet into heap | Always set `stmt.setFetchSize(5000)` |
| gRPC without mTLS in k8s | Services trust all callers inside cluster | Enforce mTLS via Istio / Linkerd service mesh |

---

## Related Documents

| Document | Description |
|----------|-------------|
| [`se-large-data-transfer-md.mmd`](se-large-data-transfer-md.mmd) | Sequence diagram — full protocol pipeline |
| [`ADR-009`](../../docs/adr/ADR-009-http-traffic-large-data-transfer.md) | Decision record — protocol choices |
| [`se-large-data-transfer-README.md`](../../docs/mermaid/se-large-data-transfer-README.md) | Protocol design guide |

