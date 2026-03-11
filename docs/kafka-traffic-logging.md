# Kafka Traffic Logging — Implementation Guide

## 📋 Overview

This document describes the **HTTP traffic logging infrastructure** implemented for the Chronos application. All requests to `/simulation/**` endpoints are captured, serialized to **Apache Avro binary format**, and published to a **Kafka topic** for asynchronous audit logging and analytics.

---

## 🎯 What Was Implemented

### 1. **Avro Schema** (`src/main/avro/TrafficLogEvent.avsc`)

Defines the structure of traffic log events:

| Field | Type | Description |
|-------|------|-------------|
| `correlationId` | string | Request correlation ID (from `X-Correlation-Id` header or auto-generated) |
| `timestampMs` | long (timestamp-millis) | Epoch milliseconds when request was received |
| `method` | string | HTTP method (GET, POST, etc.) |
| `path` | string | Request URI path |
| `queryString` | string (nullable) | URL query parameters |
| `statusCode` | int | HTTP response status code |
| `durationMs` | long | Request processing time in milliseconds |
| `clientIp` | string | Client IP (respects `X-Forwarded-For`) |
| `userAgent` | string (nullable) | Browser/client User-Agent |
| `requestBodySizeBytes` | long | Request payload size |
| `responseBodySizeBytes` | long | Response payload size |
| `serviceName` | string | Value of `spring.application.name` |

---

### 2. **Maven Dependencies & Plugin** (`pom.xml`)

Added:
- `spring-kafka` — Spring Boot Kafka integration
- `avro:1.11.3` — Apache Avro runtime
- `avro-maven-plugin` — Code generation from `.avsc` schema

The plugin runs during `generate-sources` phase and produces `TrafficLogEvent.java` in `target/generated-sources/avro/`.

---

### 3. **Kafka Configuration** (`application.properties`)

```properties
# Kafka Producer
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.ByteArraySerializer
spring.kafka.producer.retries=3
spring.kafka.producer.properties.enable.idempotence=true
spring.kafka.producer.properties.max.block.ms=3000

# Kafka Admin
spring.kafka.admin.fail-fast=false

# Topic Name
chronos.kafka.topic.traffic-log=simulation.traffic.log
```

---

### 4. **Kafka Topic Bean** (`kafka/KafkaTopicConfig.java`)

Auto-creates the `simulation.traffic.log` topic on startup:
- **3 partitions** (allows 3 parallel consumers)
- **1 replica** (dev/local setup)
- **7-day retention** (604800000 ms)

---

### 5. **Traffic Log Producer** (`kafka/TrafficLogProducer.java`)

- Serializes `TrafficLogEvent` to Avro binary format
- Publishes to Kafka using correlation ID as the message key (ensures ordering per request)
- Fire-and-forget semantics — failures are logged but never block HTTP threads

---

### 6. **Traffic Logging Filter** (`web/SimulationTrafficLoggingFilter.java`)

`OncePerRequestFilter` that:
- Intercepts **all requests** to `/simulation/**`
- Wraps request/response with `ContentCachingRequestWrapper` / `ContentCachingResponseWrapper` to capture body sizes
- Collects metadata (path, method, status, duration, IP, headers)
- Publishes `TrafficLogEvent` to Kafka after the response is sent
- Never degrades API availability — exceptions are caught and logged

**Order:** Runs after `CorrelationIdFilter` (order 10) so correlation ID is available.

---

### 7. **Docker Compose Stack** (`docker-compose.yml`)

Provides a **single-node Kafka cluster** with:

| Service | Description | Port |
|---------|-------------|------|
| **kafka** | Confluent Kafka (KRaft mode, no ZooKeeper) | 9092 (host), 29092 (containers) |
| **kafka-init** | One-shot container that creates the topic | — |
| **kafka-ui** | Provectus Kafka UI web dashboard | [http://localhost:8090](http://localhost:8090) |

**Start:** `docker compose up -d`  
**Stop:** `docker compose down`  
**Destroy:** `docker compose down -v` (removes persisted data)

---

## 🚀 How to Use

### Step 1: Start Kafka

```bash
cd /Users/copor/IdeaProjects/JavaProjects/chronos
docker compose up -d
```

Wait ~10 seconds for Kafka to become healthy. Verify topic creation:

```bash
docker logs chronos-kafka-init
```

You should see:
```
Created topic simulation.traffic.log.
Topic: simulation.traffic.log   PartitionCount: 3       ReplicationFactor: 1
```

---

### Step 2: Build & Run the Spring Boot App

```bash
mvn clean package -Dmaven.test.skip=true
mvn spring-boot:run -Dmaven.test.skip=true
```

Wait until you see:
```
Started ChronosApplication in X.XXX seconds
```

---

### Step 3: Generate Traffic

Hit the simulation endpoint:

```bash
curl http://localhost:8080/simulation/ping
```

Or send multiple requests:

```bash
for i in {1..5}; do
  curl -s -o /dev/null -w "HTTP %{http_code}\n" http://localhost:8080/simulation/ping
done
```

---

### Step 4: Verify Messages in Kafka

#### Option A: Using Kafka Console Consumer (raw binary)

```bash
docker exec chronos-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic simulation.traffic.log \
  --from-beginning \
  --max-messages 5
```

You'll see binary Avro data (not human-readable).

#### Option B: Using Kafka UI (visual)

1. Open [http://localhost:8090](http://localhost:8090)
2. Navigate to **Topics** → `simulation.traffic.log`
3. Click **Messages** tab
4. View message metadata (key, partition, offset, timestamp)
5. Message content is Avro binary — not human-readable without a deserializer

---

## 🔍 Decoding Avro Messages

To decode the Avro binary messages, you need an Avro consumer. A sample consumer class `TrafficLogConsumer.java` was created but is **disabled by default** (to avoid log noise).

### Enable the Consumer

Uncomment the `@ConditionalOnProperty` line in `TrafficLogConsumer.java`:

```java
@ConditionalOnProperty(name = "chronos.kafka.consumer.traffic-log.enabled", havingValue = "true")
```

Then add to `application.properties`:

```properties
chronos.kafka.consumer.traffic-log.enabled=true
```

Restart the app. The consumer will log decoded messages like:

```
═══════════════════════════════════════════════════════════════════════════
📊 Traffic Log Event [partition=1, offset=42]
   Correlation ID     : 7f3d8b2e-9c4a-4d1f-b5e8-a3f9c2d1e6b7
   Timestamp          : 2026-03-11T13:45:23.123Z
   Method             : GET
   Path               : /simulation/ping
   Query String       : null
   Status Code        : 200
   Duration (ms)      : 12
   Client IP          : 127.0.0.1
   User-Agent         : curl/7.64.1
   Request Body Size  : 0 bytes
   Response Body Size : 0 bytes
   Service Name       : chronos
═══════════════════════════════════════════════════════════════════════════
```

---

## 🎨 Architecture Diagram

```
┌─────────────────┐
│  HTTP Client    │
└────────┬────────┘
         │ GET /simulation/ping
         ▼
┌─────────────────────────────────────────────────────────────┐
│  Spring Boot (Chronos)                                      │
│                                                              │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  CorrelationIdFilter (Order 0)                        │ │
│  │    → Extracts/generates correlation ID                │ │
│  └────────────────────┬──────────────────────────────────┘ │
│                       │                                     │
│  ┌────────────────────▼──────────────────────────────────┐ │
│  │  SimulationTrafficLoggingFilter (Order 10)           │ │
│  │    → Wraps request/response                          │ │
│  │    → Calls controller                                │ │
│  │    → Captures metadata (path, status, duration, IP)  │ │
│  │    → Serializes to Avro binary                       │ │
│  │    → Publishes to Kafka (async)                      │ │
│  └────────────────────┬──────────────────────────────────┘ │
│                       │                                     │
│  ┌────────────────────▼──────────────────────────────────┐ │
│  │  SimulationController                                 │ │
│  │    @GetMapping("/simulation/ping")                    │ │
│  │    → Returns 200 OK                                   │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
         │
         │ Kafka Producer (async, fire-and-forget)
         ▼
┌─────────────────────────────────────────────┐
│  Kafka Broker (localhost:9092)              │
│                                              │
│  Topic: simulation.traffic.log               │
│    - 3 partitions                            │
│    - 7-day retention                         │
│    - Key: correlationId                      │
│    - Value: TrafficLogEvent (Avro binary)    │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
         ┌─────────────────────┐
         │  Consumers          │
         │  - Kafka UI         │
         │  - Analytics        │
         │  - Monitoring       │
         └─────────────────────┘
```

---

## 📊 Key Features

✅ **Non-blocking** — Kafka sends are async; failures never impact API latency  
✅ **Correlation tracking** — All events carry the same correlation ID for distributed tracing  
✅ **Compact binary format** — Avro is ~50% smaller than JSON  
✅ **Schema evolution** — Avro supports forward/backward compatible schema changes  
✅ **Partitioning by correlation ID** — All events for a single request land in the same partition (ordering guarantee)  
✅ **Docker-based Kafka** — Zero installation; `docker compose up` and you're ready  
✅ **Kafka UI included** — Browse topics, messages, and consumer groups at [http://localhost:8090](http://localhost:8090)

---

## 🛠️ Troubleshooting

### Problem: App fails to start with "Connection refused" to Kafka

**Solution:** Kafka is not running. Start it with:

```bash
docker compose up -d
docker logs chronos-kafka  # Check broker logs
```

### Problem: Messages are not appearing in Kafka

**Check 1:** Verify you're hitting the `/simulation/**` endpoint (not `/api/**` or others).

**Check 2:** Look for errors in the app logs:

```bash
grep -i "kafka\|traffic" logs/spring.log
```

**Check 3:** Check Kafka broker logs:

```bash
docker logs chronos-kafka
```

### Problem: Avro deserialization error in consumer

**Solution:** Ensure the consumer is using the **exact same schema** as the producer. Regenerate Avro sources:

```bash
mvn clean generate-sources
```

---

## 📦 Files Summary

| File | Purpose |
|------|---------|
| `src/main/avro/TrafficLogEvent.avsc` | Avro schema definition |
| `pom.xml` | Maven dependencies & avro-maven-plugin |
| `src/main/resources/application.properties` | Kafka config |
| `src/main/java/.../kafka/KafkaTopicConfig.java` | Topic bean |
| `src/main/java/.../kafka/TrafficLogProducer.java` | Kafka producer |
| `src/main/java/.../web/SimulationTrafficLoggingFilter.java` | Servlet filter |
| `src/main/java/.../kafka/TrafficLogConsumer.java` | Demo consumer (disabled by default) |
| `docker-compose.yml` | Kafka + Kafka UI stack |
| `docs/kafka-traffic-logging.md` | This file |

---

## 🎯 Next Steps

1. **Production readiness:**
   - Add Schema Registry (Confluent or Apicurio) for centralized schema management
   - Enable SSL/SASL authentication for Kafka
   - Increase replication factor to 3+ for durability
   - Monitor producer lag with Prometheus + Grafana

2. **Analytics:**
   - Stream traffic logs to Elasticsearch for full-text search
   - Build dashboards in Kibana or Grafana
   - Set up alerts for 5xx errors, slow requests (>1s), etc.

3. **Testing:**
   - Add integration tests that verify Kafka messages are published
   - Use `@EmbeddedKafka` for in-memory testing

---

## 📚 References

- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Apache Avro Documentation](https://avro.apache.org/docs/current/)
- [Kafka UI GitHub](https://github.com/provectus/kafka-ui)
- [Confluent Platform (Docker)](https://docs.confluent.io/platform/current/installation/docker/image-reference.html)

---

**Author:** GitHub Copilot  
**Date:** 2026-03-11  
**Version:** 1.0

