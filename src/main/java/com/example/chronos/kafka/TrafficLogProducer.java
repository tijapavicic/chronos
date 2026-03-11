package com.example.chronos.kafka;

import com.example.chronos.avro.TrafficLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Publishes {@link TrafficLogEvent} records to the configured Kafka topic.
 *
 * <p>Serialisation strategy: Apache Avro binary encoding without a Schema Registry.
 * The schema is embedded in the generated {@code TrafficLogEvent.SCHEMA$} constant
 * and is stable across deployments. Consumers can decode the payload using the same
 * {@code TrafficLogEvent.avsc} schema file checked into this repository.</p>
 *
 * <p>All Kafka sends are <b>fire-and-forget</b>: failures are logged but never propagate
 * to the HTTP request thread, so traffic logging never degrades API availability.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrafficLogProducer {

    @Value("${chronos.kafka.topic.traffic-log}")
    private String topic;

    // Spring Boot auto-configures KafkaTemplate<Object,Object>; the type parameters are
    // erased at runtime so injection of KafkaTemplate<String, byte[]> is safe here.
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    /**
     * Asynchronously publishes a {@link TrafficLogEvent} to Kafka.
     * The correlation ID is used as the message key so that all events belonging to
     * a single request land in the same partition (ordering guarantee).
     *
     * @param event the traffic log event to publish
     */
    public void publish(TrafficLogEvent event) {
        try {
            byte[] payload = serializeToAvro(event);

            kafkaTemplate.send(topic, event.getCorrelationId(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish traffic log event [correlationId={}, topic={}]: {}",
                                    event.getCorrelationId(), topic, ex.getMessage());
                        } else {
                            log.debug("Traffic log event published [correlationId={}, topic={}, partition={}, offset={}]",
                                    event.getCorrelationId(),
                                    topic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });

        } catch (Exception e) {
            log.error("Error preparing Avro payload for traffic log [correlationId={}]: {}",
                    event.getCorrelationId(), e.getMessage(), e);
        }
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    /**
     * Serialises a {@link TrafficLogEvent} to Avro binary format (no framing header).
     * A new {@link SpecificDatumWriter} instance is used per call to stay thread-safe.
     */
    private byte[] serializeToAvro(TrafficLogEvent event) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            SpecificDatumWriter<TrafficLogEvent> writer =
                    new SpecificDatumWriter<>(TrafficLogEvent.SCHEMA$);
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
            writer.write(event, encoder);
            encoder.flush();
            return baos.toByteArray();
        }
    }
}

