package com.example.chronos.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    static Stream<Arguments> errorResponseProvider() {
        return Stream.of(
                Arguments.of(404, "Not Found", "facility not found", "/api/facilities", List.of("facilityName: must not be blank"), "ERR_VALIDATION", "abc-123"),
                Arguments.of(400, "Bad Request", "invalid input", "/api/facilities", List.of("id: must be numeric"), "ERR_INPUT", "def-456"),
                Arguments.of(500, "Internal Server Error", "server error", "/api/facilities", List.of("unexpected error"), "ERR_SERVER", "ghi-789")
        );
    }

    @ParameterizedTest
    @MethodSource("errorResponseProvider")
    void serializationAndAccessors(int status,
                                   String error,
                                   String message,
                                   String path,
                                   List<String> details,
                                   String code,
                                   String correlationId) throws Exception {
        OffsetDateTime ts = OffsetDateTime.parse("2026-02-03T00:00:00Z");
        ErrorResponse er = ErrorResponse.builder()
                .timestamp(ts)
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .details(details)
                .code(code)
                .correlationId(correlationId)
                .build();

        // Accessors (Lombok-generated)
        assertEquals(status, er.getStatus());
        assertEquals(message, er.getMessage());
        assertEquals(error, er.getError());
        assertEquals(ts, er.getTimestamp());
        assertEquals(path, er.getPath());
        assertNotNull(er.getDetails());
        assertEquals(code, er.getCode());
        assertEquals(correlationId, er.getCorrelationId());

        // JSON serialization — configure ObjectMapper like the application
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String json = om.writeValueAsString(er);
        assertNotNull(json);

        JsonNode node = om.readTree(json);
        assertEquals("2026-02-03T00:00:00Z", node.get("timestamp").asText());
        assertEquals(status, node.get("status").asInt());
        assertEquals(error, node.get("error").asText());
        assertEquals(message, node.get("message").asText());
        assertEquals(path, node.get("path").asText());
        assertTrue(node.get("details").isArray());
        assertEquals(details.get(0), node.get("details").get(0).asText());
        assertEquals(code, node.get("code").asText());
        assertEquals(correlationId, node.get("correlationId").asText());
    }
}
