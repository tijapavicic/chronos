package com.example.chronos.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BadRequestException Tests")
class BadRequestExceptionTest {

    @Test
    @DisplayName("Constructor with message should create exception")
    void constructorWithMessage() {
        // Given
        String message = "Invalid request";

        // When
        BadRequestException exception = new BadRequestException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Constructor with message and cause should create exception")
    void constructorWithMessageAndCause() {
        // Given
        String message = "Invalid request";
        Throwable cause = new IllegalArgumentException("Illegal argument");

        // When
        BadRequestException exception = new BadRequestException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Should be a RuntimeException")
    void shouldBeRuntimeException() {
        // Given / When
        BadRequestException exception = new BadRequestException("test");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
