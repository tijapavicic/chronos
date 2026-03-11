package com.example.chronos.web;

import com.example.chronos.avro.TrafficLogEvent;
import com.example.chronos.config.CorrelationIdFilter;
import com.example.chronos.kafka.TrafficLogProducer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Servlet filter that captures HTTP traffic metadata for every request under
 * {@code /simulation/**} and publishes an Avro-encoded {@link TrafficLogEvent}
 * to Kafka after the response has been written.
 *
 * <h3>Design notes</h3>
 * <ul>
 *   <li>{@link ContentCachingRequestWrapper} and {@link ContentCachingResponseWrapper}
 *       buffer bodies in memory so we can read their sizes <em>after</em> the controller
 *       has written the response.</li>
 *   <li>{@code wrappedResponse.copyBodyToResponse()} <strong>must</strong> be called in
 *       the {@code finally} block to forward the buffered body back to the client.</li>
 *   <li>Kafka publishing is asynchronous (fire-and-forget) – failures never block or
 *       degrade the HTTP response.</li>
 *   <li>Order 10 ensures this filter runs <em>after</em> {@link CorrelationIdFilter}
 *       (which has a lower explicit order), so the correlation ID is already in the
 *       request attributes when we read it.</li>
 * </ul>
 */
@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class SimulationTrafficLoggingFilter extends OncePerRequestFilter {

    private static final String SIMULATION_PATH_PREFIX = "/simulation";

    @Value("${spring.application.name:chronos}")
    private String serviceName;

    private final TrafficLogProducer trafficLogProducer;

    // ─── Filter routing ───────────────────────────────────────────────────────

    /**
     * Skip this filter for every path that does NOT belong to the Simulation controller.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(SIMULATION_PATH_PREFIX);
    }

    // ─── Core logic ───────────────────────────────────────────────────────────

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Wrap request & response so we can read body bytes after the chain runs
        ContentCachingRequestWrapper  wrappedRequest  = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTimeMs = System.currentTimeMillis();

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = System.currentTimeMillis() - startTimeMs;

            publishTrafficLog(wrappedRequest, wrappedResponse, startTimeMs, durationMs);

            // CRITICAL: copy buffered body back so the client actually receives the response
            wrappedResponse.copyBodyToResponse();
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void publishTrafficLog(ContentCachingRequestWrapper  request,
                                   ContentCachingResponseWrapper response,
                                   long startTimeMs,
                                   long durationMs) {
        try {
            String correlationId = (String) request.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            TrafficLogEvent event = TrafficLogEvent.newBuilder()
                    .setCorrelationId(correlationId)
                    .setTimestampMs(Instant.ofEpochMilli(startTimeMs))
                    .setMethod(request.getMethod())
                    .setPath(request.getRequestURI())
                    .setQueryString(request.getQueryString())          // nullable → avro union
                    .setStatusCode(response.getStatus())
                    .setDurationMs(durationMs)
                    .setClientIp(resolveClientIp(request))
                    .setUserAgent(request.getHeader("User-Agent"))     // nullable → avro union
                    .setRequestBodySizeBytes(request.getContentAsByteArray().length)
                    .setResponseBodySizeBytes(response.getContentAsByteArray().length)
                    .setServiceName(serviceName)
                    .build();

            trafficLogProducer.publish(event);

        } catch (Exception e) {
            // Never let audit-logging break the HTTP pipeline
            log.error("Failed to build/publish traffic log event for path={}: {}",
                    request.getRequestURI(), e.getMessage(), e);
        }
    }

    /**
     * Returns the real client IP, respecting {@code X-Forwarded-For} when the app
     * sits behind a reverse proxy or load balancer.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

