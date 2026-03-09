# Simulation Engine Integration Architecture

## Scope

Design for:
- `simulation-engine-frontend`
- `simulation-engine-backend`
- `calculation-engine`
- source database (queried by calculation engine)
- simulation persistence database (written by simulation backend)

Target data volume:
- up to `1,000,000` rows with `50` columns per job

## Protocols

1. Frontend to Simulation Backend
- Protocol: `HTTPS + REST`
- Use for: submit job, get status, cancel job
- Optional realtime updates: `SSE` or `WebSocket`

2. Simulation Backend to Calculation Engine
- Protocol: `gRPC`
- Use for: `StartCalculation`, `GetJobStatus`, `CancelCalculation`

3. Calculation Engine to Source Database
- Protocol: native DB wire protocol (`PostgreSQL/MySQL`)
- Use server-side cursor/streaming fetch for large result sets

4. Calculation Engine result delivery
- Do not return `1,000,000 x 50` rows inline over gRPC/HTTP
- Write chunked result files to object storage (`S3/MinIO`) as:
  - preferred: `Parquet + Snappy`
  - fallback: `CSV.gz`
- Publish completion event with file URIs and metadata via broker (`Kafka/NATS/RabbitMQ`)

5. Simulation Backend persistence
- Load files into staging table using bulk import (`COPY`/`LOAD DATA`)
- Validate counts/checksums
- Merge/upsert into target tables transactionally

## REST Contract (Frontend <-> Simulation Backend)

### Submit Job
`POST /v1/simulations/jobs`

Request:
```json
{
  "systemId": "sys-12345",
  "requestId": "c2f5f0d2-6ff1-4f2e-bf12-b8d4e5ef4d0a",
  "parameters": {
    "scenario": "baseline",
    "from": "2026-01-01",
    "to": "2026-01-31"
  },
  "result": {
    "format": "PARQUET",
    "compression": "SNAPPY"
  }
}
```

Response (`202 Accepted`):
```json
{
  "jobId": "job-9f1b",
  "status": "ACCEPTED",
  "createdAt": "2026-03-07T15:00:00Z"
}
```

### Get Job Status
`GET /v1/simulations/jobs/{jobId}`

Response:
```json
{
  "jobId": "job-9f1b",
  "systemId": "sys-12345",
  "status": "RUNNING",
  "progress": {
    "phase": "CALCULATING",
    "percent": 42
  },
  "metrics": {
    "rowsProcessed": 420000
  },
  "updatedAt": "2026-03-07T15:02:10Z"
}
```

### Cancel Job
`POST /v1/simulations/jobs/{jobId}:cancel` -> `202 Accepted`

### Idempotency Rule
- Require `requestId`
- Map `(systemId, requestId) -> jobId` and return same `jobId` on duplicate submit

## gRPC Contract (Simulation Backend -> Calculation Engine)

```proto
syntax = "proto3";
package calc.v1;

service CalculationService {
  rpc StartCalculation(StartCalculationRequest) returns (StartCalculationResponse);
  rpc GetJobStatus(GetJobStatusRequest) returns (GetJobStatusResponse);
  rpc CancelCalculation(CancelCalculationRequest) returns (CancelCalculationResponse);
}

message StartCalculationRequest {
  string job_id = 1;
  string system_id = 2;
  string request_id = 3;
  map<string,string> parameters = 4;
  ResultSpec result_spec = 5;
}

message ResultSpec {
  string format = 1;       // PARQUET, CSV
  string compression = 2;  // SNAPPY, GZIP
  int32 target_chunk_mb = 3;
}

message StartCalculationResponse {
  string job_id = 1;
  string status = 2; // ACCEPTED
}

message GetJobStatusRequest { string job_id = 1; }
message GetJobStatusResponse { string job_id = 1; string status = 2; int32 percent = 3; }
message CancelCalculationRequest { string job_id = 1; }
message CancelCalculationResponse { string job_id = 1; string status = 2; }
```

## Event Contract (Calculation Engine -> Broker -> Simulation Backend)

Topic examples:
- `calc.job.status.v1`
- `calc.job.completed.v1`
- `calc.job.failed.v1`

Completion event payload:
```json
{
  "eventId": "evt-2d17",
  "eventType": "JOB_COMPLETED",
  "occurredAt": "2026-03-07T15:10:00Z",
  "jobId": "job-9f1b",
  "systemId": "sys-12345",
  "rowCount": 1000000,
  "columnCount": 50,
  "schemaVersion": "2026-03-01",
  "result": {
    "format": "PARQUET",
    "compression": "SNAPPY",
    "files": [
      {
        "uri": "s3://calc-results/job-9f1b/part-0001.parquet",
        "rows": 250000,
        "checksum": "sha256:..."
      },
      {
        "uri": "s3://calc-results/job-9f1b/part-0002.parquet",
        "rows": 250000,
        "checksum": "sha256:..."
      }
    ]
  }
}
```

## Retry and Idempotency Rules

1. Control-plane calls (`REST`, `gRPC`)
- Exponential backoff with jitter
- Keep `StartCalculation` timeout short (`5-15s`)
- Long-running work is async via status/event

2. Broker processing
- Assume at-least-once delivery
- Deduplicate by `eventId` (or tuple: `jobId + eventType + sequence`)
- Send poison messages to dead-letter topic

3. Storage consistency
- Write result to temporary prefix
- Publish completion only after manifest commit
- Include checksums per part

4. Import safety
- Import per-job into staging
- Merge/upsert in a transaction
- Mark job `PERSISTED` only after successful commit

## Suggested State Machine

`ACCEPTED -> RUNNING -> CALCULATED -> PERSISTING -> COMPLETED`

Error/stop paths:
- `* -> FAILED`
- `ACCEPTED/RUNNING -> CANCELLING -> CANCELLED`

## Persistence Pattern

Staging table example:
- `staging_results(job_id, system_id, col1..col50, ingest_ts, part_id)`

Flow:
1. Bulk-load all file parts to staging
2. Validate expected row counts/checksums
3. Merge/upsert to target
4. Write `job_runs` audit record

## Diagrams

- High-level flow: see `simulation-engine-flow.mmd`
- Sequence flow: see `simulation-engine-sequence.mmd`
