package com.example.chronos.controller;

import com.example.chronos.avro.TrafficLogEvent;
import com.example.chronos.kafka.TrafficLogProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/simulation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Simulation", description = "Simulation-related endpoints")
public class SimulationController {

    private final TrafficLogProducer trafficLogProducer;

    @Value("${spring.application.name:chronos}")
    private String serviceName;

    @Operation(summary = "Ping the simulation server", description = "Returns 200 OK when the server is reachable")
    @ApiResponse(responseCode = "200", description = "Server is alive")
    @GetMapping(path = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Void> ping() {
        return ResponseEntity.ok().build();
    }

    /**
     * Manually publishes a custom traffic log event to Kafka using Avro serialization.
     * 
     * Note: The SimulationTrafficLoggingFilter already automatically logs ALL requests
     * to /simulation/** endpoints. This endpoint is for demonstration purposes and
     * allows manual/custom event publishing.
     *
     * @param request HTTP request for extracting metadata
     * @return Success message with correlation ID
     */
    @Operation(
        summary = "Manually log a custom event to Kafka",
        description = "Creates and publishes a TrafficLogEvent to Kafka using Avro binary serialization. " +
                      "This demonstrates direct Kafka publishing from the controller."
    )
    @ApiResponse(responseCode = "200", description = "Event successfully published to Kafka")
    @PostMapping(path = "/log-event", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> logCustomEvent(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, Object> payload) {

        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();

        try {
            // Build Avro event using the generated TrafficLogEvent class
            TrafficLogEvent event = TrafficLogEvent.newBuilder()
                    .setCorrelationId(correlationId)
                    .setTimestampMs(Instant.ofEpochMilli(startTime))
                    .setMethod(request.getMethod())
                    .setPath(request.getRequestURI())
                    .setQueryString(request.getQueryString())
                    .setStatusCode(200)
                    .setDurationMs(0L) // Will be set after processing
                    .setClientIp(getClientIp(request))
                    .setUserAgent(request.getHeader("User-Agent"))
                    .setRequestBodySizeBytes(payload != null ? payload.toString().length() : 0L)
                    .setResponseBodySizeBytes(0L)
                    .setServiceName(serviceName)
                    .build();

            // Publish to Kafka (async)
            trafficLogProducer.publish(event);

            long duration = System.currentTimeMillis() - startTime;
            
            log.info("📊 Custom event published to Kafka [correlationId={}, method={}, path={}, duration={}ms]",
                    correlationId, request.getMethod(), request.getRequestURI(), duration);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Event published to Kafka topic: simulation.traffic.log",
                    "correlationId", correlationId,
                    "format", "Avro binary",
                    "durationMs", String.valueOf(duration)
            ));

        } catch (Exception e) {
            log.error("Failed to publish custom event [correlationId={}]: {}", 
                      correlationId, e.getMessage(), e);
            
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Failed to publish event: " + e.getMessage(),
                    "correlationId", correlationId
            ));
        }
    }

    /**
     * Example endpoint that demonstrates business logic with Avro logging.
     * Simulates a calculation and logs the result to Kafka.
     */
    @Operation(
        summary = "Run simulation and log results",
        description = "Executes a simulation task and publishes detailed metrics to Kafka using Avro"
    )
    @ApiResponse(responseCode = "200", description = "Simulation completed successfully")
    @PostMapping(path = "/run", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> runSimulation(
            HttpServletRequest request,
            @RequestBody Map<String, Object> simulationParams) {

        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();

        log.info("🚀 Starting simulation [correlationId={}, params={}]", correlationId, simulationParams);

        // Simulate some business logic
        try {
            Thread.sleep(100); // Simulate computation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long duration = System.currentTimeMillis() - startTime;

        // Create Avro event with simulation metadata
        TrafficLogEvent event = TrafficLogEvent.newBuilder()
                .setCorrelationId(correlationId)
                .setTimestampMs(Instant.ofEpochMilli(startTime))
                .setMethod(request.getMethod())
                .setPath(request.getRequestURI())
                .setQueryString(request.getQueryString())
                .setStatusCode(200)
                .setDurationMs(duration)
                .setClientIp(getClientIp(request))
                .setUserAgent(request.getHeader("User-Agent"))
                .setRequestBodySizeBytes((long) simulationParams.toString().length())
                .setResponseBodySizeBytes(0L)
                .setServiceName(serviceName)
                .build();

        // Publish to Kafka using Avro serialization
        trafficLogProducer.publish(event);

        log.info("✅ Simulation completed [correlationId={}, duration={}ms, published to Kafka]",
                correlationId, duration);

        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "correlationId", correlationId,
                "durationMs", duration,
                "params", simulationParams,
                "message", "Results published to Kafka (Avro binary)",
                "topic", "simulation.traffic.log"
        ));
    }

    /**
     * Resolves the real client IP address, respecting X-Forwarded-For header.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
