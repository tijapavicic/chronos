package com.example.chronos.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConflictException Tests")
class ConflictExceptionTest {

    @Test
    @DisplayName("Constructor with message should create exception")
    void constructorWithMessage() {
        // Given
        String message = "Resource already exists";

        // When
        ConflictException exception = new ConflictException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Constructor with message and cause should create exception")
    void constructorWithMessageAndCause() {
        // Given
        String message = "Conflict detected";
        Throwable cause = new IllegalStateException("Duplicate entry");

        // When
        ConflictException exception = new ConflictException(message, cause);

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
        ConflictException exception = new ConflictException("test");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
