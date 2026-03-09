# gRPC vs Kafka: How to Choose (Developer Guide)

## TL;DR

- Choose **gRPC** for synchronous request/response, low-latency APIs, and immediate success/failure semantics.
- Choose **Kafka** for asynchronous event streams, decoupled services, replayability, and high-throughput pipelines.
- Use **both** when you need command-style APIs plus event-driven distribution.

## Core Model

- **gRPC** = direct RPC call between services.
  - Caller waits for result (or timeout).
  - Tighter runtime coupling between caller and callee.
  - Strong contract via Protobuf.
- **Kafka** = append-only event log with producers and consumers.
  - Producer does not wait for business processing outcome.
  - Loose coupling and independent scaling.
  - Event contracts and schema evolution become critical.

## Decision Specification (What to Measure)

Use this checklist before implementation:

1. Interaction style
- Need immediate answer in same call path? -> gRPC
- Can process later? -> Kafka

2. Latency target
- p95 under `50-200ms` end-to-end for request path -> gRPC
- Seconds/minutes acceptable -> Kafka

3. Delivery and durability
- Best effort request with retries acceptable -> gRPC
- Must retain and replay events -> Kafka

4. Fan-out
- One caller to one service -> gRPC
- One producer to many independent consumers -> Kafka

5. Failure behavior
- Caller must fail fast if dependency unavailable -> gRPC
- Caller should continue and process downstream later -> Kafka

6. Throughput profile
- Moderate QPS, low payload per call -> gRPC
- Burst/batch streams, high sustained throughput -> Kafka

7. Ordering and exactly-once needs
- Per-request ordering not central -> gRPC
- Partition key ordering + idempotent consumers needed -> Kafka

8. Operational maturity
- Team comfortable with service-to-service RPC and load balancing -> gRPC
- Team can run broker clusters, partitioning, lag monitoring, schema governance -> Kafka

## Practical Rules

Choose **gRPC** when:
- User/API request must synchronously validate and return result.
- You need strict request/response semantics and low latency.
- You need bidi streaming RPC between known services.

Choose **Kafka** when:
- You need event history, replay, and auditability.
- Multiple consumers need same data at different speeds.
- You want producer/consumer deployment independence.

Choose **Hybrid (common in production)**:
- `Command via gRPC` to start work.
- `Events via Kafka` for progress, completion, and downstream integration.

## Scoring Matrix (Quick Spec)

Score each item from 1 to 5 and sum:

| Criterion | Weight | gRPC Score | Kafka Score |
|---|---:|---:|---:|
| Immediate response required | 5 | 5 | 1 |
| Sub-200ms latency needed | 5 | 5 | 1 |
| Replay/event history needed | 4 | 1 | 5 |
| Many consumers (fan-out) | 4 | 2 | 5 |
| Loose coupling priority | 3 | 2 | 5 |
| Simpler debugging in request path | 3 | 5 | 2 |
| Large burst throughput | 3 | 2 | 5 |
| Ordered stream processing | 2 | 2 | 4 |

Interpretation:
- If gRPC total is higher by 20%+, prefer gRPC.
- If Kafka total is higher by 20%+, prefer Kafka.
- If close, use hybrid and split command vs events.

## Contract Design Guidance

For gRPC:
- Define strict protobuf contracts.
- Set explicit deadlines/timeouts.
- Make RPCs idempotent where possible (`request_id`).
- Propagate trace/context metadata.

For Kafka:
- Define event schema versioning (Avro/Protobuf/JSON schema + registry).
- Use deterministic partition keys (`system_id`, `job_id`).
- Implement idempotent consumers and deduplication keys.
- Add DLQ and retry policy with backoff.

## Code Examples

### 1) gRPC Protobuf Contract

```proto
syntax = "proto3";
package jobs.v1;

service JobService {
  rpc StartJob(StartJobRequest) returns (StartJobResponse);
  rpc GetJobStatus(GetJobStatusRequest) returns (GetJobStatusResponse);
}

message StartJobRequest {
  string request_id = 1;
  string system_id = 2;
}

message StartJobResponse {
  string job_id = 1;
  string status = 2; // ACCEPTED
}

message GetJobStatusRequest {
  string job_id = 1;
}

message GetJobStatusResponse {
  string job_id = 1;
  string status = 2; // RUNNING, COMPLETED, FAILED
}
```

### 2) Node.js gRPC Server (minimal)

```ts
import * as grpc from "@grpc/grpc-js";
import * as protoLoader from "@grpc/proto-loader";

const pkgDef = protoLoader.loadSync("jobs.proto");
const proto = grpc.loadPackageDefinition(pkgDef) as any;

function startJob(
  call: grpc.ServerUnaryCall<any, any>,
  callback: grpc.sendUnaryData<any>
) {
  const { request_id, system_id } = call.request;
  // Idempotency lookup by (system_id, request_id) should happen here.
  callback(null, { job_id: "job-123", status: "ACCEPTED" });
}

const server = new grpc.Server();
server.addService(proto.jobs.v1.JobService.service, { StartJob: startJob });
server.bindAsync("0.0.0.0:50051", grpc.ServerCredentials.createInsecure(), () => {
  server.start();
});
```

### 3) Node.js Kafka Producer/Consumer (kafkajs)

```ts
import { Kafka } from "kafkajs";

const kafka = new Kafka({ clientId: "job-service", brokers: ["localhost:9092"] });
const producer = kafka.producer({ allowAutoTopicCreation: false });
const consumer = kafka.consumer({ groupId: "job-status-processors" });

async function publishJobCompleted() {
  await producer.connect();
  await producer.send({
    topic: "jobs.completed.v1",
    messages: [
      {
        key: "system-42", // partition key for ordering per system
        value: JSON.stringify({
          event_id: "evt-001",
          job_id: "job-123",
          system_id: "system-42",
          status: "COMPLETED"
        })
      }
    ]
  });
}

async function consumeJobCompleted() {
  await consumer.connect();
  await consumer.subscribe({ topic: "jobs.completed.v1", fromBeginning: false });
  await consumer.run({
    eachMessage: async ({ message }) => {
      const event = JSON.parse(String(message.value));
      // Deduplicate by event_id before applying side effects.
      console.log("processed", event.event_id);
    }
  });
}
```

### 4) Hybrid Pattern Example

```text
Client -> gRPC StartJob -> JobService
JobService -> Kafka jobs.started.v1
Workers consume jobs.started.v1 and process
Workers -> Kafka jobs.completed.v1 or jobs.failed.v1
API reads latest state from store and serves GetJobStatus via gRPC/REST
```

## Common Pitfalls

- Using Kafka for request/response user flows that need immediate answers.
- Using gRPC for long-running workflows without async handoff/events.
- No idempotency key (`request_id`/`event_id`) leading to duplicate side effects.
- No schema evolution strategy.
- Ignoring backpressure and retry storms.

## Recommended Baseline Spec (Template)

```yaml
use_case: "start simulation and track completion"
response_time_target_ms_p95: 200
max_throughput_events_per_sec: 5000
need_replay: true
consumer_count: 6
fanout_required: true
ordering_key: "system_id"
durability_retention_days: 14
decision:
  control_plane: "gRPC"
  data_plane_events: "Kafka"
rationale:
  - "Immediate submit/status APIs need low-latency RPC"
  - "Progress/completion must fan out and be replayable"
```

## Final Recommendation Pattern

For most distributed systems with heavy processing:
- Use **gRPC** for command/control (`start`, `cancel`, `status`).
- Use **Kafka** for domain events (`started`, `progress`, `completed`, `failed`).
- Keep contracts explicit, versioned, and idempotent in both layers.
