# SimulationController - Avro Kafka Logging Examples

## Overview

The `SimulationController` now includes endpoints that demonstrate **manual Avro event creation and Kafka publishing**. This shows how to:

1. Build `TrafficLogEvent` objects using the generated Avro class
2. Serialize them to Avro binary format
3. Publish to the `simulation.traffic.log` Kafka topic

---

## 🎯 Important Note

**The `SimulationTrafficLoggingFilter` already automatically logs ALL requests to `/simulation/**` endpoints.**

The endpoints below are for:
- **Demonstration purposes** — Show how to use Avro + Kafka directly
- **Custom business logic** — When you need to log additional application-specific events
- **Testing** — Verify the Kafka pipeline works end-to-end

---

## 📋 Endpoints

### 1. `GET /simulation/ping`

**Purpose:** Health check endpoint (no Kafka logging from controller itself)

**Example:**
```bash
curl http://localhost:8080/simulation/ping
```

**Response:** `200 OK` (empty body)

**Note:** This request WILL be logged by the `SimulationTrafficLoggingFilter` automatically.

---

### 2. `POST /simulation/log-event`

**Purpose:** Manually create and publish a custom TrafficLogEvent to Kafka

**Features:**
- ✅ Builds Avro `TrafficLogEvent` object
- ✅ Serializes to Avro binary format
- ✅ Publishes to Kafka topic `simulation.traffic.log`
- ✅ Returns correlation ID and confirmation

**Request:**
```bash
curl -X POST http://localhost:8080/simulation/log-event \
  -H "Content-Type: application/json" \
  -d '{"customField": "test data", "userId": 123}'
```

**Response:**
```json
{
  "status": "success",
  "message": "Event published to Kafka topic: simulation.traffic.log",
  "correlationId": "7f3d8b2e-9c4a-4d1f-b5e8-a3f9c2d1e6b7",
  "format": "Avro binary",
  "durationMs": "5"
}
```

**What Happens:**
1. Controller receives the request
2. Creates a `TrafficLogEvent` with:
   - Generated correlation ID
   - Current timestamp
   - HTTP method and path
   - Client IP and User-Agent
   - Request body size
3. `TrafficLogProducer` serializes it to Avro binary
4. Publishes to Kafka asynchronously
5. Returns success response

**Code Example:**
```java
TrafficLogEvent event = TrafficLogEvent.newBuilder()
    .setCorrelationId(correlationId)
    .setTimestampMs(Instant.ofEpochMilli(startTime))
    .setMethod(request.getMethod())
    .setPath(request.getRequestURI())
    .setQueryString(request.getQueryString())
    .setStatusCode(200)
    .setDurationMs(0L)
    .setClientIp(getClientIp(request))
    .setUserAgent(request.getHeader("User-Agent"))
    .setRequestBodySizeBytes(payload != null ? payload.toString().length() : 0L)
    .setResponseBodySizeBytes(0L)
    .setServiceName(serviceName)
    .build();

trafficLogProducer.publish(event);
```

---

### 3. `POST /simulation/run`

**Purpose:** Simulate business logic and log detailed metrics to Kafka

**Features:**
- ✅ Simulates a computation (100ms sleep)
- ✅ Logs start and completion
- ✅ Creates Avro event with actual duration
- ✅ Publishes to Kafka
- ✅ Returns simulation results

**Request:**
```bash
curl -X POST http://localhost:8080/simulation/run \
  -H "Content-Type: application/json" \
  -d '{
    "algorithm": "monte-carlo",
    "iterations": 10000,
    "precision": 0.001
  }'
```

**Response:**
```json
{
  "status": "completed",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "durationMs": 105,
  "params": {
    "algorithm": "monte-carlo",
    "iterations": 10000,
    "precision": 0.001
  },
  "message": "Results published to Kafka (Avro binary)",
  "topic": "simulation.traffic.log"
}
```

**What Happens:**
1. Controller receives simulation parameters
2. Logs start: `🚀 Starting simulation [correlationId=..., params=...]`
3. Executes simulated business logic (sleep 100ms)
4. Measures actual duration
5. Creates `TrafficLogEvent` with real metrics
6. Publishes to Kafka using Avro serialization
7. Logs completion: `✅ Simulation completed [correlationId=..., duration=105ms, published to Kafka]`
8. Returns results to client

**Use Case:**
This pattern is useful when you want to:
- Log business-specific metrics (not just HTTP traffic)
- Track long-running operations
- Publish custom events for analytics
- Include domain-specific metadata

---

## 🔍 Viewing Published Events

### Option 1: Kafka UI (Visual)

1. Open http://localhost:8090
2. Navigate to **Topics** → `simulation.traffic.log`
3. Click **Messages** tab
4. View all published events (Avro binary format)

### Option 2: Kafka Console Consumer (CLI)

```bash
docker exec chronos-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic simulation.traffic.log \
  --from-beginning \
  --max-messages 10
```

You'll see binary Avro data (not human-readable).

### Option 3: TrafficLogConsumer (Deserialized)

If you enable the `TrafficLogConsumer` (see `AVRO_EXPLANATION_COMPLETE.md`), you'll see decoded logs:

```
═══════════════════════════════════════════════════════════════════════════
📊 Traffic Log Event [partition=1, offset=42]
   Correlation ID     : 7f3d8b2e-9c4a-4d1f-b5e8-a3f9c2d1e6b7
   Timestamp          : 2026-03-11T14:20:34.567Z
   Method             : POST
   Path               : /simulation/log-event
   Status Code        : 200
   Duration (ms)      : 5
   ...
═══════════════════════════════════════════════════════════════════════════
```

---

## 🧪 Testing the Flow

### Complete Test Sequence

```bash
# 1. Start Kafka
docker compose up -d

# 2. Start Spring Boot app
mvn spring-boot:run -Dmaven.test.skip=true

# 3. Test ping endpoint (automatic filter logging)
curl http://localhost:8080/simulation/ping

# 4. Test manual event publishing
curl -X POST http://localhost:8080/simulation/log-event \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}'

# 5. Test simulation endpoint
curl -X POST http://localhost:8080/simulation/run \
  -H "Content-Type: application/json" \
  -d '{"algorithm": "test", "iterations": 100}'

# 6. Check Kafka messages
docker exec chronos-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic simulation.traffic.log \
  --from-beginning \
  --max-messages 3
```

Expected: **3 messages** in Kafka (1 from ping, 1 from log-event, 1 from run)

---

## 🎓 What You Learned

### How Avro Events Are Created

```java
// 1. Use the builder pattern (generated by avro-maven-plugin)
TrafficLogEvent event = TrafficLogEvent.newBuilder()
    .setCorrelationId("abc-123")           // String field
    .setTimestampMs(Instant.now())          // Instant (timestamp-millis)
    .setMethod("POST")                      // String field
    .setStatusCode(200)                     // int field
    .setDurationMs(105L)                    // long field
    .setQueryString(null)                   // nullable field
    .build();

// 2. Publish to Kafka (async)
trafficLogProducer.publish(event);
```

### Serialization Flow

```
TrafficLogEvent (Java object)
    ↓ TrafficLogProducer.publish()
SpecificDatumWriter + BinaryEncoder
    ↓ Uses TrafficLogEvent.SCHEMA$
byte[~200] (Avro binary)
    ↓ KafkaTemplate.send()
Kafka Topic: simulation.traffic.log
```

### Key Classes

| Class | Purpose |
|-------|---------|
| `TrafficLogEvent` | Generated Avro class (from `.avsc` schema) |
| `TrafficLogProducer` | Serializes and publishes to Kafka |
| `SimulationController` | Business logic + manual event creation |
| `SimulationTrafficLoggingFilter` | Automatic traffic logging for all `/simulation/**` |

---

## 💡 Best Practices

### When to Use Manual Logging

✅ **Use manual logging when:**
- You need to log business-specific events (e.g., "simulation completed")
- You want custom metadata not in HTTP traffic (e.g., algorithm type)
- You're tracking long-running operations
- You need application-level metrics

❌ **Don't use manual logging when:**
- HTTP traffic logging is enough (the filter handles this)
- You'd be duplicating what the filter already does

### Correlation ID Strategy

The controller generates a **new correlation ID** for each manual event. In production:

1. **Read from filter** if you want the same ID:
   ```java
   String correlationId = (String) request.getAttribute("correlationId");
   ```

2. **Generate new ID** for business events unrelated to HTTP requests

---

## 📊 Comparison: Filter vs Controller Logging

| Aspect | SimulationTrafficLoggingFilter | SimulationController |
|--------|--------------------------------|----------------------|
| **Trigger** | Automatic (every request) | Manual (you call it) |
| **Scope** | HTTP traffic only | Any business event |
| **Metadata** | Request/response details | Custom application data |
| **Use Case** | Audit logging, monitoring | Business logic tracking |
| **Overhead** | Transparent to controller | Controller must build event |

**Recommendation:** Use **both**:
- Filter handles automatic HTTP audit logs
- Controller handles custom business events

---

## 🚀 Next Steps

1. **Try the endpoints** — Run the curl commands above
2. **View in Kafka UI** — See the Avro binary messages
3. **Enable consumer** — Decode and view the events
4. **Customize schema** — Add fields to `TrafficLogEvent.avsc`
5. **Production ready** — Add Schema Registry for centralized schema management

---

**Documentation:** See `docs/kafka-traffic-logging.md` and `docs/avro-schema-complete-flow.md` for complete details.

