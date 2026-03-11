package com.example.chronos.kafka;

import com.example.chronos.avro.TrafficLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Demo Kafka consumer that listens to {@code simulation.traffic.log} topic
 * and deserializes Avro-encoded {@link TrafficLogEvent} messages.
 *
 * <p>This consumer is for <strong>demonstration and debugging purposes only</strong>.
 * In a production environment, traffic logs would typically be consumed by a
 * dedicated log aggregation / analytics service (e.g., ELK, Splunk, DataDog).</p>
 *
 * <p><strong>Note:</strong> This consumer is DISABLED by default via the
 * {@code @ConditionalOnProperty} annotation (commented out). Uncomment to enable.</p>
 */
@Service
@Slf4j
// Uncomment the next line to enable this consumer:
// @ConditionalOnProperty(name = "chronos.kafka.consumer.traffic-log.enabled", havingValue = "true")
public class TrafficLogConsumer {

    /**
     * Listens to the traffic log topic and deserializes each Avro message.
     * The consumer group ID is set to "chronos-traffic-log-viewer" so it doesn't
     * interfere with production consumers.
     *
     * @param payload binary Avro payload
     * @param partition partition the message came from
     * @param offset message offset
     */
    @KafkaListener(
            topics = "${chronos.kafka.topic.traffic-log}",
            groupId = "chronos-traffic-log-viewer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload byte[] payload,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            TrafficLogEvent event = deserializeFromAvro(payload);

            log.info("═══════════════════════════════════════════════════════════════════════════");
            log.info("📊 Traffic Log Event [partition={}, offset={}]", partition, offset);
            log.info("   Correlation ID     : {}", event.getCorrelationId());
            log.info("   Timestamp          : {}", event.getTimestampMs());
            log.info("   Method             : {}", event.getMethod());
            log.info("   Path               : {}", event.getPath());
            log.info("   Query String       : {}", event.getQueryString());
            log.info("   Status Code        : {}", event.getStatusCode());
            log.info("   Duration (ms)      : {}", event.getDurationMs());
            log.info("   Client IP          : {}", event.getClientIp());
            log.info("   User-Agent         : {}", event.getUserAgent());
            log.info("   Request Body Size  : {} bytes", event.getRequestBodySizeBytes());
            log.info("   Response Body Size : {} bytes", event.getResponseBodySizeBytes());
            log.info("   Service Name       : {}", event.getServiceName());
            log.info("═══════════════════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("Failed to deserialize traffic log event [partition={}, offset={}]: {}",
                    partition, offset, e.getMessage(), e);
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Deserializes an Avro binary payload into a {@link TrafficLogEvent} instance.
     */
    private TrafficLogEvent deserializeFromAvro(byte[] payload) throws IOException {
        SpecificDatumReader<TrafficLogEvent> reader =
                new SpecificDatumReader<>(TrafficLogEvent.SCHEMA$);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(payload, null);
        return reader.read(null, decoder);
    }
}

