package com.example.chronos.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the Kafka topic(s) owned by this service.
 * Spring Kafka's KafkaAdmin will create them automatically on startup if they do not yet exist.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${chronos.kafka.topic.traffic-log}")
    private String trafficLogTopic;

    /**
     * Topic that receives one Avro-encoded {@code TrafficLogEvent} message per inbound
     * HTTP request handled by the Simulation controller.
     *
     * <ul>
     *   <li>3 partitions – allows up to 3 parallel consumers</li>
     *   <li>1 replica  – suitable for single-broker local / dev clusters</li>
     *   <li>7-day retention (604 800 000 ms)</li>
     * </ul>
     */
    @Bean
    public NewTopic simulationTrafficLogTopic() {
        return TopicBuilder.name(trafficLogTopic)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000")
                .config("cleanup.policy", "delete")
                .build();
    }
}

