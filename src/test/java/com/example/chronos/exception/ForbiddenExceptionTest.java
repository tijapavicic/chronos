package com.example.chronos.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ForbiddenException Tests")
class ForbiddenExceptionTest {

    @Test
    @DisplayName("Constructor with message should create exception")
    void constructorWithMessage() {
        // Given
        String message = "Access forbidden";

        // When
        ForbiddenException exception = new ForbiddenException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("Constructor with message and cause should create exception")
    void constructorWithMessageAndCause() {
        // Given
        String message = "Access forbidden";
        Throwable cause = new SecurityException("Insufficient permissions");

        // When
        ForbiddenException exception = new ForbiddenException(message, cause);

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
        ForbiddenException exception = new ForbiddenException("test");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
