# Spring Boot Integration Checklist — Parquet

Use case: expose large analytical exports (download or object-store manifest) from a Spring Boot service currently serving JSON/NDJSON.

## 1. Libraries
- Add Apache Parquet + Arrow writer dependencies:
  ```xml
  <dependency>
      <groupId>org.apache.parquet</groupId>
      <artifactId>parquet-avro</artifactId>
      <version>1.13.1</version>
  </dependency>
  <dependency>
      <groupId>org.apache.arrow</groupId>
      <artifactId>arrow-vector</artifactId>
  </dependency>
  ```
- For Kotlin/Java records, create a schema builder (Avro schema reused for Parquet writer).

## 2. Controller Pattern (Streaming Response)
```java
@GetMapping(value = "/exports/{jobId}", produces = "application/octet-stream")
public ResponseEntity<StreamingResponseBody> download(@PathVariable UUID jobId) {
    StreamingResponseBody body = outputStream -> parquetService.write(jobId, outputStream);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=job-" + jobId + ".parquet")
        .body(body);
}
```
- Replace JSON serialization with a service that writes Parquet rows directly into the response stream (no buffering).

## 3. Writer Skeleton
```java
public void write(UUID jobId, OutputStream out) {
    try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
            .<GenericRecord>builder(new OutputStreamOutputFile(out))
            .withSchema(schema)
            .withCompressionCodec(CompressionCodecName.SNAPPY)
            .build()) {
        repository.streamRows(jobId, batch -> batch.forEach(writer::write));
    }
}
```
- `repository.streamRows` should use JDBC cursor/batch to avoid loading 1M rows.

## 4. Controller Delta vs JSON
| Concern | JSON Export | Parquet Export |
| --- | --- | --- |
| MIME type | `application/json` / `application/x-ndjson` | `application/octet-stream` or `application/parquet` |
| Serialization | Jackson writes row-by-row | Parquet writer + schema |
| Client hints | Browser renders JSON inline | Force download via `Content-Disposition` |
| Compression | Gzip (optional) | Built-in Snappy; optionally wrap response with `GZIPOutputStream` |

## 5. Post-processing & Storage
- Consider writing large files to S3/Object Storage first, then return a signed URL rather than streaming through the app node.
- Publish manifest metadata (row count, schema hash) so downstream Spark/DuckDB jobs can verify.

