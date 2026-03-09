# S-DB Snapshot Transfer (Developer One-Pager)

## Context
- Current payload is **>1,000,000 rows** per export cycle.
- Estimated transfer size is **~450 MB per snapshot** (depends on schema width, serialization format, and compression).
- This workload is analytics-oriented, not low-latency transactional traffic.

## Problem Statement
Using a traditional synchronous REST request-response model for this volume introduces:
- High timeout/retry risk
- Unstable latency and backpressure on source services
- Tight runtime coupling between producer and consumer

## Solution Statement: gRPC
Deliver S-DB snapshots through a decoupled, event-driven bulk-transfer workflow that stages data outside the OLTP path, streams objects through a durable transport (object store + queue, Kafka, or gRPC streaming), and lets consumers ingest on their own schedule with resumable checkpoints.

## Solution Statement: Kafka
Leverage Kafka topics as the control plane and data plane coordinator: emit `SnapshotReady` events with object-store pointers (or chunked binary payloads via compacted topics), enable consumer groups per downstream system for horizontal scaling, and rely on Kafka retention plus exactly-once semantics (idempotent producers + transactional consumers) so snapshots can be replayed or resumed without coordinating tightly coupled REST calls.

## Architectural Position
S-DB exists as a **separate analytics extraction layer**:
- Source DB -> extraction job -> S-DB analytics table(s) -> snapshot transfer
- Goal: isolate heavy analytical reads from OLTP paths and provide consistent, analysis-ready datasets

## Required Communication Pattern
We do **not** require real-time request-response.
We require **event-driven snapshot delivery**:

1. Business/system event occurs.
2. Extraction process materializes a consistent snapshot in S-DB.
3. Snapshot is published/transferred to analytics consumers.
4. Consumer processes snapshot asynchronously.

## Implementation Guidance
- Prefer asynchronous pipeline (event bus / job trigger / batch export), not synchronous REST endpoint.
- Snapshot boundary must be explicit (`snapshot_id`, timestamp/window, source version).
- Transfers should be idempotent (`snapshot_id` as dedup key).
- Support resumable/retriable delivery for large payloads.
- Track operational metrics: extraction duration, snapshot size (MB), lag, failure rate, retry count.

## Detailed Technical Proposal
- **Snapshot orchestration**: Use a scheduler (Airflow, Argo, or Spring Batch) to trigger extract jobs, persist metadata (`snapshot_id`, schema hash, row count) in a control table, and emit `SnapshotReady` events on Kafka or an event bus.
- **Serialization format**: Store snapshot payloads as columnar files (Parquet or ORC) in object storage for down-stream compression benefits; optionally offer row-based JSON for debugging.
- **Transport**: Push object-location references (URI + checksum) via Kafka topics or gRPC streaming control channel; large binaries move through S3/GCS/Azure Blob signed URLs to avoid overloading the message bus.
- **Ingestion contract**: Consumers acknowledge receipt per `snapshot_id`, persist state to allow replay, and expose metrics (lag, throughput) to a shared Grafana dashboard.
- **Resiliency**: Enable chunked uploads/downloads, leverage checksum validation before commit, and support replay-on-demand using retained Kafka topics or object-versioning.
- **Security & governance**: Encrypt data at rest (KMS-managed keys) and in transit (mTLS), apply IAM-scoped service accounts, and log data access for audit.
- **Operations**: Maintain runbooks for snapshot failures, define SLOs (e.g., snapshot availability within 30 minutes of trigger), and automate paging when lag exceeds thresholds.

## Decision
Implement high-volume analytics transfer as an **event-triggered DB snapshot pipeline via S-DB**, not as a traditional REST service.
