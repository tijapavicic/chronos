package com.example.chronos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
@Jacksonized
@Schema(description = "Standard API error response")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred (ISO-8601)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime timestamp;

    @Schema(description = "HTTP status code")
    Integer status;

    @Schema(description = "Short error description")
    String error;

    @Schema(description = "Human readable message")
    String message;

    @Schema(description = "Request path that caused the error")
    String path;

    @Schema(description = "Optional list of detailed error messages (for validation etc.)")
    List<String> details;

    @Schema(description = "Optional machine-readable error code")
    String code;

    @Schema(description = "Correlation id propagated from incoming request or generated")
    String correlationId;
}
